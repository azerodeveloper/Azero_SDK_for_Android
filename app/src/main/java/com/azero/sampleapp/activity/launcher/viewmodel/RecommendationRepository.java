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
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.azero.sampleapp.activity.launcher.recommendation.Recommendation;
import com.azero.sampleapp.activity.launcher.db.LauncherDatabase;
import com.azero.sampleapp.activity.launcher.recommendation.RecommendationDao;

import java.util.List;

public class RecommendationRepository {
    private RecommendationDao mRecommendationDao;
    private LiveData<List<Recommendation>> mRecommendations;

    public RecommendationRepository(Application application) {
        LauncherDatabase db = LauncherDatabase.getDatabase(application);
        mRecommendationDao = db.recommendationDao();
        mRecommendations = mRecommendationDao.getRecommendationsLiveData();
    }

    public LiveData<List<Recommendation>> getALLRecommendations() {
        return mRecommendations;
    }

    public void insert(Recommendation recommendation) {
        new insertAsyncTask(mRecommendationDao).execute(recommendation);
    }

    private static class insertAsyncTask extends AsyncTask<Recommendation, Void, Void> {

        private RecommendationDao mAsyncTaskDao;

        insertAsyncTask(RecommendationDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Recommendation... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
