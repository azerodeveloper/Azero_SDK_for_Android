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

public class WeatherAir implements Parcelable {
    private String data;
    private String aqi;
    private String pm25;
    private String pm10;
    private String so2;
    private String no2;
    private String co;
    private String o3;
    private String primaryPollutant;
    private String quality;
    private int level;
    private int maxLevel;
    private String iconUrl;
    private String backgroundUrl;



    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getBackgroundUrl() {
        return backgroundUrl;
    }

    public void setBackgroundUrl(String backgroundUrl) {
        this.backgroundUrl = backgroundUrl;
    }

    public String getAqi() {
        return aqi;
    }

    public void setAqi(String aqi) {
        this.aqi = aqi;
    }

    public String getPm25() {
        return pm25;
    }

    public void setPm25(String pm25) {
        this.pm25 = pm25;
    }

    public String getPm10() {
        return pm10;
    }

    public void setPm10(String pm10) {
        this.pm10 = pm10;
    }

    public String getSo2() {
        return so2;
    }

    public void setSo2(String so2) {
        this.so2 = so2;
    }

    public String getNo2() {
        return no2;
    }

    public void setNo2(String no2) {
        this.no2 = no2;
    }

    public String getCo() {
        return co;
    }

    public void setCo(String co) {
        this.co = co;
    }

    public String getO3() {
        return o3;
    }

    public void setO3(String o3) {
        this.o3 = o3;
    }

    public String getPrimaryPollutant() {
        return primaryPollutant;
    }

    public void setPrimaryPollutant(String primary_pollutant) {
        this.primaryPollutant = primary_pollutant;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
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


    public WeatherAir() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.data);
        dest.writeString(this.aqi);
        dest.writeString(this.pm25);
        dest.writeString(this.pm10);
        dest.writeString(this.so2);
        dest.writeString(this.no2);
        dest.writeString(this.co);
        dest.writeString(this.o3);
        dest.writeString(this.primaryPollutant);
        dest.writeString(this.quality);
        dest.writeInt(this.level);
        dest.writeInt(this.maxLevel);
        dest.writeString(this.iconUrl);
        dest.writeString(this.backgroundUrl);
    }

    protected WeatherAir(Parcel in) {
        this.data = in.readString();
        this.aqi = in.readString();
        this.pm25 = in.readString();
        this.pm10 = in.readString();
        this.so2 = in.readString();
        this.no2 = in.readString();
        this.co = in.readString();
        this.o3 = in.readString();
        this.primaryPollutant = in.readString();
        this.quality = in.readString();
        this.level = in.readInt();
        this.maxLevel = in.readInt();
        this.iconUrl = in.readString();
        this.backgroundUrl = in.readString();
    }

    public static final Creator<WeatherAir> CREATOR = new Creator<WeatherAir>() {
        @Override
        public WeatherAir createFromParcel(Parcel source) {
            return new WeatherAir(source);
        }

        @Override
        public WeatherAir[] newArray(int size) {
            return new WeatherAir[size];
        }
    };
}
