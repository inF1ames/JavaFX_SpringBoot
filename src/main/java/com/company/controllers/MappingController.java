/**
 * Copyright TraderEvolution LTD. © 2018.. All rights reserved.
 */

package com.company.controllers;

import com.company.StatefulContext;
import com.company.dxfeedapi.DxFeedApi;
import com.company.view.FxmlView;
import com.company.view.StageManager;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@Lazy
@Scope("prototype")
public class MappingController implements FxmlController {

    private final StageManager stageManager;

    @Autowired
    private StatefulContext context;
    @Autowired
    private DxFeedApi api;

    @FXML
    private CheckBox onlyRegularHours;
    @FXML
    private CheckBox alignOnTradingSession;
    @FXML
    private TextField instrumentName;

    @Autowired
    @Lazy
    public MappingController(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    @Override
    public void initialize() {
        onlyRegularHours.setSelected(context.getRequestModels().get(0).isOnlyRegularHours());
        alignOnTradingSession.setSelected(context.getRequestModels().get(0).isAlignOnTradingSession());
        instrumentName.setText(context.getRequestModels().get(0).getSymbol());
        setUpValidation(instrumentName);
    }

    @FXML
    public void back(ActionEvent event) {
        stageManager.switchScene(FxmlView.DATA_TYPE_SETTINGS);
    }

    @FXML
    public void next(ActionEvent event) {
        if (instrumentName.getText().isEmpty()) {
            validate(instrumentName);
            return;
        }
        fillRequest();
        stageManager.switchScene(FxmlView.PROCESS_IMPORT);
    }

    private void fillRequest() {
        context.getRequestModels().forEach(request -> {
            request.setOnlyRegularHours(onlyRegularHours.isSelected());
            request.setAlignOnTradingSession(alignOnTradingSession.isSelected());
            request.setSymbol(instrumentName.getText());
        });
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
