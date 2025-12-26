package org.springdot.forpan.gui;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class App extends Application {
    private static final Logger LOG = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        configureLogging();
        launch();
    }

    private static void configureLogging(){
        try (var in = App.class.getResourceAsStream("/logging.properties")) {
            if (in == null) {
                throw new IllegalStateException("logging.properties not found on classpath");
            }
            LogManager.getLogManager().readConfiguration(in);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start(Stage stage) {
        LOG.info("app starting");

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