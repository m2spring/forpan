package org.springdot.forpan.gui;

import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.springdot.forpan.cpanel.api.CPanelDomain;

class AddRecWin{
    private Env env;
    private Stage primaryStage;
    private Stage dialog;
    private ComboBox<CPanelDomain> domainSelector;
    private TextField forwarderField;
    private TextField targetField;

    public AddRecWin(Env env, Stage primaryStage){
        this.env = env;
        this.primaryStage = primaryStage;
    }

    void show(){
        dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.setTitle("New Forwarder");

        var grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(8);
        grid.setVgap(8);

        {
            var lbl = new Label("Domain:");
            GridPane.setHalignment(lbl,HPos.RIGHT);
            grid.add(lbl,0,0);
        }
        domainSelector = new ComboBox<>();
        domainSelector.getItems().addAll(env.model.getDomains());
        domainSelector.getSelectionModel().selectFirst();
        grid.add(domainSelector,1,0);

        {
            Label lbl = new Label("Forwarder:");
            GridPane.setHalignment(lbl,HPos.RIGHT);
            grid.add(lbl,0,1);
        }
        forwarderField = new TextField();
        grid.add(forwarderField,1,1);

        {
            Label lbl = new Label("Target:");
            GridPane.setHalignment(lbl,HPos.RIGHT);
            grid.add(lbl,0,2);
        }
        targetField = new TextField();
        grid.add(targetField,1,2);

        var buttons = new HBox();
        buttons.setSpacing(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        {
            var c = buttons.getChildren();
            {
                var b = new Button("Cancel");
                b.setOnAction(ev -> dialog.close());
                c.add(b);
            }
            {
                Button b = new Button("OK");
                b.setBackground(new Background(new BackgroundFill(Color.GREEN,null,null)));
                b.setOnAction(this::add);
                c.add(b);
            }
        }

        grid.add(buttons,1,3);

        var bp = new BorderPane();
        bp.setCenter(grid);
        bp.setPadding(new Insets(10));

        var scene = new Scene(bp);
        dialog.setScene(scene);
        scene.setOnKeyPressed(ev -> {
            if (Common.KEY_ESC.match(ev)){
                dialog.close();
            }else if (Common.KEY_ENTER.match(ev)){
                add(null);
            }
        });

        var x = primaryStage.getX() + primaryStage.getWidth()/2d;
        var y = primaryStage.getY() + primaryStage.getHeight()/2d;

// https://stackoverflow.com/questions/40104688/javafx-center-child-stage-to-parent-stage/40105390#40105390
// If I use this as recommended, the dialog.close() does not work
//        dialog.setOnShowing(ev -> dialog.hide());
        dialog.setOnShown(ev -> {
            dialog.setX(x - dialog.getWidth()/2d);
            dialog.setY(y - dialog.getHeight()/2d);
        });

        dialog.show();
    }

    private void add(ActionEvent ev){
        String fwdr = forwarderField.getText();
        CPanelDomain dmn = domainSelector.getValue();
        env.model.addForwarder(fwdr,dmn,targetField.getText());
        dialog.close();
        env.mainWindow.refreshTable();

        // TODO: find a better way to select & navigate to the newly added forwarded
        env.mainWindow.gotoForwarderByName(fwdr+"@"+dmn);
    }
}
