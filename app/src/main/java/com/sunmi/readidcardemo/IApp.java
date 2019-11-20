package com.sunmi.readidcardemo;

import android.app.Application;
import android.util.Log;

import com.sunmi.eidlibrary.EidCall;
import com.sunmi.eidlibrary.EidSDK;

/**
 * @author Darren(Zeng Dongyang)
 * @date 2019-10-11
 */
public class IApp extends Application implements EidCall {
    // TODO:  请替换应用appId
    private static final String EID_APP_ID = "请替换appId";

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            EidSDK.init(this, EID_APP_ID, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        EidSDK.destroy();
    }

    @Override
    public void onCallData(int code, String msg) {
        switch (code) {
            case 1://初始化完成
            case 5001://请检查套餐
            case 4001://appId参数错误/未初始化
            case 4002://解析域名异常
            case 4003://网络连接异常
            default:
                Log.d("app", "onCallData: code:" + code + ", msg:" + msg);
                break;
        }
    }
}
