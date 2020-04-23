package com.azero.sampleapp.activity.weather.data;

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

import android.os.Parcel;
import android.os.Parcelable;

public class WeatherNow implements Parcelable {
    private String text;
    private String code;
    private String temperature;
    private String feelsLike;
    private String pressure;
    private String humidity;
    private String visibility;
    private String windDirection;
    private String windDirectionDegree;
    private String windSpeed;
    private String windScale;
    private String iconUrl;
    private String backgroundUrl;
    private String windDirectionIcon;


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(String feelsLike) {
        this.feelsLike = feelsLike;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
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

    public String getWindDirectionIcon() {
        return windDirectionIcon;
    }

    public void setWindDirectionIcon(String windDirectionIcon) {
        this.windDirectionIcon = windDirectionIcon;
    }


    public WeatherNow() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.text);
        dest.writeString(this.code);
        dest.writeString(this.temperature);
        dest.writeString(this.feelsLike);
        dest.writeString(this.pressure);
        dest.writeString(this.humidity);
        dest.writeString(this.visibility);
        dest.writeString(this.windDirection);
        dest.writeString(this.windDirectionDegree);
        dest.writeString(this.windSpeed);
        dest.writeString(this.windScale);
        dest.writeString(this.iconUrl);
        dest.writeString(this.backgroundUrl);
        dest.writeString(this.windDirectionIcon);
    }

    protected WeatherNow(Parcel in) {
        this.text = in.readString();
        this.code = in.readString();
        this.temperature = in.readString();
        this.feelsLike = in.readString();
        this.pressure = in.readString();
        this.humidity = in.readString();
        this.visibility = in.readString();
        this.windDirection = in.readString();
        this.windDirectionDegree = in.readString();
        this.windSpeed = in.readString();
        this.windScale = in.readString();
        this.iconUrl = in.readString();
        this.backgroundUrl = in.readString();
        this.windDirectionIcon = in.readString();
    }

    public static final Creator<WeatherNow> CREATOR = new Creator<WeatherNow>() {
        @Override
        public WeatherNow createFromParcel(Parcel source) {
            return new WeatherNow(source);
        }

        @Override
        public WeatherNow[] newArray(int size) {
            return new WeatherNow[size];
        }
    };
}
