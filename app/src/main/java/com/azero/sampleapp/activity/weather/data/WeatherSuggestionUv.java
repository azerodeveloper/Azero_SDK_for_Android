
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

package com.azero.sampleapp.activity.weather.data;

import android.os.Parcel;
import android.os.Parcelable;

public class WeatherSuggestionUv implements Parcelable {
    private String brief;
    private String details;
    private int level;
    private int maxLevel;
    private String iconUrl;
    private String suggest;

    public String getSuggest() {
        return suggest;
    }

    public void setSuggest(String suggest) {
        this.suggest = suggest;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getBrief() {
        return brief;
    }

    public void setBrief(String brief) {
        this.brief = brief;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }


    public WeatherSuggestionUv() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.brief);
        dest.writeString(this.details);
        dest.writeInt(this.level);
        dest.writeInt(this.maxLevel);
        dest.writeString(this.iconUrl);
    }

    protected WeatherSuggestionUv(Parcel in) {
        this.brief = in.readString();
        this.details = in.readString();
        this.level = in.readInt();
        this.maxLevel = in.readInt();
        this.iconUrl = in.readString();
    }

    public static final Creator<WeatherSuggestionUv> CREATOR = new Creator<WeatherSuggestionUv>() {
        @Override
        public WeatherSuggestionUv createFromParcel(Parcel source) {
            return new WeatherSuggestionUv(source);
        }

        @Override
        public WeatherSuggestionUv[] newArray(int size) {
            return new WeatherSuggestionUv[size];
        }
    };
}
