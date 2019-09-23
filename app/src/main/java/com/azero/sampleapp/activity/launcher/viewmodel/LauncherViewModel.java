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


import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.azero.sampleapp.activity.launcher.recommendation.Recommendation;

import java.util.List;

public class LauncherViewModel extends AndroidViewModel {
    private LiveData<List<Recommendation>> mRecommendationData;
    private RecommendationRepository mRepository;

    public LauncherViewModel(@NonNull Application application) {
        super(application);
        mRepository = new RecommendationRepository(application);
        mRecommendationData = mRepository.getALLRecommendations();
    }

    public LiveData<List<Recommendation>> getRecommendationList() {
        return mRecommendationData;
    }

    public void insert(Recommendation recommendation) {
        mRepository.insert(recommendation);
    }
}
