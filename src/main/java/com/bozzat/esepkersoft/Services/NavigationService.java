package com.bozzat.esepkersoft.Services;
import com.bozzat.esepkersoft.Interfaces.ViewModelAware;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;

import javax.swing.text.View;
import java.io.IOException;

public class NavigationService {
    public void navigateTo(String fxmlPath, Scene currentScene)  {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newPageRoot = loader.load();
            currentScene.setRoot(newPageRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void navigateTo(String fxmlPath, Scene currentScene, Object viewModel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newPageRoot = loader.load();
            currentScene.setRoot(newPageRoot);
            ViewModelAware<Object> currentPageController = loader.getController();
            currentPageController.setViewModel(viewModel);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newPageRoot = loader.load();
            getSceneFromEvent(event).setRoot(newPageRoot);
        } catch (IOException exception) {

        }
    }

    public void navigateTo(String fxmlPath, ActionEvent event, Object viewModel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newPageRoot = loader.load();
            getSceneFromEvent(event).setRoot(newPageRoot);
            ViewModelAware currentController = loader.getController();
            currentController.setViewModel(viewModel);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
    private Scene getSceneFromEvent(ActionEvent event) {
        Node source = (Node) event.getSource();
        return source.getScene();
    }
}
