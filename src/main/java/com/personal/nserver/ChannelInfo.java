package com.personal.nserver;

import io.netty.channel.ChannelHandlerContext;
public class ChannelInfo {

    private ChannelHandlerContext ctx;//传输通道context
    private String host;//Ip地址
    private String phoneModel;//手机型号
    private String serialNumber;//序列号
    private String jobNember;//工号

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public String getHost() {
        return host == null ? "" : host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPhoneModel() {
        return phoneModel == null ? "" : phoneModel;
    }

    public void setPhoneModel(String phoneModel) {
        this.phoneModel = phoneModel;
    }

    public String getSerialNumber() {
        return serialNumber == null ? "" : serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getJobNember() {
        return jobNember == null ? "" : jobNember;
    }

    public void setJobNember(String jobNember) {
        this.jobNember = jobNember;
    }

    @Override
    public String toString() {
        return phoneModel;
    }
}
