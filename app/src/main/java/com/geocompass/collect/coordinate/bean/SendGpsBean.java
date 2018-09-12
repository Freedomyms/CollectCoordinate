package com.geocompass.collect.coordinate.bean;

/**
 * Created by admin on 2018/7/24.
 */

public class SendGpsBean {
    private String coor;
    private String id;

    @Override
    public String toString() {
        return "SendGpsBean{" +
                "coor='" + coor + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

    public String getCoor() {
        return coor;
    }

    public void setCoor(String coor) {
        this.coor = coor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
