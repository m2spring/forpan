package org.springdot.forpan.gui;

import atlantafx.base.controls.CustomTextField;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2MZ;
import org.springdot.forpan.mailscan.MailboxAddressLister;
import org.springdot.forpan.mailscan.ThunderbirdProfile;
import org.springdot.forpan.model.ForpanModel;
import org.springdot.forpan.model.FwRecord;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.springdot.forpan.model.RecordState.COMMISSIONED;
import static org.springdot.forpan.model.RecordState.DECOMMISSIONED;
import static org.springdot.forpan.util.Util.callIfIntPropertyIsSet;

class MainWindow{
    private static final Logger LOG = Logger.getLogger(MainWindow.class.getName());

    private Env env;
    private Stage stage;
    private FilteredList<FwRecord> filteredRecs;
    private TableView<FwRecord> table;
    private TextField statusField;
    private CustomTextField searchField;
    private Button editButton;
    private Button decommissionButton;
    private TableColumn<FwRecord,Integer> mailboxCol;
    private volatile Map<String,Integer> mailboxRank = Map.of();

    public MainWindow(Env env, Stage stage){
        this.env = env;
        this.stage = stage;
    }

    void show(){
        stage.setTitle("Forpan");

        statusField = new TextField();
//        status.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));

        var toolbar = new HBox();
        {
            var c = toolbar.getChildren();
            {
                var b = new Button("Reload");
                b.setTooltip(new Tooltip("Reload forwarders"));
                b.setOnAction(aev -> refreshTable());
                c.add(b);
            }
            {
                var b = new Button("Add");
                b.setTooltip(new Tooltip("Add a new forwarder"));
                b.setOnAction(this::addRecord);
                c.add(b);
            }
            {
                editButton = new Button("Edit");
                editButton.setTooltip(new Tooltip("Edit current forwarder"));
                editButton.setOnAction(this::editRecord);
                c.add(editButton);
            }
            {
                decommissionButton = new Button("Decommission");
                decommissionButton.setTooltip(new Tooltip("Decommission current forwarder"));
                decommissionButton.setOnAction(this::decommissionRecord);
                c.add(decommissionButton);
            }
            {
                var r = new Region();
                HBox.setHgrow(r,Priority.ALWAYS);
                c.add(r);
            }
            {
                searchField = new CustomTextField();
                searchField.setLeft(new FontIcon(Material2MZ.SEARCH));
                searchField.setPromptText("Search (Ctrl-F)...");
                searchField.setOnKeyPressed(ev -> {
                    if (Common.KEY_CURSOR_DN.match(ev)) table.requestFocus();
                });
                searchField.textProperty().addListener((observable,oldVal,newVal) -> {
                    setFilterPredicate(newVal);
                });
                c.add(searchField);
            }
        }

        var bp = new BorderPane();
        bp.setTop(toolbar);
        bp.setCenter(mkTable());
        bp.setBottom(statusField);

        table.getSelectionModel().selectedItemProperty().addListener((observable,oldVal,newVal) -> updateButtonStates(newVal));
        updateButtonStates(table.getSelectionModel().getSelectedItem());

        var scene = new Scene(bp,800,600);
        scene.setOnKeyPressed(this::handleKey);

        stage.setScene(scene);
        callIfIntPropertyIsSet("window.x",v -> stage.setX(v));
        callIfIntPropertyIsSet("window.y",v -> stage.setY(v));
        stage.show();
        table.requestFocus();
    }

    private Timeline activeStatusTimeline = null;

    synchronized void setStatus(String msg){
        if (activeStatusTimeline != null) activeStatusTimeline.stop();

        Platform.runLater(() -> statusField.setText(msg));

        activeStatusTimeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            statusField.clear();
            activeStatusTimeline = null;
        }));
        activeStatusTimeline.setCycleCount(1);
        activeStatusTimeline.play();
    }

    private void handleKey(KeyEvent ev){
        if (Common.KEY_CONTROL_Q.match(ev)){
            LOG.info("exit");
            Platform.exit();
            System.exit(0);
        }else if (Common.KEY_CONTROL_R.match(ev)){
            refreshTable();
        }else if (Common.KEY_CONTROL_N.match(ev) || Common.KEY_INSERT.match(ev)){
            addRecord(null);
        }else if (Common.KEY_CONTROL_E.match(ev) || Common.KEY_ENTER.match(ev)){
            editRecord(null);
        }else if (Common.KEY_CONTROL_D.match(ev) || Common.KEY_DELETE.match(ev)){
            decommissionRecord(null);
        }else if (Common.KEY_CONTROL_C.match(ev)){
            copyRecord();
        }else if (Common.KEY_CONTROL_F.match(ev)){
            searchField.requestFocus();
        }else if (Common.KEY_ESC.match(ev)){
            searchField.clear();
            ensureSelectedRowIsVisible();
            table.requestFocus();
        }
    }

    private Control mkTable(){
        table = new TableView<FwRecord>();
        table.setPlaceholder(new Label(""));

        var titleCol = new TableColumn<FwRecord,String>("Title");
        var fwdrCol = new TableColumn<FwRecord,String>("Forwarder");
        var statCol = new TableColumn<FwRecord,Void>("State");
        var trgtCol = new TableColumn<FwRecord,String>("Target");
        mailboxCol = new TableColumn<FwRecord,Integer>("Mailbox");

        titleCol.setCellValueFactory(new PropertyValueFactory<FwRecord,String>("title"));
        titleCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));

        fwdrCol.setCellValueFactory(new PropertyValueFactory<FwRecord,String>("forwarder"));
        fwdrCol.prefWidthProperty().bind(table.widthProperty().multiply(0.52));
        fwdrCol.setCellFactory(rec -> new TableCell<>(){
            private final Text text = new Text();
            private final Tooltip tooltip = new Tooltip();

            @Override
            protected void updateItem(String item, boolean empty){
                super.updateItem(item,empty);
                FwRecord rec = getTableRow().getItem();
                if (empty || rec == null || StringUtils.isBlank(item)){
                    setGraphic(null);
                }else{
                    text.setText(item);
                    boolean strikethrough = rec.getLastState() != COMMISSIONED;
                    text.setStrikethrough(strikethrough);
                    if (strikethrough){
                        tooltip.setText(item);
                        Tooltip.install(text,tooltip);
                    }else{
                        Tooltip.uninstall(text,tooltip);
                    }
                    setGraphic(text);
                }
            }
        });

        statCol.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        statCol.setCellFactory(rec -> new TableCell<>(){
            private final FontIcon icon = new FontIcon();
            private final Tooltip tooltip = new Tooltip();
            {
                Tooltip.install(icon,tooltip);
            }

            @Override
            protected void updateItem(Void item, boolean empty){
                super.updateItem(item,empty);
                FwRecord rec = getTableRow().getItem();
                if (empty || rec == null){
                    setGraphic(null);
                }else{
                    StateRender sr = StateRender.get(rec.getLastState());
                    icon.setIconLiteral(sr.icon());
                    icon.setStyle("-fx-icon-color: "+sr.color()+";");
                    tooltip.setText(sr.tooltip());
                    setGraphic(icon);
                }
            }
        });

        trgtCol.setCellValueFactory(new PropertyValueFactory<FwRecord,String>("target"));
        trgtCol.prefWidthProperty().bind(table.widthProperty().multiply(0.25));

        mailboxCol.setCellValueFactory(cd -> {
            String fwdr = cd.getValue().getForwarder();
            Integer rank = fwdr == null? null : mailboxRank.get(fwdr.toLowerCase(Locale.ROOT));
            return new SimpleObjectProperty<>(rank);
        });
        mailboxCol.setComparator((a,b) -> {
            if (a == null && b == null) return 0;
            if (a == null) return 1;
            if (b == null) return -1;
            return Integer.compare(a,b);
        });
        mailboxCol.prefWidthProperty().bind(table.widthProperty().multiply(0.08));

        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(titleCol,fwdrCol,statCol,trgtCol,mailboxCol);
        table.getSelectionModel().selectedItemProperty().addListener((observable,oldVal,newVal) -> {
            if (newVal == null && !table.getItems().isEmpty()){
                table.getSelectionModel().select(0);
            }
        });
        return table;
    }

    /**
     * Scans the mailbox(es) configured under config.properties' "mailscan.accounts" and
     * ranks each forwarder address by how recently it last showed up there (1 = most
     * recent), so a recently-used forwarder is easy to spot for possible decommissioning.
     * Runs on a background thread; safe to skip (leaves the Mailbox column blank) if
     * nothing is configured or the scan fails for any reason.
     */
    void loadMailboxRanks(){
        try{
            List<File> mboxFiles = ThunderbirdProfile.configuredInboxFiles();
            if (mboxFiles.isEmpty()) return;

            List<String> addresses = new MailboxAddressLister().listByRecency(mboxFiles);
            Map<String,Integer> rank = new HashMap<>();
            for (int i=0, n=addresses.size(); i<n; i++){
                rank.put(addresses.get(i),i+1);
            }
            mailboxRank = rank;

            Platform.runLater(table::refresh);
        }catch (Exception e){
            LOG.log(Level.WARNING,"could not scan configured mailboxes",e);
        }
    }

    void refreshTable(){
        refreshTable(model -> model.syncFromServer());
    }

    void refreshTableLocal(){
        refreshTable(model -> {});
    }

    void refreshTable(Consumer<ForpanModel> modelAction){
        SortState sortState = SortState.get(table);
        FwRecord currFwdr = getSelectedForwarder();
        setStatus("loading model...");
        modelAction.accept(env.model);
        List<FwRecord> recs = env.model.getRecords();
        setStatus("model loaded ("+recs.size()+")");

        // this method is also called from a background thread (see App.start()), so all
        // TableView mutations below - notably sortState.apply(), which touches the sort-order
        // list the column headers listen on - must happen on the FX application thread.
        Runnable applyToTable = () -> {
            filteredRecs = new FilteredList<>(FXCollections.observableArrayList(recs));
            setFilterPredicate(searchField.getText());

            SortedList<FwRecord> sortedRecs = new SortedList<>(filteredRecs);
            table.setItems(sortedRecs);
            sortedRecs.comparatorProperty().bind(table.comparatorProperty());
            table.refresh();

            sortState.apply(table);
            if (currFwdr != null){
                gotoForwarderByName(currFwdr.getForwarder());
            }else{
                table.getSelectionModel().select(0);
                table.scrollTo(0);
            }
        };

        if (Platform.isFxApplicationThread()){
            applyToTable.run();
        }else{
            Platform.runLater(applyToTable);
        }
    }

    private void setFilterPredicate(String searchStr){
        filteredRecs.setPredicate(rec ->
            StringUtils.containsIgnoreCase(rec.getForwarder(),searchStr)
            || StringUtils.containsIgnoreCase(rec.getTitle(),searchStr)
        );
    }

    void gotoForwarderByName(String fwdr){
        ObservableList<FwRecord> recs = table.getItems();
        for (int i=0, n=recs.size(); i<n; i++){
            FwRecord rec = recs.get(i);
            if (fwdr.equalsIgnoreCase(rec.getForwarder())){
                table.getSelectionModel().clearSelection();
                selectRow(i);
                Platform.runLater(() -> table.requestFocus());
                return;
            }
        }
    }

    private void ensureSelectedRowIsVisible(){
        if (table.getItems().isEmpty()) return;

        TableView.TableViewSelectionModel<FwRecord> sm = table.getSelectionModel();
        selectRow(sm.getSelectedItems().isEmpty()? 0 : sm.getSelectedIndex());
    }

    private void selectRow(int idx){
        if (table.getItems().isEmpty()) return;

        table.getSelectionModel().select(idx);
        if (table.getSkin() instanceof TableViewSkin<?> skin){
            VirtualFlow<?> flow = (VirtualFlow<?>)skin.getChildren().stream()
                .filter(node -> node instanceof VirtualFlow)
                .findFirst()
                .orElse(null);
            if (flow != null){
                IndexedCell first = flow.getFirstVisibleCell();
                IndexedCell last = flow.getLastVisibleCell();
                if (first != null && last != null && first.getIndex() <= idx && idx <= last.getIndex()){
                    // idx-th row is visible, nothing to scroll
                    return;
                }
            }
        }

        Platform.runLater(() -> table.scrollTo(idx));
    }

    private FwRecord getSelectedForwarder(){
        ObservableList<FwRecord> items = table.getSelectionModel().getSelectedItems();
        return items.size() < 1? null : items.get(0);
    }

    private void addRecord(ActionEvent aev){
        new RecordWindow(env,stage)
            .createNewFwdr()
            .show();
    }

    private void editRecord(ActionEvent aev){
        applyToCurrFwdr(currFwdr -> {
            new RecordWindow(env,stage)
                .setOrigRec(currFwdr)
                .show();
        });
    }

    private void decommissionRecord(ActionEvent aev){
        applyToCurrFwdr(currFwdr -> {
            var alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(stage);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Decommission Forwarder?");
            alert.setContentText(currFwdr.getForwarder()+" → "+currFwdr.getTarget());
            Optional<ButtonType> res = alert.showAndWait();

            if (res.get() == ButtonType.OK){
                int currIdx = table.getSelectionModel().getSelectedIndex();
                env.model.decommissionForwarder(currFwdr);
                refreshTable();
                int size = table.getItems().size();
                if (currIdx > size) currIdx = size-1;
                table.getSelectionModel().select(currIdx);
            }
        });
    }

    private void applyToCurrFwdr(Consumer<FwRecord> action){
        FwRecord currFwdr = getSelectedForwarder();
        if (isModifiable(currFwdr)){
            action.accept(currFwdr);
        }
    }

    private boolean isModifiable(FwRecord rec){
        return rec != null && rec.getLastState() != DECOMMISSIONED;
    }

    private void updateButtonStates(FwRecord rec){
        boolean modifiable = isModifiable(rec);
        editButton.setDisable(!modifiable);
        decommissionButton.setDisable(!modifiable);
    }

    private void copyRecord(){
        FwRecord currFwdr = getSelectedForwarder();
        if (currFwdr == null) return;

        String fwdr = currFwdr.getForwarder();
        Common.copyToClipboard(fwdr);
        setStatus("copied "+fwdr+" to clipboard");
    }
}
