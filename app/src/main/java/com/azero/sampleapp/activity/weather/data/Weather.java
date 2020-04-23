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

import java.util.List;

public class Weather implements Parcelable {

    private String date = null;
    private String week = null;
    private String city = null;
    private WeatherNow now;
    private WeatherSuggestion suggestion;
    private WeatherSuggestionUv uv;
    private WeatherAir air;
    private WeatherDailyBean daily;
    private WeatherDaily weather;
    private WeatherNotSupport notSupport;
    private List<WeatherAir> airs;
    private String answer;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public WeatherNow getNow() {
        return now;
    }

    public void setNow(WeatherNow now) {
        this.now = now;
    }

    public WeatherSuggestion getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(WeatherSuggestion suggestion) {
        this.suggestion = suggestion;
    }

    public WeatherSuggestionUv getUv() {
        return uv;
    }

    public void setUv(WeatherSuggestionUv uv) {
        this.uv = uv;
    }

    public WeatherAir getAir() {
        return air;
    }

    public void setAir(WeatherAir air) {
        this.air = air;
    }

    public WeatherDailyBean getDaily() {
        return daily;
    }

    public void setDaily(WeatherDailyBean daily) {
        this.daily = daily;
    }

    public WeatherDaily getWeather() {
        return weather;
    }

    public void setWeather(WeatherDaily weather) {
        this.weather = weather;
    }

    public WeatherNotSupport getNotSupport() {
        return notSupport;
    }

    public void setNotSupport(WeatherNotSupport notSupport) {
        this.notSupport = notSupport;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<WeatherAir> getAirs() {
        return airs;
    }

    public void setAirs(List<WeatherAir> airs) {
        this.airs = airs;
    }


    public Weather() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.date);
        dest.writeString(this.week);
        dest.writeString(this.city);
        dest.writeParcelable(this.now, flags);
        dest.writeParcelable(this.suggestion, flags);
        dest.writeParcelable(this.uv, flags);
        dest.writeParcelable(this.air, flags);
        dest.writeParcelable(this.daily, flags);
        dest.writeParcelable(this.weather, flags);
        dest.writeParcelable(this.notSupport, flags);
        dest.writeTypedList(this.airs);
        dest.writeString(this.answer);
    }

    protected Weather(Parcel in) {
        this.date = in.readString();
        this.week = in.readString();
        this.city = in.readString();
        this.now = in.readParcelable(WeatherNow.class.getClassLoader());
        this.suggestion = in.readParcelable(WeatherSuggestion.class.getClassLoader());
        this.uv = in.readParcelable(WeatherSuggestionUv.class.getClassLoader());
        this.air = in.readParcelable(WeatherAir.class.getClassLoader());
        this.daily = in.readParcelable(WeatherDailyBean.class.getClassLoader());
        this.weather = in.readParcelable(WeatherDaily.class.getClassLoader());
        this.notSupport = in.readParcelable(WeatherNotSupport.class.getClassLoader());
        this.airs = in.createTypedArrayList(WeatherAir.CREATOR);
        this.answer = in.readString();
    }

    public static final Creator<Weather> CREATOR = new Creator<Weather>() {
        @Override
        public Weather createFromParcel(Parcel source) {
            return new Weather(source);
        }

        @Override
        public Weather[] newArray(int size) {
            return new Weather[size];
        }
    };
}
