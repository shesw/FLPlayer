package com.compassl.anji.songs_ssw;

/**
 * Created by Administrator on 2017/10/28.
 */
public class Song {
    private String name;
    private String imgRes;

    public Song(String name, String imgRes) {
        this.name = name;
        this.imgRes = imgRes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgRes() {
        return imgRes;
    }

    public void setImgRes(String imgRes) {
        this.imgRes = imgRes;
    }
}