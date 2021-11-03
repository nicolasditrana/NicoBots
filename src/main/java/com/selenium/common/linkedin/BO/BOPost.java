package com.selenium.common.linkedin.BO;

import java.util.Objects;

public class BOPost {

    private Integer score;
    private String perfilName;
    private String url;
    private String text;
    private String isConnect;

    public static final String FOLLOWING = "Following";
    public static final String CONNECTED = "Connected";
    public static final String FOLLOW_SENT = "Follow sent";
    public static final String CONNECTED_SENT = "Connect sent";
    public static final String NOT_AGGREGATE = "Not aggregate";

    public BOPost() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getPerfilName() {
        return perfilName;
    }

    public void setPerfilName(String perfilName) {
        this.perfilName = perfilName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIsConnect() {
        return isConnect;
    }

    public void setIsConnect(String isConnect) {
        this.isConnect = isConnect;
    }

    public String toString() {
        return "" + this.getScore() + "." + this.getPerfilName() + " - " + this.getIsConnect() + " - " + this.getUrl();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BOPost boPost = (BOPost) o;
        return Objects.equals(score, boPost.score) && Objects.equals(perfilName, boPost.perfilName) && Objects.equals(url, boPost.url) && Objects.equals(text, boPost.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(score, perfilName, url, text);
    }
}