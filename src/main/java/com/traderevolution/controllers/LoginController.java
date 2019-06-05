package com.traderevolution.controllers;

import com.traderevolution.Main;
import com.traderevolution.dxfeedapi.DxFeedApi;
import com.traderevolution.StatefulContext;
import com.traderevolution.view.FxmlView;
import com.traderevolution.view.StageManager;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class LoginController implements FxmlController {

    @Autowired
    private StatefulContext context;

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
        setUpValidation(urlField);
        setUpValidation(portField);
        portField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                portField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    @FXML
    public void login(ActionEvent event) {
        if (urlField.getText().isEmpty() || portField.getText().isEmpty()) {
            validate(urlField);
            validate(portField);
            return;
        }
        Thread thread = new Thread(() -> {
            Platform.runLater(() -> {
                loginButton.setDisable(true);
                Main.mainStage.getScene().setCursor(Cursor.WAIT);
            });
            if (DxFeedApi.checkCredentials(urlField.getText(), userField.getText(), passwordField.getText(), Integer.parseInt(portField.getText()))) {
                context.setLogin(userField.getText());
                context.setPassword(passwordField.getText());
                context.setUrl(urlField.getText());
                context.setPort(portField.getText());
                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    stageManager.switchScene(FxmlView.DATA_TYPE_SETTINGS);
                    Main.mainStage.getScene().setCursor(Cursor.DEFAULT);
                });
            } else {
                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    Main.mainStage.getScene().setCursor(Cursor.DEFAULT);
                    clearCredentials();
                    statusLabel.setText(LOGIN_ERROR_MSG);
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void clearCredentials() {
        userField.clear();
        passwordField.clear();
    }

    private void setUpValidation(final TextField tf) {
        tf.focusedProperty().addListener((observable, oldValue, newValue) -> validate(tf));
    }

    private void validate(TextField tf) {
        ObservableList<String> styleClass = tf.getStyleClass();
        if (Strings.isEmpty(tf.getText())) {
            if (!styleClass.contains("error")) {
                styleClass.add("error");
            }
        } else {
            styleClass.removeAll(Collections.singleton("error"));
        }
    }

}
