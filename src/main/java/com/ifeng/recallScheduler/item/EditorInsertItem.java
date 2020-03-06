package com.ifeng.recallScheduler.item;


import java.util.List;

public class EditorInsertItem {
    private String id;
    private String title;
    private String simId;
    private String type;
    private String picFingerprint;
    private int pos;//位置
    private int evLimit=0;//曝光上限
    private String channel;//频道
    private List<HourEditorInsert> hourEditorInsert;//24h聚合类
    private int pullnum;//第几刷出

    public int getPullnum() {
        return pullnum;
    }

    public void setPullnum(int pullnum) {
        this.pullnum = pullnum;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSimId() {
        return simId;
    }

    public void setSimId(String simId) {
        this.simId = simId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPicFingerprint() {
        return picFingerprint;
    }

    public void setPicFingerprint(String picFingerprint) {
        this.picFingerprint = picFingerprint;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getEvLimit() {
        return evLimit;
    }

    public void setEvLimit(int evLimit) {
        this.evLimit = evLimit;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public List<HourEditorInsert> getHourEditorInsert() {
        return hourEditorInsert;
    }

    public void setHourEditorInsert(List<HourEditorInsert> hourEditorInsert) {
        this.hourEditorInsert = hourEditorInsert;
    }

    @Override
    public String toString() {
        return "EditorInsert{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", simId='" + simId + '\'' +
                '}';
    }

   public class HourEditorInsert{
       private String id;
       private String title;
       private String simId;
       private String type;
       private String picFingerprint;

       public String getId() {
           return id;
       }

       public void setId(String id) {
           this.id = id;
       }

       public String getTitle() {
           return title;
       }

       public void setTitle(String title) {
           this.title = title;
       }

       public String getSimId() {
           return simId;
       }

       public void setSimId(String simId) {
           this.simId = simId;
       }

       public String getType() {
           return type;
       }

       public void setType(String type) {
           this.type = type;
       }

       public String getPicFingerprint() {
           return picFingerprint;
       }

       public void setPicFingerprint(String picFingerprint) {
           this.picFingerprint = picFingerprint;
       }


   }
}
