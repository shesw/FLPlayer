package com.compassl.anji.flsts.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/10/28.
 */
public class SongInfo extends DataSupport{

    private int id;
    private int song_id;
    private String song_name;
    private String urlMp3;
    private String urlLyc;
    private String urlBgs;
    private String urlImg;
    private String urlWeb;

    public String getUrlImg() {
        return urlImg;
    }

    public void setUrlImg(String urlImg) {
        this.urlImg = urlImg;
    }

    public SongInfo() {
    }

    public SongInfo(int song_id, String song_name, String urlMp3, String urlLyc, String urlBgs,String urlImg) {
        this.song_id = song_id;
        this.song_name = song_name;
        this.urlMp3 = urlMp3;
        this.urlLyc = urlLyc;
        this.urlBgs = urlBgs;
        this.urlImg = urlImg;
    }

    public SongInfo(int song_id,String song_name, String urlMp3, String urlLyc, String urlBgs, String urlImg,String urlWeb) {
        this.song_id = song_id;
        this.song_name = song_name;
        this.urlMp3 = urlMp3;
        this.urlLyc = urlLyc;
        this.urlBgs = urlBgs;
        this.urlImg = urlImg;
        this.urlWeb = urlWeb;
    }

    public SongInfo(String song_name, String urlMp3, String urlLyc, String urlBgs, String urlImg, String urlWeb) {
        this.song_name = song_name;
        this.urlMp3 = urlMp3;
        this.urlLyc = urlLyc;
        this.urlBgs = urlBgs;
        this.urlImg = urlImg;
        this.urlWeb = urlWeb;
    }

    public String getUrlWeb() {
        return urlWeb;
    }

    public void setUrlWeb(String urlWeb) {
        this.urlWeb = urlWeb;
    }

    public int getSong_id() {
        return song_id;
    }

    public void setSong_id(int song_id) {
        this.song_id = song_id;
    }

    public String getSong_name() {
        return song_name;
    }

    public void setSong_name(String song_name) {
        this.song_name = song_name;
    }

    public String getUrlMp3() {
        return urlMp3;
    }

    public void setUrlMp3(String urlMp3) {
        this.urlMp3 = urlMp3;
    }

    public String getUrlLyc() {
        return urlLyc;
    }

    public void setUrlLyc(String urlLyc) {
        this.urlLyc = urlLyc;
    }

    public String getUrlBgs() {
        return urlBgs;
    }

    public void setUrlBgs(String urlBgs) {
        this.urlBgs = urlBgs;
    }
}