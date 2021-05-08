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

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RecommendationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Recommendation... recommendations);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Recommendation> recommendations);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int update(Recommendation... recommendations);

    @Delete
    void delete(Recommendation... recommendations);

    @Query("SELECT * FROM Recommendation")
    LiveData<List<Recommendation>> getRecommendationsLiveData();

    @Query("SELECT * FROM Recommendation")
    List<Recommendation> getRecommendations();


}
