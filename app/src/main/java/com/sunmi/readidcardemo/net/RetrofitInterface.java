package com.sunmi.readidcardemo.net;

import com.sunmi.readidcardemo.bean.ResultInfo;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;


public interface RetrofitInterface {

    @SuppressWarnings("rawtypes")
    @FormUrlEncoded
    @POST("eid/decode")
    Observable<ResultInfo> parse(@Field("request_id") String reqId);
}
