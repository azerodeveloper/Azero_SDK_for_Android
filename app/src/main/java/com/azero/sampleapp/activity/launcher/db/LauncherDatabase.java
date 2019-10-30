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

package com.azero.sampleapp.activity.launcher.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.azero.sampleapp.activity.launcher.recommendation.Recommendation;
import com.azero.sampleapp.activity.launcher.recommendation.RecommendationDao;
import com.azero.sdk.util.log;

import java.util.ArrayList;
import java.util.List;

@Database(entities = {Recommendation.class}, version = 1, exportSchema = false)
public abstract class LauncherDatabase extends RoomDatabase {

    public abstract RecommendationDao recommendationDao();

    private static volatile LauncherDatabase INSTANCE;

    public static LauncherDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (LauncherDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), LauncherDatabase.class, "azero_room_launcher.db")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback() {

                @Override
                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                    super.onOpen(db);
                    new PopulateDbAsync(INSTANCE).execute();
                }
            };

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final RecommendationDao mDao;

        PopulateDbAsync(LauncherDatabase db) {
            mDao = db.recommendationDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            List<Recommendation> allRecommendations = mDao.getRecommendations();
            if (allRecommendations != null && allRecommendations.size() > 0) {
                return null;
            }
            log.d("create default recommendation list");
            //数据库为空时，创建默认界面
            List<Recommendation> recommendations = new ArrayList<>();
            recommendations.add(new Recommendation(0, Recommendation.Type.TEMPLATE2, "每日天气预报", "“今天天气怎么样”", "https://cms-azero.soundai.cn:8443/v1/cmsservice/resource/e6c634f1dfcfb7d1072c74faf3f3db19.jpg", ""));
            recommendations.add(new Recommendation(1, Recommendation.Type.TEMPLATE2, "经典老歌:带你回顾蔡琴的渡口", "“播放蔡琴的渡口”", "https://cms-azero.soundai.cn:8443/v1/cmsservice/resource/8a5438b57d63348624f9b6a4f5feefc9.jpg", ""));
            recommendations.add(new Recommendation(2, Recommendation.Type.TEMPLATE2, "爆笑相声集:让你笑不停", "“播放相声”", "https://cms-azero.soundai.cn:8443/v1/cmsservice/resource/513ec1f20c77fd7ab796bc15b1d4e59a.jpg", ""));
            recommendations.add(new Recommendation(3, Recommendation.Type.TEMPLATE2, "闹钟提醒:设定提醒，合理安排时间", "“下午四点提醒我取钱”", "https://cms-azero.soundai.cn:8443/v1/cmsservice/resource/19c0cf732375eaef5010bdc9391a7bf6.jpg", ""));
            recommendations.add(new Recommendation(4, Recommendation.Type.TEMPLATE2, "垃圾分类:助你轻松归类生活垃圾", "“酸奶瓶是什么垃圾”", "https://cms-azero.soundai.cn:8443/v1/cmsservice/resource/0cdc9a70977d93312115284e0000d433.jpg", ""));
            mDao.insert(recommendations);
            return null;
        }
    }
}
