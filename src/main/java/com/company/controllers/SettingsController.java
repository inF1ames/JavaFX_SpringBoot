package com.company.controllers;

import com.company.StatefulContext;
import com.company.model.HistoryRequestModel;
import com.company.util.Period;
import com.company.view.FxmlView;
import com.company.view.StageManager;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Paint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import tornadofx.control.DateTimePicker;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    @FXML
    private Button addButton;


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
                    addButton.setDisable(false);
                });
            }
        });

        table.getColumns().setAll(periodColumn, targetDataType, dateRangeColumn, remove);

        table.setItems(context.getRequestModels());
    }

    @FXML
    public void add(ActionEvent event) throws IOException {
        if (!validateRange()) {
            return;
        }
        final HistoryRequestModel historyRequestModel = fillRequest();
        context.getRequestModels().add(historyRequestModel);
        addButton.setDisable(true);
    }

    @FXML
    public void back(ActionEvent event) {
        context.clearContext();
        stageManager.switchScene(FxmlView.LOGIN);
    }

    @FXML
    public void next(ActionEvent event) {
        if (context.getRequestModels().isEmpty()) {
            return;
        }
        stageManager.switchScene(FxmlView.MAPPING);
    }


    private HistoryRequestModel fillRequest() {
        final HistoryRequestModel model = new HistoryRequestModel();
        model.setFromDate(getMillis(dateFrom.getDateTimeValue()));
        model.setToDate(getMillis(dateTo.getDateTimeValue()));
        model.setPeriod(period.getValue());
        model.setTargetDataType(DEFAULT_TARGET_DATA_TYPE);
        model.setDateRange(formatLocalDate(dateFrom.getDateTimeValue()) + " / " + formatLocalDate(dateTo.getDateTimeValue()));
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

    private boolean validateRange() {
        LocalDate from = dateFrom.getValue();
        LocalDate to = dateTo.getValue();
        if (from != null && to != null) {
            if (from.isAfter(to)) {
                ObservableList<String> fromStyleClass = dateFrom.getStyleClass();
                if (!fromStyleClass.contains("error")) {
                    fromStyleClass.add("error");
                }
                return false;
            }
            if (to.isAfter(LocalDate.now())) {
                ObservableList<String> toStyleClass = dateTo.getStyleClass();
                if (!toStyleClass.contains("error")) {
                    toStyleClass.add("error");
                }
                return false;
            }
            return true;
        }
        return false;
    }

    private String formatLocalDate(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return date.format(formatter);
    }

}
