package com.bozzat.esepkersoft.Controllers;

import com.bozzat.esepkersoft.Services.NavigationService;
import javafx.fxml.FXML;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class LoginPageController {
    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private Button loginButton;
    @FXML private Button newUserButton;
    @FXML private Label messageLabel;
    @FXML private GridPane mainPane;

    // Services
    NavigationService navigationService = new NavigationService();

    @FXML
    private void initialize() {
        // Set the action for the New User button to open the sign‑up page.
        newUserButton.setOnAction(event -> openNewUserPage());
    }

    private void openNewUserPage() {
       navigationService.navigateTo("/com/bozzat/esepkersoft/NewUserPage.fxml", mainPane.getScene());
    }
}
