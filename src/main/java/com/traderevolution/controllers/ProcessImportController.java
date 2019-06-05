/**
 * Copyright TraderEvolution LTD. © 2018.. All rights reserved.
 */

package com.traderevolution.controllers;

import com.dxfeed.event.candle.Candle;
import com.traderevolution.Main;
import com.traderevolution.StatefulContext;
import com.traderevolution.dxfeedapi.DxFeedApi;
import com.traderevolution.model.ProcessImportModel;
import com.traderevolution.spring.config.SpringFXMLLoader;
import com.traderevolution.view.FxmlView;
import com.traderevolution.view.StageManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Lazy
@Scope("prototype")
public class ProcessImportController implements FxmlController {

    private final StageManager stageManager;

    @Autowired
    private SpringFXMLLoader loader;
    @Autowired
    private StatefulContext context;
    @Autowired
    private DxFeedApi api;

    @FXML
    private TableView<ProcessImportModel> table;
    @FXML
    private MenuButton menuButton;

    private static final String CSV_SEPARATOR = ",";

    @PostConstruct
    public void init() {
        Thread thread = new Thread(() -> api.fetchCandles());
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
        final MenuItem csv = new MenuItem("Export to CSV");
        csv.setOnAction(e -> {
            String fileName = "log_" + LocalDateTime.now() + ".csv";
            Path log = Paths.get(fileName);
            try {
                Files.write(log, context.getCandles().stream().map(this::toCsvLine).collect(Collectors.toList()), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        });
        final MenuItem table = new MenuItem("Show in table");
        table.setOnAction(e -> {
            Stage dialog = new Stage();
            final TextArea textArea = new TextArea();
            textArea.appendText(context.getCandles().stream().map(Candle::toString).collect(Collectors.joining("\n")));
            textArea.setPrefSize(600, 600);
            dialog.setScene(new Scene(textArea));
            dialog.initOwner(Main.mainStage);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.show();
        });
        menuButton.getItems().addAll(csv, table);

        TableColumn<ProcessImportModel, String> dateTimeColumn = new TableColumn<>("Date time");
        dateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("dateTime"));

        TableColumn<ProcessImportModel, String> eventColumn = new TableColumn<>("Event");
        eventColumn.setCellValueFactory(new PropertyValueFactory<>("event"));

        this.table.getColumns().setAll(dateTimeColumn, eventColumn);

        this.table.setItems(context.getImportInfo());
    }

    public void back(ActionEvent actionEvent) {
        context.getImportInfo().clear();
        context.getCandles().clear();
        stageManager.switchScene(FxmlView.MAPPING);
    }

    public void newWizard(ActionEvent actionEvent) {
        context.clearContext();
        stageManager.switchScene(FxmlView.LOGIN);
    }

    private String toCsvLine(final Candle candle) {
        return new StringBuilder()
                .append(candle.getTime())
                .append(CSV_SEPARATOR)
                .append(candle.getCount())
                .append(CSV_SEPARATOR)
                .append(candle.getOpen())
                .append(CSV_SEPARATOR)
                .append(candle.getHigh())
                .append(CSV_SEPARATOR)
                .append(candle.getLow())
                .append(CSV_SEPARATOR)
                .append(candle.getClose())
                .append(CSV_SEPARATOR)
                .append(candle.getVolume())
                .toString();
    }
}
