package com.ifeng.recallScheduler.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.ifeng.recallScheduler.constant.GyConstant;

/**
 * 用户画像中的统计信息
 */
public class RecordInfo {
    @SerializedName("n")
    @Expose
    private String recordName;

    @SerializedName("c")
    @Expose
    private int readFrequency;

    @SerializedName("s")
    @Expose
    private double weight;

    @SerializedName("e")
    @Expose
    private int expose = -1;

    @SerializedName("sim")
    @Expose
    private double sim;


    public RecordInfo(String recordName, int frequency, double weight) {
        super();
        this.recordName = recordName;
        this.readFrequency = frequency;
        this.weight = weight;
    }


    public String getRecordName() {
        return recordName;
    }

    public void setRecordName(String recordName) {
        this.recordName = recordName;
    }


    public int getReadFrequency() {
        return readFrequency;
    }

    public void setReadFrequency(int readFrequency) {
        this.readFrequency = readFrequency;
    }


    public double getWeight() {
        return weight;
    }


    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getExpose() {
        return expose;
    }

    public void setExpose(int expose) {
        this.expose = expose;
    }

    public double getSim() {
        return sim;
    }

    public void setSim(double sim) {
        this.sim = sim;
    }

    @Override
    public String toString() {
        return recordName + GyConstant.Symb_Pound + readFrequency + GyConstant.Symb_Pound + weight + GyConstant.Symb_Pound + expose;
    }
}
