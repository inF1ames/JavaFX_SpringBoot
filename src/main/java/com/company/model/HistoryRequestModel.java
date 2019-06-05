package com.company.model;

public class HistoryRequestModel {

    private String period;
    private String targetDataType;
    private String dateRange;
    private long fromDate;
    private long toDate;
    private boolean onlyRegularHours;
    private boolean alignOnTradingSession;
    private String symbol = "";

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getTargetDataType() {
        return targetDataType;
    }

    public void setTargetDataType(String targetDataType) {
        this.targetDataType = targetDataType;
    }

    public String getDateRange() {
        return dateRange;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    public long getFromDate() {
        return fromDate;
    }

    public void setFromDate(long fromDate) {
        this.fromDate = fromDate;
    }

    public long getToDate() {
        return toDate;
    }

    public void setToDate(long toDate) {
        this.toDate = toDate;
    }

    public boolean isOnlyRegularHours() {
        return onlyRegularHours;
    }

    public void setOnlyRegularHours(boolean onlyRegularHours) {
        this.onlyRegularHours = onlyRegularHours;
    }

    public boolean isAlignOnTradingSession() {
        return alignOnTradingSession;
    }

    public void setAlignOnTradingSession(boolean alignOnTradingSession) {
        this.alignOnTradingSession = alignOnTradingSession;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
