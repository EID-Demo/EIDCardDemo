package com.zkteco.android.IDReader;

import android.graphics.Bitmap;

public class IDCardPhoto {
    public IDCardPhoto() {
    }

    public static Bitmap getIDCardPhoto(byte[] encryptedPhotoInfo) {
        byte[] buf = new byte[WLTService.imgLength];
        return 1 == WLTService.wlt2Bmp(encryptedPhotoInfo, buf) ? IDPhotoHelper.Bgr2Bitmap(buf) : null;
    }


}
