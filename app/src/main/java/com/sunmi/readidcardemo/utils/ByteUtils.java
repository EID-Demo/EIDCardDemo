package com.sunmi.readidcardemo.utils;

public class ByteUtils {
    /**
     * 将无符号short转换成int，大端模式(高位在前)
     */
    public static int unsignedShort2IntBE(byte[] src, int offset) {
        return (src[offset] & 0xff) << 8 | (src[offset + 1] & 0xff);
    }
}
