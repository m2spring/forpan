package org.springdot.forpan.gui;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springdot.forpan.core.Util;
import org.springdot.forpan.cpanel.api.CPanelAPI;
import org.springdot.forpan.model.FwModel;
import org.springdot.forpan.model.FwRecord;

public class App extends Application {

    private final static KeyCombination KEY_CONTROL_C = new KeyCodeCombination(KeyCode.C,KeyCodeCombination.CONTROL_DOWN);
    private final static KeyCombination KEY_ESC = new KeyCodeCombination(KeyCode.ESCAPE);

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
//        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        stage.setTitle("stage.title");


        var button = new Button("List Domains");
        button.setOnAction(this::buttonPressed);

        var pane = new VBox();
        pane.getChildren().addAll(button,new Button("button2"),mkTable());

//        var scene = new Scene(new StackPane(button), 640, 480);
        var scene = new Scene(pane, 640, 480);
        scene.setOnKeyPressed(ev -> {
            System.out.println("key: ("+ev.getClass().getCanonicalName()+")"+ev.getCode());
            if (KEY_CONTROL_C.match(ev) || KEY_ESC.match(ev)){
                System.out.println("exit");
                Platform.exit();
            }
        });

        stage.setScene(scene);
        stage.show();
    }

    private Control mkTable(){
        var table = new TableView<FwRecord>();

        var forwarderCol = new TableColumn<FwRecord,String>("Forwarder");
        var domainCol = new TableColumn<FwRecord,String>("Domain");
        var targetCol = new TableColumn<FwRecord,String>("Target");

        forwarderCol.setCellValueFactory(new PropertyValueFactory<FwRecord,String>("forwarder"));
        domainCol.setCellValueFactory(new PropertyValueFactory<FwRecord,String>("domain"));
        targetCol.setCellValueFactory(new PropertyValueFactory<FwRecord,String>("target"));

        FwModel model = FwModel.instance.get();
        System.out.println("loading model...");
        model.reload();
        System.out.println("model loaded");
        ObservableList<FwRecord> records = FXCollections.observableArrayList(model.getRecords());
        table.setItems(records);

        table.getColumns().addAll(forwarderCol,domainCol,targetCol);

        return table;
    }

    private void buttonPressed(ActionEvent aev){
        try{
            System.out.println("button pressed: "+aev+" "+Util.helper());
            System.out.println("Domains:");
            CPanelAPI.mkImpl().getDomains().stream().forEach(d -> System.out.println(d.name()));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}