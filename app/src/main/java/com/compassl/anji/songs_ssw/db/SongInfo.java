package com.compassl.anji.songs_ssw.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/10/28.
 */
public class SongInfo extends DataSupport{

    private int id;
    private int newId;
    private String name;
    private String author_lyrics;
    private String author_melody;
    private String lyrics;
    private String description;


    public SongInfo(int newId, String name, String author_lyrics, String author_melody, String lyrics, String description) {
        this.newId = newId;
        this.name = name;
        this.author_lyrics = author_lyrics;
        this.author_melody = author_melody;
        this.lyrics = lyrics;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNewId() {
        return newId;
    }

    public void setNewId(int newId) {
        this.newId = newId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor_lyrics() {
        return author_lyrics;
    }

    public void setAuthor_lyrics(String author_lyrics) {
        this.author_lyrics = author_lyrics;
    }

    public String getAuthor_melody() {
        return author_melody;
    }

    public void setAuthor_melody(String author_melody) {
        this.author_melody = author_melody;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}