package com.traderevolution.controllers;

import com.traderevolution.dxfeedapi.DxFeedApi;
import com.traderevolution.Context;
import com.traderevolution.view.FxmlView;
import com.traderevolution.view.StageManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component 
public class LoginController implements FxmlController {

    @Autowired
    private Context context;

    @FXML
    private TextField urlField;
    @FXML
    private TextField portField;
    @FXML
    private TextField userField;
    @FXML
    private TextField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Label statusLabel;

    private static final String LOGIN_ERROR_MSG = "Invalid username and/or password provided";
    private final StageManager stageManager;

    @Autowired
    @Lazy
    public LoginController(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    @Override
    public void initialize() {
    } 

    @FXML
    public void loginButtonPressed(ActionEvent event) {
        if (DxFeedApi.checkCredentials(urlField.getText(), userField.getText(), passwordField.getText(), Integer.parseInt(portField.getText()))) {
            context.setLogin(userField.getText());
            context.setPassword(passwordField.getText());
            context.setUrl(urlField.getText());
            context.setPort(portField.getText());
            stageManager.switchScene(FxmlView.DATA_TYPE_SETTINGS);
        } else {
            clearCredentials();
            statusLabel.setText(LOGIN_ERROR_MSG);
        }
    }

    private void clearCredentials() {
        userField.clear();
        passwordField.clear();
    }

}
