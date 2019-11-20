package com.sunmi.readidcardemo.bean;

import java.io.Serializable;

public class BaseInfo implements Serializable {

    /**
     * 证件类型
     * 01 身份证
     */
    public String idType;
    public String classify;
    /**
     * 姓名
     */
    public String name;
    /**
     * 性别
     */
    public String sex;
    /**
     * 民族
     */
    public String nation;
    /**
     * 出生年月日
     */
    public String birthDate;
    /**
     * 身份证住址
     */
    public String address;
    /**
     * 身份证号码
     */
    public String idnum;
    /**
     * 签发机关
     */
    public String signingOrganization;
    /**
     * 身份证有效期限开始时间
     */
    public String beginTime;
    /**
     * 身份证有效期限结束时间
     */
    public String endTime;

    @Override
    public String toString() {
        return "BaseInfo{" +
                "idType='" + idType + '\'' +
                ", classify='" + classify + '\'' +
                ", name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", nation='" + nation + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", address='" + address + '\'' +
                ", idnum='" + idnum + '\'' +
                ", signingOrganization='" + signingOrganization + '\'' +
                ", beginTime='" + beginTime + '\'' +
                ", endTime='" + endTime + '\'' +
                '}';
    }
}
