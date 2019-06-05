/**
 * Copyright TraderEvolution LTD. © 2018.. All rights reserved.
 */

package com.company.model;

public class ProcessImportModel {

    private String dateTime;
    private String event;

    public ProcessImportModel(String dateTime, String event) {
        this.dateTime = dateTime;
        this.event = event;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}
