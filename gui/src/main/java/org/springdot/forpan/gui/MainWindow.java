package org.springdot.forpan.gui;

import atlantafx.base.controls.CustomTextField;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
import org.springdot.forpan.model.ForpanModel;
import org.springdot.forpan.model.FwRecord;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.springdot.forpan.model.RecordState.COMMISSIONED;
import static org.springdot.forpan.util.Util.callIfIntPropertyIsSet;

class MainWindow{

    private Env env;
    private Stage stage;
    private FilteredList<FwRecord> filteredRecs;
    private TableView<FwRecord> table;
    private TextField statusField;
    private CustomTextField searchField;

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
                var b = new Button("Delete");
                b.setTooltip(new Tooltip("Delete current forwarder"));
                // TODO: Delete button should only be active if a row is selected
                b.setOnAction(this::delRecord);
                c.add(b);
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
            System.out.println("exit");
            Platform.exit();
            System.exit(0);
        }else if (Common.KEY_CONTROL_R.match(ev)){
            refreshTable();
        }else if (Common.KEY_CONTROL_N.match(ev) || Common.KEY_INSERT.match(ev)){
            addRecord(null);
        }else if (Common.KEY_CONTROL_D.match(ev) || Common.KEY_DELETE.match(ev)){
            delRecord(null);
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

        titleCol.setCellValueFactory(new PropertyValueFactory<FwRecord,String>("title"));
        titleCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));

        fwdrCol.setCellValueFactory(new PropertyValueFactory<FwRecord,String>("forwarder"));
        fwdrCol.prefWidthProperty().bind(table.widthProperty().multiply(0.6));
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

        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(titleCol,fwdrCol,statCol,trgtCol);
        table.getSelectionModel().selectedItemProperty().addListener((observable,oldVal,newVal) -> {
            if (newVal == null && !table.getItems().isEmpty()){
                table.getSelectionModel().select(0);
            }
        });
        return table;
    }

    void refreshTable(){
        refreshTable(model -> model.syncFromServer());
    }

    void refreshTable(Consumer<ForpanModel> modelAction){
        SortState sortState = SortState.get(table);
        FwRecord currFwdr = getSelectedForwarder();
        setStatus("loading model...");
        modelAction.accept(env.model);
        List<FwRecord> recs = env.model.getRecords();
        setStatus("model loaded ("+recs.size()+")");

        filteredRecs = new FilteredList<>(FXCollections.observableArrayList(recs));
        setFilterPredicate(searchField.getText());

        SortedList<FwRecord> sortedRecs = new SortedList<>(filteredRecs);
        table.setItems(sortedRecs);
        sortedRecs.comparatorProperty().bind(table.comparatorProperty());

        sortState.apply(table);
        if (currFwdr != null){
            gotoForwarderByName(currFwdr.getForwarder());
        }else{
            table.getSelectionModel().select(0);
            Platform.runLater(() -> table.scrollTo(0));
        }
    }

    private void setFilterPredicate(String searchStr){
        filteredRecs.setPredicate(rec ->
            StringUtils.containsIgnoreCase(rec.getForwarder(),searchStr)
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
        new RecordWindow(env,stage).show();
    }

    private void delRecord(ActionEvent aev){
        FwRecord currFwdr = getSelectedForwarder();
        if (currFwdr == null) return;

        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(stage);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Delete Forwarder?");
        alert.setContentText(currFwdr.getForwarder()+" → "+currFwdr.getTarget());
        Optional<ButtonType> res = alert.showAndWait();

        if (res.get() == ButtonType.OK){
            int currIdx = table.getSelectionModel().getSelectedIndex();
            env.model.removeForwarder(currFwdr);
            refreshTable();
            int size = table.getItems().size();
            if (currIdx > size) currIdx = size-1;
            table.getSelectionModel().select(currIdx);
        }
    }

    private void copyRecord(){
        FwRecord currFwdr = getSelectedForwarder();
        if (currFwdr == null) return;

        String fwdr = currFwdr.getForwarder();
        Common.copyToClipboard(fwdr);
        setStatus("copied "+fwdr+" to clipboard");
    }
}
