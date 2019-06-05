/**
 * Copyright TraderEvolution LTD. © 2018.. All rights reserved.
 */

package com.traderevolution.controllers;

import com.traderevolution.StatefulContext;
import com.traderevolution.dxfeedapi.DxFeedApi;
import com.traderevolution.model.ProcessImportModel;
import com.traderevolution.view.FxmlView;
import com.traderevolution.view.StageManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Lazy
@Scope("prototype")
public class ProcessImportController implements FxmlController {

    private final StageManager stageManager;

    @Autowired
    private StatefulContext context;

    @Autowired
    private DxFeedApi api;

    @FXML
    private TableView<ProcessImportModel> table;

    @PostConstruct
    public void init() {
        Thread thread = new Thread(() -> api.getInputStream());
        thread.setDaemon(true);
        thread.start();
    }

    @Autowired
    @Lazy
    public ProcessImportController(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    @Override
    public void initialize() {
        TableColumn<ProcessImportModel, String> dateTimeColumn = new TableColumn<>("Date time");
        dateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("dateTime"));

        TableColumn<ProcessImportModel, String> eventColumn = new TableColumn<>("Event");
        eventColumn.setCellValueFactory(new PropertyValueFactory<>("event"));

        table.getColumns().setAll(dateTimeColumn, eventColumn);

        table.setItems(context.getImportInfo());
    }

    public void back(ActionEvent actionEvent) {
        stageManager.switchScene(FxmlView.MAPPING);
    }

    public void next(ActionEvent actionEvent) {

    }
}
