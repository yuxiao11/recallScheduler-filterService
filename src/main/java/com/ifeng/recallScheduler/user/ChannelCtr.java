package com.ifeng.recallScheduler.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class ChannelCtr {

    @Expose
    @SerializedName("hot_pv")
    private int  hot_pv;

    @Expose
    @SerializedName("hot_ev")
    private int  hot_ev;

    @Expose
    @SerializedName("hot_ctr")
    private double  hot_ctr;

    @Expose
    @SerializedName("local_pv")
    private int  local_pv;

    @Expose
    @SerializedName("local_ev")
    private int  local_ev;

    @Expose
    @SerializedName("local_ctr")
    private double  local_ctr;

    @Expose
    @SerializedName("jppool_ev")
    private int jppool_ev;

    @Expose
    @SerializedName("jppool_ctr")
    private double jppool_ctr;

    @Expose
    @SerializedName("total_pv")
    private int  total_pv;

    @Expose
    @SerializedName("total_ev")
    private int  total_ev;

    @Expose
    @SerializedName("total_ctr")
    private double  total_ctr;

    public int getHot_pv() {
        return hot_pv;
    }

    public void setHot_pv(int hot_pv) {
        this.hot_pv = hot_pv;
    }

    public int getHot_ev() {
        return hot_ev;
    }

    public void setHot_ev(int hot_ev) {
        this.hot_ev = hot_ev;
    }

    public double getHot_ctr() {
        return hot_ctr;
    }

    public void setHot_ctr(double hot_ctr) {
        this.hot_ctr = hot_ctr;
    }

    public int getLocal_pv() {
        return local_pv;
    }

    public void setLocal_pv(int local_pv) {
        this.local_pv = local_pv;
    }

    public int getLocal_ev() {
        return local_ev;
    }

    public void setLocal_ev(int local_ev) {
        this.local_ev = local_ev;
    }

    public double getJppool_ctr() {
        return jppool_ctr;
    }

    public void setJppool_ctr(double jppool_ctr) {
        this.jppool_ctr = jppool_ctr;
    }

    public int getJppool_ev() {
        return jppool_ev;
    }

    public void setJppool_ev(int jppool_ev) {
        this.jppool_ev = jppool_ev;
    }

    public double getLocal_ctr() {
        return local_ctr;
    }

    public void setLocal_ctr(double local_ctr) {
        this.local_ctr = local_ctr;
    }

    public int getTotal_pv() {
        return total_pv;
    }

    public void setTotal_pv(int total_pv) {
        this.total_pv = total_pv;
    }

    public int getTotal_ev() {
        return total_ev;
    }

    public void setTotal_ev(int total_ev) {
        this.total_ev = total_ev;
    }

    public double getTotal_ctr() {
        return total_ctr;
    }

    public void setTotal_ctr(double total_ctr) {
        this.total_ctr = total_ctr;
    }




}
