package com.sunmi.readidcardemo.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 网络接口包装类
 */
public class RetrofitWrapper {
    private static RetrofitWrapper instance;
    private static final int TIMEOUT = 15;

    private RetrofitWrapper() {
    }

    public static RetrofitWrapper getInstance() {
        if (instance == null) {
            instance = new RetrofitWrapper();
        }
        return instance;
    }

    public <T> T getNetService(Class<T> clazz) {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .serializeNulls()
                .create();
        Interceptor interceptor = chain -> {
            Request request = chain.request()
                    .newBuilder()
                    .addHeader("userAgent", "sunmi.com ")
                    .build();
            return chain.proceed(request);
        };

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .addInterceptor(interceptor)
                // http 请求 log
                //.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("请替换成自己的demo服务器地址")// TODO 请替换成自己的demo服务器地址
                .client(client)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return retrofit.create(clazz);
    }

}


