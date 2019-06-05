package com.traderevolution.controllers;

import com.traderevolution.StatefulContext;
import com.traderevolution.model.HistoryRequestModel;
import com.traderevolution.util.Period;
import com.traderevolution.view.FxmlView;
import com.traderevolution.view.StageManager;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Paint;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import tornadofx.control.DateTimePicker;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;


@Component
@Scope("prototype")
@Lazy
public class SettingsController implements FxmlController {

    private final StageManager stageManager;

    @Autowired
    private StatefulContext context;

    @FXML
    private TableView<HistoryRequestModel> table;
    @FXML
    private DateTimePicker dateFrom;
    @FXML
    private DateTimePicker dateTo;
    @FXML
    private ComboBox<String> period;


    private ObservableList<HistoryRequestModel> data = FXCollections.observableArrayList();
    private final String DEFAULT_TARGET_DATA_TYPE = "Trade";

    @Autowired
    @Lazy
    public SettingsController(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    @Override
    public void initialize() {
        setUpValidation(dateFrom);
        setUpValidation(dateTo);
        period.setItems(FXCollections.observableArrayList(Arrays.stream(Period.values()).map(Enum::name).collect(Collectors.toList())));
        period.getSelectionModel().selectFirst();

        TableColumn<HistoryRequestModel, String> periodColumn = new TableColumn<>("Vendor data Period");
        periodColumn.setCellValueFactory(new PropertyValueFactory<>("period"));

        TableColumn<HistoryRequestModel, String> targetDataType = new TableColumn<>("Target data type");
        targetDataType.setCellValueFactory(new PropertyValueFactory<>("targetDataType"));

        TableColumn<HistoryRequestModel, String> dateRangeColumn = new TableColumn<>("History depth");
        dateRangeColumn.setCellValueFactory(new PropertyValueFactory<>("dateRange"));

        TableColumn<HistoryRequestModel, HistoryRequestModel> remove = new TableColumn<>("Remove");
        remove.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        remove.setCellFactory(param -> new TableCell<HistoryRequestModel, HistoryRequestModel>() {
            private final Button deleteButton = new Button("delete");

            @Override
            protected void updateItem(HistoryRequestModel item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setGraphic(null);
                    return;
                }
                deleteButton.setStyle("-fx-background-color: #666666");
                deleteButton.setTextFill(Paint.valueOf("#f5efef"));
                setAlignment(Pos.CENTER);
                setGraphic(deleteButton);
                deleteButton.setOnAction(event -> {
                    getTableView().getItems().remove(item);
                    context.getRequestModels().remove(item);
                });
            }
        });

        table.getColumns().setAll(periodColumn, targetDataType, dateRangeColumn, remove);

        table.setItems(context.getRequestModels());
    }

    @FXML
    public void add(ActionEvent event) throws IOException {
        if (dateFrom.getValue() == null || dateTo.getValue() == null) {
            validate(dateFrom);
            validate(dateTo);
            return;
        }
        final HistoryRequestModel historyRequestModel = fillRequest();
        context.getRequestModels().add(historyRequestModel);
    }

    @FXML
    public void back(ActionEvent event) {
        stageManager.switchScene(FxmlView.LOGIN);
    }

    @FXML
    public void next(ActionEvent event) {
        stageManager.switchScene(FxmlView.MAPPING);
    }


    private HistoryRequestModel fillRequest() {
        final HistoryRequestModel model = new HistoryRequestModel();
        model.setFromDate(getMillis(dateFrom.getDateTimeValue()));
        model.setToDate(getMillis(dateTo.getDateTimeValue()));
        model.setPeriod(period.getValue());
        model.setTargetDataType(DEFAULT_TARGET_DATA_TYPE);
        model.setDateRange(dateFrom.getValue() + " / " + dateTo.getValue());
        return model;
    }

    private long getMillis(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private void setUpValidation(final DateTimePicker dtp) {
        dtp.focusedProperty().addListener((observable, oldValue, newValue) -> validate(dtp));
    }

    private void validate(DateTimePicker dtp) {
        ObservableList<String> styleClass = dtp.getStyleClass();
        if (dtp.getValue() == null) {
            if (!styleClass.contains("error")) {
                styleClass.add("error");
            }
        } else {
            styleClass.removeAll(Collections.singleton("error"));
        }
    }

}
