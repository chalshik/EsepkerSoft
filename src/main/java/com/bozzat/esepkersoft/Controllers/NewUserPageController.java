package com.bozzat.esepkersoft.Controllers;

import com.bozzat.esepkersoft.Services.NavigationService;
import com.bozzat.esepkersoft.ViewModel.RegistrationViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javafx.event.*;

public class NewUserPageController {
    // Models

    RegistrationViewModel registrationViewModel = new RegistrationViewModel();
    NavigationService navigationService = new NavigationService();

    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private TextField confirmPasswordField;
    @FXML private Button signUpButton;
    @FXML private Button backToLoginButton;
    @FXML private Label messageLabel;


    @FXML
    public void initialize() {
        initializeBindings();
        setButtonActions();
    }

    public void initializeBindings() {
        usernameField.textProperty().bindBidirectional(registrationViewModel.usernameProperty());
        passwordField.textProperty().bindBidirectional(registrationViewModel.passwordProperty());
        confirmPasswordField.textProperty().bindBidirectional(registrationViewModel.passwordProperty());
    }

    public void setButtonActions() {
        signUpButton.setOnAction(event -> {
            checkAndAddUser(event);
        });
    }

    public void checkAndAddUser(ActionEvent event) {
        if (registrationViewModel.checkAndAddUser()) {
            backToLogin(event);
        } else {
            duplicateUser();
        }
    }

    public void duplicateUser() {

    }

    public void backToLogin(ActionEvent event) {
        navigationService.navigateTo("/com/bozzat/esepkersoft/LoginPage.fxml", event);
    }
}
