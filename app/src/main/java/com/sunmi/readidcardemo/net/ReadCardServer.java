package com.sunmi.readidcardemo.net;

import android.support.annotation.NonNull;

import com.sunmi.readidcardemo.bean.ResultInfo;

import rx.Observable;

public class ReadCardServer {

    private static ReadCardServer instance;
    private final RetrofitInterface mNetService;

    private ReadCardServer() {
        RetrofitWrapper retrofitWrapper = RetrofitWrapper.getInstance();
        mNetService = retrofitWrapper.getNetService(RetrofitInterface.class);
    }

    public static ReadCardServer getInstance() {
        if (instance == null) {
            instance = new ReadCardServer();
        }
        return instance;
    }

    public Observable<ResultInfo> parse(@NonNull String reqId) {
        return mNetService.parse(reqId);
    }
}
