/**
 * Copyright TraderEvolution LTD. © 2018.. All rights reserved.
 */

package com.traderevolution.controllers;

import com.traderevolution.Context;
import com.traderevolution.view.FxmlView;
import com.traderevolution.view.StageManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class MappingController implements FxmlController {

    private final StageManager stageManager;

    @Autowired
    private Context context;

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

    }

    @FXML
    public void back(ActionEvent event) {
        stageManager.switchScene(FxmlView.DATA_TYPE_SETTINGS);
    }

    @FXML
    public void next(ActionEvent event) {
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

}
