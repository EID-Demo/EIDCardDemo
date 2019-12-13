package com.zkteco.android.IDReader;

/**
集成时
native 方法路径包名，不可更改   com.zkteco.android.IDReader
 */
public class WLTService {
	public static int imgWidth;
    public static int imgHeight;
    public static int imgLength;
	public WLTService() {
    }

    public static native int wlt2Bmp(byte[] var0, byte[] var1);

    static {
        System.loadLibrary("zkwltdecode");
        imgWidth = 102;
        imgHeight = 126;
        imgLength = 38556;
    }
    
    
}
