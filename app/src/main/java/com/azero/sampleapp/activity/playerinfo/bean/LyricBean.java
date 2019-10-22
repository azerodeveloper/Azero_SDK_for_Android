package com.azero.sampleapp.activity.playerinfo.bean;

import java.util.ArrayList;
import java.util.List;

public class LyricBean {

    private List<LineBean> lines;
    private String artist;
    private String title;
    private String album;
    private long offset;

    public List<LineBean> getLines() {
        if (lines == null) {
            lines = new ArrayList<>();
        }
        return lines;
    }

    public void setLines(List<LineBean> lines) {
        this.lines = lines;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }
}
