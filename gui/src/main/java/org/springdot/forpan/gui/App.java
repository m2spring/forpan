package org.springdot.forpan.gui;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        Env env = new Env();
        MainWindow w = new MainWindow(env,stage);
        env.mainWindow = w;
        w.show();

        new Thread(() -> {
            w.refreshTable(model -> model.load());
            w.refreshTable();
        }).start();
    }
}