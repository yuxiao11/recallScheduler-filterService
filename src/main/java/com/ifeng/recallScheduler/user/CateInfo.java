package com.ifeng.recallScheduler.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.ifeng.recallScheduler.constant.GyConstant;

/**
 * 用户画像中的统计信息
 */
public class CateInfo {
    @SerializedName("n")
    @Expose
    private String recordName;

    @SerializedName("c")
    @Expose
    private int readFrequency;


    @SerializedName("level")
    @Expose
    private String level = "level2";




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


    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return recordName + GyConstant.Symb_Pound + readFrequency + GyConstant.Symb_Pound + level;
    }
}
