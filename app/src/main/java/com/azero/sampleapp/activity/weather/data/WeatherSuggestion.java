
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

public class WeatherSuggestion implements Parcelable {
    Suggestion carWashing;
    Suggestion dressing;
    Suggestion umbrella;
    String backgroundUrl;


    public WeatherSuggestion() {
    }

    public Suggestion getCarWashing() {
        return carWashing;
    }

    public void setCarWashing(Suggestion carWashing) {
        this.carWashing = carWashing;
    }


    public Suggestion getDressing() {
        return dressing;
    }

    public void setDressing(Suggestion dressing) {
        this.dressing = dressing;
    }

    public Suggestion getUmbrella() {
        return umbrella;
    }

    public void setUmbrella(Suggestion umbrella) {
        this.umbrella = umbrella;
    }

    public String getBackgroundUrl() {
        return backgroundUrl;
    }

    public void setBackgroundUrl(String backgroundUrl) {
        this.backgroundUrl = backgroundUrl;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.carWashing, flags);
        dest.writeParcelable(this.dressing, flags);
        dest.writeParcelable(this.umbrella, flags);
        dest.writeString(this.backgroundUrl);
    }

    protected WeatherSuggestion(Parcel in) {
        this.carWashing = in.readParcelable(Suggestion.class.getClassLoader());
        this.dressing = in.readParcelable(Suggestion.class.getClassLoader());
        this.umbrella = in.readParcelable(Suggestion.class.getClassLoader());
        this.backgroundUrl = in.readString();
    }

    public static final Creator<WeatherSuggestion> CREATOR = new Creator<WeatherSuggestion>() {
        @Override
        public WeatherSuggestion createFromParcel(Parcel source) {
            return new WeatherSuggestion(source);
        }

        @Override
        public WeatherSuggestion[] newArray(int size) {
            return new WeatherSuggestion[size];
        }
    };
}
