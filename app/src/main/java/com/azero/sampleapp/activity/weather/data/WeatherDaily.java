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

public class WeatherDaily implements Parcelable {
    private String date;
    private String textDay;
    private String codeDay;
    private String textNight;
    private String codeNight;
    private String high;
    private String low;
    private String precip;
    private String windDirection;
    private String windDirectionDegree;
    private String windSpeed;
    private String windScale;
    private String iconUrl;
    private String backgroundUrl;
    private String week;
    private String monthDay;


    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getMonthDay() {
        return monthDay;
    }

    public void setMonthDay(String monthDay) {
        this.monthDay = monthDay;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTextDay() {
        return textDay;
    }

    public void setTextDay(String textDay) {
        this.textDay = textDay;
    }

    public String getCodeDay() {
        return codeDay;
    }

    public void setCodeDay(String codeDay) {
        this.codeDay = codeDay;
    }

    public String getTextNight() {
        return textNight;
    }

    public void setTextNight(String textNight) {
        this.textNight = textNight;
    }

    public String getCodeNight() {
        return codeNight;
    }

    public void setCodeNight(String codeNight) {
        this.codeNight = codeNight;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getPrecip() {
        return precip;
    }

    public void setPrecip(String precip) {
        this.precip = precip;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    public String getWindDirectionDegree() {
        return windDirectionDegree;
    }

    public void setWindDirectionDegree(String windDirectionDegree) {
        this.windDirectionDegree = windDirectionDegree;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getWindScale() {
        return windScale;
    }

    public void setWindScale(String windScale) {
        this.windScale = windScale;
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


    public WeatherDaily() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.date);
        dest.writeString(this.textDay);
        dest.writeString(this.codeDay);
        dest.writeString(this.textNight);
        dest.writeString(this.codeNight);
        dest.writeString(this.high);
        dest.writeString(this.low);
        dest.writeString(this.precip);
        dest.writeString(this.windDirection);
        dest.writeString(this.windDirectionDegree);
        dest.writeString(this.windSpeed);
        dest.writeString(this.windScale);
        dest.writeString(this.iconUrl);
        dest.writeString(this.backgroundUrl);
        dest.writeString(this.week);
        dest.writeString(this.monthDay);
    }

    protected WeatherDaily(Parcel in) {
        this.date = in.readString();
        this.textDay = in.readString();
        this.codeDay = in.readString();
        this.textNight = in.readString();
        this.codeNight = in.readString();
        this.high = in.readString();
        this.low = in.readString();
        this.precip = in.readString();
        this.windDirection = in.readString();
        this.windDirectionDegree = in.readString();
        this.windSpeed = in.readString();
        this.windScale = in.readString();
        this.iconUrl = in.readString();
        this.backgroundUrl = in.readString();
        this.week = in.readString();
        this.monthDay = in.readString();
    }

    public static final Creator<WeatherDaily> CREATOR = new Creator<WeatherDaily>() {
        @Override
        public WeatherDaily createFromParcel(Parcel source) {
            return new WeatherDaily(source);
        }

        @Override
        public WeatherDaily[] newArray(int size) {
            return new WeatherDaily[size];
        }
    };
}
