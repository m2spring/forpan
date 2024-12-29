package org.springdot.forpan.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.springdot.forpan.model.FwRecord;

import java.util.List;
import java.util.Optional;

class MainWindow{

    private Env env;
    private Stage stage;
    private TableView<FwRecord> table;
    private TextField status;

    public MainWindow(Env env, Stage stage){
        this.env = env;
        this.stage = stage;
    }

    void show(){
        stage.setTitle("Forpan");

        status = new TextField();
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
        }

        var bp = new BorderPane();
        bp.setTop(toolbar);
        bp.setCenter(mkTable());
        bp.setBottom(status);

        var scene = new Scene(bp,800,600);
        scene.setOnKeyPressed(this::handleKey);

        stage.setScene(scene);
        stage.setX(4000);
        stage.show();
        table.requestFocus();
    }

    void setStatus(String msg){
        Platform.runLater(() -> status.setText(msg));
    }

    private void handleKey(KeyEvent ev){
        if (Common.KEY_CONTROL_Q.match(ev) || Common.KEY_ESC.match(ev)){
            System.out.println("exit");
            Platform.exit();
            System.exit(0);
        }else if (Common.KEY_CONTROL_R.match(ev)){
            refreshTable();
        }else if (Common.KEY_CONTROL_N.match(ev) || Common.KEY_INSERT.match(ev)){
            addRecord(null);
        }else if (Common.KEY_CONTROL_D.match(ev) || Common.KEY_DELETE.match(ev)){
            delRecord(null);
        }
    }

    private Control mkTable(){
        table = new TableView<FwRecord>();
        table.setPlaceholder(new Label(""));

        var fwdrCol = new TableColumn<FwRecord,String>("Forwarder");
        var trgtCol = new TableColumn<FwRecord,String>("Target");

        fwdrCol.setCellValueFactory(new PropertyValueFactory<FwRecord,String>("forwarder"));
        fwdrCol.prefWidthProperty().bind(table.widthProperty().multiply(0.65));
        trgtCol.setCellValueFactory(new PropertyValueFactory<FwRecord,String>("target"));
        trgtCol.prefWidthProperty().bind(table.widthProperty().multiply(0.3));
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(fwdrCol,trgtCol);

        return table;
    }

    void refreshTable(){
        SortState sortState = SortState.get(table);
        FwRecord currFwdr = getSelectedForwarder();
        setStatus("loading model...");
        env.model.syncFromServer();
        List<FwRecord> recs = env.model.getRecords();
        setStatus("model loaded ("+recs.size()+")");
        table.setItems(FXCollections.observableArrayList(recs));
        sortState.apply(table);
        if (currFwdr != null){
            gotoForwarderByName(currFwdr.getForwarder());
        }else{
            table.getSelectionModel().select(0);
            Platform.runLater(() -> table.scrollTo(0));
        }
    }

    void gotoForwarderByName(String fwdr){
        ObservableList<FwRecord> recs = table.getItems();
        for (int i=0, n=recs.size(); i<n; i++){
            FwRecord rec = recs.get(i);
            if (fwdr.equalsIgnoreCase(rec.getForwarder())){
                table.getSelectionModel().clearSelection();
                table.getSelectionModel().select(i);
                table.scrollTo(i);
                table.requestFocus();
                return;
            }
        }
    }

    private FwRecord getSelectedForwarder(){
        ObservableList<FwRecord> items = table.getSelectionModel().getSelectedItems();
        return items.size() < 1? null : items.get(0);
    }

    private void addRecord(ActionEvent aev){
        new AddRecWin(env,stage).show();
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
            env.model.delForwarder(currFwdr);
            refreshTable();
            int size = table.getItems().size();
            if (currIdx > size) currIdx = size-1;
            table.getSelectionModel().select(currIdx);
        }
    }
}
