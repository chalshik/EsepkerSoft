package com.bozzat.esepkersoft.Services;

import com.bozzat.esepkersoft.Models.User;

import java.util.List;
import java.util.Map;

public class LoginService {
    private dbManager db = dbManager.getInstance();
    public void LoginService() {

    }

    public Boolean checkAndAddUser(User user) {
        String checkQuery = "SELECT * FROM users WHERE username = ?";
        List<Map<String, Object>> object = db.executeGet(checkQuery, user.getUsername());
        if (object.isEmpty()) {
            String insertQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
            db.executeSet(insertQuery, user.getUsername(), user.getPassword());
            return true;
        } else {
            return false;
        }
    }


}
