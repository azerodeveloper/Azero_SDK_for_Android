package com.azero.sampleapp.activity.playerinfo.bean;

import android.support.annotation.NonNull;

public class LineBean implements Comparable<LineBean>{

    private String content;
    private long start;
    private long end;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "LineBean{" +
                "content='" + content + '\'' +
                ", start=" + start +
                ", end=" + end +
                '}';
    }

    @Override
    public int compareTo(@NonNull LineBean lineBean) {
        int i = (int) (this.getStart()-lineBean.getStart());
        if(i==0){
            return (int) (this.getEnd()-lineBean.getEnd());
        }
        return i;
    }
}
