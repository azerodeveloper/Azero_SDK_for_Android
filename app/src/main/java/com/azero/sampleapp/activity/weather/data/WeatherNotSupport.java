
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

public class WeatherNotSupport implements Parcelable {
    private String message;
    private String backgroundUrl;


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getBackgroundUrl() {
        return backgroundUrl;
    }

    public void setBackgroundUrl(String backgroundUrl) {
        this.backgroundUrl = backgroundUrl;
    }

    public WeatherNotSupport() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.message);
        dest.writeString(this.backgroundUrl);
    }

    protected WeatherNotSupport(Parcel in) {
        this.message = in.readString();
        this.backgroundUrl = in.readString();
    }

    public static final Creator<WeatherNotSupport> CREATOR = new Creator<WeatherNotSupport>() {
        @Override
        public WeatherNotSupport createFromParcel(Parcel source) {
            return new WeatherNotSupport(source);
        }

        @Override
        public WeatherNotSupport[] newArray(int size) {
            return new WeatherNotSupport[size];
        }
    };
}
