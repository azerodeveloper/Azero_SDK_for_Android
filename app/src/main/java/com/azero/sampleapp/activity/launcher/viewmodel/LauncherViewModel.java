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
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;

import com.azero.sampleapp.activity.launcher.LauncherDataManager;
import com.azero.sampleapp.activity.launcher.recommendation.Recommendation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LauncherViewModel extends AndroidViewModel {
    private LauncherDataManager launcherDataManager;

    private boolean needClearRecommendations = true;

    private MutableLiveData<List<Recommendation>> mRecommendationData = new MutableLiveData<>();

    public LauncherViewModel(@NonNull Application application) {
        super(application);
        launcherDataManager = new LauncherDataManager();
        mRecommendationData.setValue(new ArrayList<>());
    }

    public LiveData<List<Recommendation>> getRecommendationList() {
        return mRecommendationData;
    }

    public void loadRecommendations(JSONObject template) {
        List<Recommendation> recommendationList = mRecommendationData.getValue();
        if (recommendationList == null) {
            return;
        }
        if (needClearRecommendations) {
            recommendationList.clear();
            needClearRecommendations = false;
        }
        try {
            String url = ((JSONObject) (template.getJSONObject("backgroundImage")
                    .getJSONArray("sources").get(0))).getString("url");
            String contentId = template.getString("contentId");
            String textContent = template.getString("textContent");
            String prompt = template.getString("prompt");
            Recommendation recommendation = new Recommendation();
            recommendation.setBgUrl(url);
            recommendation.setContentId(contentId);
            recommendation.setTitle(textContent);
            recommendation.setIntroduce(prompt);

            recommendationList.add(recommendation);
            mRecommendationData.postValue(recommendationList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateRecommendations(String contentId) {
        launcherDataManager.updateLauncher(contentId);
    }

    public void initDefaultRecommendation() {
        List<Recommendation> recommendationList = mRecommendationData.getValue();
        if (recommendationList == null) {
            return;
        }
        Recommendation recommendation = new Recommendation(0, Recommendation.Type.TEMPLATE2, "每日天气预报", "“今天天气怎么样”", "https://cms-azero.soundai.cn:8443/v1/cmsservice/resource/e6c634f1dfcfb7d1072c74faf3f3db19.jpg", "");
        recommendationList.add(recommendation);
        mRecommendationData.postValue(recommendationList);
    }
}
