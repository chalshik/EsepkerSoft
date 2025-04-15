package com.bozzat.esepkersoft;

import com.bozzat.esepkersoft.Services.dbManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/bozzat/esepkersoft/SalePoint.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("FXML tes1 Application");

            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        dbManager db = dbManager.getInstance();
        launch();
    }
}