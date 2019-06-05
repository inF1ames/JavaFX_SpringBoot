/**
 * Copyright TraderEvolution LTD. © 2018.. All rights reserved.
 */

package com.company.dxfeedapi;

import com.dxfeed.api.DXEndpoint;
import com.dxfeed.api.DXFeed;
import com.dxfeed.event.candle.Candle;
import com.dxfeed.event.candle.CandleAlignment;
import com.dxfeed.event.candle.CandleExchange;
import com.dxfeed.event.candle.CandlePeriod;
import com.dxfeed.event.candle.CandlePrice;
import com.dxfeed.event.candle.CandleSession;
import com.dxfeed.event.candle.CandleSymbol;
import com.dxfeed.event.candle.CandleType;
import com.company.StatefulContext;
import com.company.model.HistoryRequestModel;
import com.company.model.ProcessImportModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

@Component
public class DxFeedApi {

    private final StatefulContext context;

    private static final long MAX_CONNECT_TIME = 20000;
    private static final CandlePrice DEFAULT_CANDLE_PRICE = CandlePrice.LAST; // На данном этапе используем только значение LAST

    @Autowired
    public DxFeedApi(StatefulContext context) {
        this.context = context;
    }


    public static boolean checkCredentials(final String url, final String login, final String password, final int port) {
        final DXEndpoint dxEndpoint = connect(url, port, login, password);
        return dxEndpoint != null;
    }

    public void fetchCandles() {
        final DXEndpoint dxEndpoint = connect(context.getUrl(), Integer.parseInt(context.getPort()), context.getLogin(), context.getPassword());
        final HistoryRequestModel request = context.getRequestModels().get(0);
        final DXFeed feed = dxEndpoint.getFeed();
        final CandleSession candleSession = request.isOnlyRegularHours() ? CandleSession.REGULAR : CandleSession.ANY;
        final CandleAlignment candleAlignment = request.isAlignOnTradingSession() ? CandleAlignment.SESSION : CandleAlignment.MIDNIGHT;
        final Optional<Character> exchangeCode = getExchangeCode(request.getSymbol());
        final CandleExchange candleExchange = exchangeCode.isPresent() ? CandleExchange.valueOf(exchangeCode.get()) : CandleExchange.COMPOSITE;
        final CandleSymbol candleSymbol = CandleSymbol.valueOf(request.getSymbol(), getCandlePeriod(request.getPeriod()), candleExchange, candleSession, candleAlignment, DEFAULT_CANDLE_PRICE);
        List<Candle> candles = fetchCandles(feed, candleSymbol, request);
        context.getCandles().addAll(candles);
    }

    private List<Candle> fetchCandles(DXFeed feed, CandleSymbol candleSymbol, HistoryRequestModel request) {

        try {
            context.getImportInfo()
                    .add(new ProcessImportModel(getDateTimeAsString(), " Start Importing data. Instrument: " + request.getSymbol() + ";  Data period: " + request.getPeriod()));
            final List<Candle> candles = feed.getTimeSeriesPromise(Candle.class, candleSymbol, request.getFromDate(), request.getToDate()).await(10, TimeUnit.SECONDS);
            context.getImportInfo()
                    .add(new ProcessImportModel(getDateTimeAsString(), " Finish Importing data. Instrument: " + request.getSymbol() + ";  Data period: " + request.getPeriod() + "; Bars: " + candles.size()));
            return candles;
        } catch (CancellationException e) {
            context.getImportInfo()
                    .add(new ProcessImportModel(getDateTimeAsString(), " Empty response. Instrument: " + request.getSymbol() + ";  Data period: " + request.getPeriod()));
        }

        return Collections.emptyList();
    }

    private static DXEndpoint connect(final String url, final int port, final String login, final String password) {
        final DXEndpoint dxEndpoint = DXEndpoint.create(DXEndpoint.Role.ON_DEMAND_FEED).user(login).password(password).connect(url + ":" + port);
        final long start = System.currentTimeMillis();
        while (dxEndpoint.getState() == DXEndpoint.State.CONNECTING && System.currentTimeMillis() - start < MAX_CONNECT_TIME) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        if (dxEndpoint.getState() != DXEndpoint.State.CONNECTED) {
            return null;
        }
        return dxEndpoint;
    }

    private CandlePeriod getCandlePeriod(final String tradeInterval) {
        switch (tradeInterval) {
            case "Tick":
                return CandlePeriod.TICK;
            case "DAY":
                return CandlePeriod.DAY;
            case "MINUTE":
            default:
                return CandlePeriod.valueOf(1, CandleType.MINUTE);
        }
    }

    private Optional<Character> getExchangeCode(final String vendorInstrName) {
        if (vendorInstrName.contains("&")) {
            return Optional.of(vendorInstrName.split("&")[1].charAt(0));
        }
        return Optional.empty();
    }

    private String getDateTimeAsString() {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
        return format.format(new Date(System.currentTimeMillis()));
    }

}