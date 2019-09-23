/*
 * Copyright (c) 2019 SoundAI. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.azero.sampleapp.activity.launcher.recommendation;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.os.Parcel;
import android.os.Parcelable;

import com.azero.sampleapp.activity.launcher.viewmodel.Converters;

/**
 * Description:
 * Created by WangXin 19-2-25
 */
@Entity
public class Recommendation implements Parcelable {

    @PrimaryKey
    private long id;

    private String title;
    private String introduce;
    private String bgUrl;
    private String asrQuery;
    @TypeConverters({Converters.class})
    private Type type;

    public Recommendation(long id, Type type, String title, String introduce, String bgUrl, String asrQuery) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.introduce = introduce;
        this.bgUrl = bgUrl;
        this.asrQuery = asrQuery;
    }

    public String getTitle() {
        return title;
    }

    public String getIntroduce() {
        return introduce;
    }

    public String getBgUrl() {
        return bgUrl;
    }

    public String getAsrQuery() {
        return asrQuery;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.introduce);
        dest.writeString(this.bgUrl);
        dest.writeString(this.asrQuery);
    }

    protected Recommendation(Parcel in) {
        this.title = in.readString();
        this.introduce = in.readString();
        this.bgUrl = in.readString();
        this.asrQuery = in.readString();
    }

    public static final Creator<Recommendation> CREATOR = new Creator<Recommendation>() {
        @Override
        public Recommendation createFromParcel(Parcel source) {
            return new Recommendation(source);
        }

        @Override
        public Recommendation[] newArray(int size) {
            return new Recommendation[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public void setBgUrl(String bgUrl) {
        this.bgUrl = bgUrl;
    }

    public void setAsrQuery(String asrQuery) {
        this.asrQuery = asrQuery;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        TEMPLATE1,
        TEMPLATE2;

        public static Type convert(int value) {
            switch (value) {
                case 0:
                    return TEMPLATE1;
                case 1:
                    return TEMPLATE2;
                default:
                    return TEMPLATE1;
            }
        }
    }
}
