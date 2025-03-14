package org.springdot.forpan.gui;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.springdot.forpan.config.ForpanConfig;
import org.springdot.forpan.cpanel.api.CPanelDomain;
import org.springdot.forpan.util.Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springdot.forpan.gui.Common.EMAIL_ADDRESS_PATTERN;

class AddRecWin{
    private Env env;
    private Stage primaryStage;
    private Memo memo;
    private Stage dialog;
    private FormAttr<ComboBox<CPanelDomain>> domainAttr;
    private FormAttr<TextField> forwarderAttr;
    private FormAttr<TextField> targetAttr;
    private Button okButton;

    public AddRecWin(Env env, Stage primaryStage){
        this.env = env;
        this.primaryStage = primaryStage;
        this.memo = Memo.load();
    }

    void show(){
        dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.setTitle("New Forwarder");

        var grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(8);

        // TODO: have a more generic "form of attribute" notion
        AtomicInteger row = new AtomicInteger();

        {
            var combo = new ComboBox<CPanelDomain>();
            domainAttr = new FormAttr(grid,"Domain",combo,row);
            combo.getItems().addAll(env.model.getDomains());
            preSelectDomain();
        }

        {
            forwarderAttr = new FormAttr<>(grid,"Forwarder",new TextField(),row)
                .setValidator(attr -> {
                    String fwdr = attr.field.getText()+"@"+domainAttr.field.getValue();
                    if (!EMAIL_ADDRESS_PATTERN.matcher(fwdr).matches()){
                        throw new AttrValidationErrorException("invalid forwarder username");
                    }
                    if (env.model.containsForwarder(fwdr)){
                        throw new AttrValidationErrorException("forwarder already exists");
                    }
                });
            String ip = ForpanConfig.getForwarderInitPattern();
            if (!StringUtils.isBlank(ip)){
                forwarderAttr.field.setText(new SimpleDateFormat(ip).format(new Date()));
            }
        }

        {
            targetAttr = new FormAttr<>(grid,"Target",new TextField(),row)
                .setValidator(attr -> {
                    String txt = attr.field.getText();
                    if (txt == null) txt = "";
                    if (!EMAIL_ADDRESS_PATTERN.matcher(txt).matches()){
                        throw new AttrValidationErrorException("invalid email address "+Util.escapeJava(txt));
                    }
                });
            targetAttr.field.setText(memo.target);
        }

        {
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
                    okButton = new Button("OK");
                    okButton.setBackground(new Background(new BackgroundFill(Color.GREEN,null,null)));
                    okButton.setOnAction(this::add);
                    c.add(okButton);
                }
            }

            grid.add(buttons,1,row.get());

            row.incrementAndGet();
        }

        new Form(domainAttr,forwarderAttr,targetAttr).setValidator(nofErrAttrs -> {
            okButton.setDisable(nofErrAttrs > 0);
        });

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

        forwarderAttr.field.requestFocus();
        dialog.show();
    }

    private void preSelectDomain(){
        ObservableList<CPanelDomain> items = domainAttr.field.getItems();
        for (int i=0, n=items.size(); i<n; i++){
            CPanelDomain cPanelDomain = items.get(i);
            if (StringUtils.equals(memo.domain,cPanelDomain.name())){
                domainAttr.field.getSelectionModel().select(i);
                return;
            }
        }
        domainAttr.field.getSelectionModel().selectFirst();
    }

    private void add(ActionEvent ev){
        if (okButton.isDisabled()) return;

        String fwdr = forwarderAttr.field.getText();
        CPanelDomain dmn = domainAttr.field.getValue();
        String trgt = targetAttr.field.getText();
        env.model.addForwarder(fwdr,dmn,trgt);
        memo.domain = dmn.name();
        memo.target = trgt;
        memo.save();
        dialog.close();
        env.mainWindow.refreshTable();

        // TODO: find a better way to select & navigate to the newly added forwarded
        env.mainWindow.gotoForwarderByName(fwdr+"@"+dmn);
    }
}
