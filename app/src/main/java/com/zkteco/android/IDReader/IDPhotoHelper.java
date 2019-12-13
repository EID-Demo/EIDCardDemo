package com.zkteco.android.IDReader;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

public class IDPhotoHelper {
    public IDPhotoHelper() {
    }

    public static Bitmap Bgr2Bitmap(byte[] bgrbuf) {
        int width = WLTService.imgWidth;
        int height = WLTService.imgHeight;
        Bitmap bmp = Bitmap.createBitmap(width, height, Config.RGB_565);
        int row = 0;
        int col = width - 1;

        for (int i = bgrbuf.length - 1; i >= 3; i -= 3) {
            int color = bgrbuf[i] & 255;
            color += bgrbuf[i - 1] << 8 & '\uff00';
            color += bgrbuf[i - 2] << 16 & 16711680;
            bmp.setPixel(col--, row, color);
            if (col < 0) {
                col = width - 1;
                ++row;
            }
        }

        return bmp;
    }

    private static int parse(char c) {
        if (c >= 'a')
            return (c - 'a' + 10) & 0x0f;
        if (c >= 'A')
            return (c - 'A' + 10) & 0x0f;
        return (c - '0') & 0x0f;
    }

    public static byte[] hexStr2Bytes(String hexstr) {
        byte[] b = new byte[hexstr.length() / 2];
        int j = 0;
        for (int i = 0; i < b.length; i++) {
            char c0 = hexstr.charAt(j++);
            char c1 = hexstr.charAt(j++);
            b[i] = (byte) ((parse(c0) << 4) | parse(c1));
        }
        return b;
    }
}
