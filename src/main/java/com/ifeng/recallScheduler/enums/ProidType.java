package com.ifeng.recallScheduler.enums;

/**
 */
public enum ProidType {

    ifengnews("ifengnews"),

    ifengnewslite("ifengnewslite"),

    ifengnewsredpack("ifengnewsredpack"), //资讯版

    ifengnewsdiscovery("ifengnewsdiscovery"),  //探索版

    ifengnewsvip("ifengnewsvip"),  //专业版

    ifengnewsgold("ifengnewsgold"),  //金头条

    ifengnewsh5("ifengnewsh5"),  //手凤流量

    ifengnewssdk("ifengnewssdk");


    private String value;

    ProidType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}
