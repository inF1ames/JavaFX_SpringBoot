/**
 * Copyright TraderEvolution LTD. © 2018.. All rights reserved.
 */

package com.traderevolution.dxfeedapi;

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
import com.traderevolution.Context;
import com.traderevolution.model.HistoryRequestModel;
import com.traderevolution.model.ProcessImportModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class DxFeedApi {

    private final Context context;

    private static final long MAX_CONNECT_TIME = 20000;
    private static final CandlePrice DEFAULT_CANDLE_PRICE = CandlePrice.LAST; // На данном этапе используем только значение LAST
    private static final String CSV_SEPARATOR = ",";

    @Autowired
    public DxFeedApi(Context context) {
        this.context = context;
    }


    public static boolean checkCredentials(final String url, final String login, final String password, final int port) {
        final DXEndpoint dxEndpoint = connect(url, port, login, password);
        return dxEndpoint != null;
    }

    public InputStream getInputStream() {
        final DXEndpoint dxEndpoint = connect(context.getUrl(), Integer.parseInt(context.getPort()), context.getLogin(), context.getPassword());
        final HistoryRequestModel request = context.getRequestModels().get(0);
        final DXFeed feed = dxEndpoint.getFeed();
        final long from = request.getFromDate();
        final long to = request.getToDate();
        final CandleSession candleSession = request.isOnlyRegularHours() ? CandleSession.REGULAR : CandleSession.ANY;
        final CandleAlignment candleAlignment = request.isAlignOnTradingSession() ? CandleAlignment.SESSION : CandleAlignment.MIDNIGHT;
        final Optional<Character> exchangeCode = getExchangeCode(request.getSymbol());
        final CandleExchange candleExchange = exchangeCode.isPresent() ? CandleExchange.valueOf(exchangeCode.get()) : CandleExchange.COMPOSITE;
        final CandleSymbol candleSymbol = CandleSymbol.valueOf(request.getSymbol(), getCandlePeriod(request.getPeriod()), candleExchange, candleSession, candleAlignment, DEFAULT_CANDLE_PRICE);
        context.getImportInfo().add(new ProcessImportModel(getDateTimeAsString(), "Start Importing data. Instrument: " + request.getSymbol() + " Data period: " + request.getPeriod()));
        final List<Candle> candles = feed.getTimeSeriesPromise(Candle.class, candleSymbol, from, to).await(10, TimeUnit.SECONDS);
        context.getImportInfo().add(new ProcessImportModel(getDateTimeAsString(), "Finish Importing data. Instrument: " + request.getSymbol() + " Data period: " + request.getPeriod()));
        final String csvLines = candles.stream().map(this::toCsvLine).collect(Collectors.joining());
        System.out.println(csvLines);
        return new ByteArrayInputStream(csvLines.getBytes(StandardCharsets.UTF_8));
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
                .append(System.lineSeparator())
                .toString();
    }

    private String getDateTimeAsString() {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
        return format.format(new Date(System.currentTimeMillis()));
    }

}