package com.sunmi.readidcardemo.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * @author Darren(Zeng Dongyang)
 * @date 2019-12-13
 */
public class Utils {
    /**
     * check the app is installed
     */
    public static boolean isAppInstalled(Context context, String packageName) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        if (packageInfo == null) {
            // 没有安装
            return false;
        } else {
            // 已经安装
            return true;
        }
    }
}
