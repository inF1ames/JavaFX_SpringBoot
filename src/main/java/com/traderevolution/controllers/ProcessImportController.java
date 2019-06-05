/**
 * Copyright TraderEvolution LTD. © 2018.. All rights reserved.
 */

package com.traderevolution.controllers;

import com.traderevolution.Context;
import com.traderevolution.dxfeedapi.DxFeedApi;
import com.traderevolution.model.ProcessImportModel;
import com.traderevolution.view.StageManager;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Lazy
public class ProcessImportController implements FxmlController {

    private final StageManager stageManager;

    @Autowired
    private Context context;

    @Autowired
    private DxFeedApi api;

    @FXML
    private TableView<ProcessImportModel> table;

    @Autowired
    @Lazy
    public ProcessImportController(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    @PostConstruct
    public void init() {
        api.getInputStream();
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
}
