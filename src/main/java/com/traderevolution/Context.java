/**
 * Copyright TraderEvolution LTD. © 2018.. All rights reserved.
 */

package com.traderevolution;

import com.traderevolution.model.HistoryRequestModel;
import com.traderevolution.model.ProcessImportModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.stereotype.Component;

@Component
public class Context {

    private String login;
    private String password;
    private String url;
    private String port;
    private ObservableList<HistoryRequestModel> requestModels = FXCollections.observableArrayList();
    private ObservableList<ProcessImportModel> importInfo = FXCollections.observableArrayList();

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public ObservableList<HistoryRequestModel> getRequestModels() {
        return requestModels;
    }

    public ObservableList<ProcessImportModel> getImportInfo() {
        return importInfo;
    }
}
