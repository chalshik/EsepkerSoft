package com.bozzat.esepkersoft.ViewModel;

import com.bozzat.esepkersoft.Models.User;
import com.bozzat.esepkersoft.Services.LoginService;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.*;

public class RegistrationViewModel {
    private StringProperty username = new SimpleStringProperty();
    private StringProperty password = new SimpleStringProperty();
    private StringProperty confirmPassword = new SimpleStringProperty();
    // services
    private LoginService loginService = new LoginService();

    public Boolean checkAndAddUser() {
        if (checkConfirmPassword()) {
            User testUser = new User(username.get(), password.get());
            return loginService.checkAndAddUser(testUser);
        } else {
            passwordsDoNotMatch();
            return false;
        }
    }

    public void passwordsDoNotMatch() {

    }

    private Boolean checkConfirmPassword() {
        if (confirmPassword.equals(password)) {
            return true;
        } else {
            return false;
        }
    }
    // all that is below that is not important I hope
    public String getPassword() {
        return password.get();
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public String getUsername() {
        return username.get();
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public void setUsername(String username) {
        this.username.set(username);
    }
}
