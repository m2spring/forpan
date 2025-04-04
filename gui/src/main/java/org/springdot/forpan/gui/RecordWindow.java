package org.springdot.forpan.gui;

import atlantafx.base.controls.CustomTextField;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springdot.forpan.config.ForpanConfig;
import org.springdot.forpan.cpanel.api.CPanelDomain;
import org.springdot.forpan.model.FwRecord;
import org.springdot.forpan.util.Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import static javafx.scene.input.KeyEvent.KEY_PRESSED;
import static org.springdot.forpan.gui.Common.EMAIL_ADDRESS_PATTERN;

class RecordWindow{
    private Env env;
    private Stage primaryStage;
    private Memo memo;
    private Stage dialog;
    private FormAttr<TextField> titleAttr;
    private FormAttr<ComboBox<CPanelDomain>> domainAttr;
    private FormAttr<CustomTextField> forwarderAttr;
    private FormAttr<TextField> targetAttr;
    private Button okButton;

    public RecordWindow(Env env, Stage primaryStage){
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
            titleAttr = new FormAttr<>(grid,"Title",new TextField(),row);
        }

        {
            var combo = new ComboBox<CPanelDomain>();
            domainAttr = new FormAttr(grid,"Domain",combo,row);
            combo.getItems().addAll(env.model.getDomains());
            preSelectDomain();
        }

        {
            CustomTextField forwarderTextField = new CustomTextField();
            {
                FontIcon icon = new FontIcon();
                icon.setIconLiteral("fas-feather");
                forwarderTextField.setRight(icon);
                Tooltip.install(icon,new Tooltip("Generate Random (Ctrl-G)"));
                icon.setOnMouseEntered(e -> icon.setCursor(Cursor.HAND));
                icon.setOnMouseExited(e -> icon.setCursor(Cursor.DEFAULT));
                icon.setOnMouseClicked(e -> generateRandomForwarder());
                forwarderTextField.addEventFilter(KEY_PRESSED,e -> {
                    if (Common.KEY_CONTROL_G.match(e)) generateRandomForwarder();
                });
            }

            forwarderAttr = new FormAttr<>(grid,"Forwarder",forwarderTextField,row)
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
                forwarderAttr.field.setText(new StringSubstitutor(key -> {
                    if ("R".equals(key)) return env.model.generateRandomForwarder();
                    if (key.startsWith("T:")) return new SimpleDateFormat(key.substring(2)).format(new Date());
                    return "??"+key+"??";
                }).replace(ip));
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

        new Form(titleAttr,domainAttr,forwarderAttr,targetAttr).setValidator(nofErrAttrs -> {
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

        titleAttr.field.requestFocus();
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

    private void generateRandomForwarder(){
        forwarderAttr.field.setText(env.model.generateRandomForwarder());
    }

    private void add(ActionEvent ev){
        if (okButton.isDisabled()) return;

        CPanelDomain domain = domainAttr.field.getValue();
        String forwarder = forwarderAttr.field.getText()+"@"+domain;
        String target = targetAttr.field.getText();

        FwRecord rec = new FwRecord();
        rec.setForwarder(forwarder);
        rec.setTarget(target);
        rec.setTitle(titleAttr.field.getText());

        env.model.addForwarder(rec);
        env.model.createForwarder(rec);

        memo.domain = domain.name();
        memo.target = target;
        memo.save();
        dialog.close();
        env.mainWindow.refreshTable();

        // TODO: find a better way to select & navigate to the newly added forwarded
        env.mainWindow.gotoForwarderByName(forwarder);
    }
}
