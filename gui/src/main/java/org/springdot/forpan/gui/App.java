package org.springdot.forpan.gui;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class App extends Application {
    private static final Logger LOG = Logger.getLogger(App.class.getName());

    private static SingleInstanceGuard instanceGuard;

    public static void main(String[] args) {
        configureLogging();

        instanceGuard = SingleInstanceGuard.acquire();
        if (instanceGuard == null){
            LOG.info("another instance is already running, exiting");
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(instanceGuard::close));

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

        instanceGuard.listenForFocusRequests(() -> Platform.runLater(() -> {
            stage.setIconified(false);
            stage.toFront();
            stage.requestFocus();
        }));

        new Thread(() -> {
            w.refreshTable(model -> model.load());
            w.refreshTable();
        }).start();

        new Thread(w::loadMailboxRanks).start();
    }
}