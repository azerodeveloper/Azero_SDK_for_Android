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

package com.azero.sampleapp.activity.launcher.viewmodel;



import com.azero.sampleapp.activity.launcher.recommendation.Recommendation;

import androidx.room.TypeConverter;

public class Converters {
    @TypeConverter
    public static Recommendation.Type fromValue(int value) {
        return Recommendation.Type.values()[value];
    }

    @TypeConverter
    public static int typeToValue(Recommendation.Type type) {
        return type.ordinal();
    }
}
