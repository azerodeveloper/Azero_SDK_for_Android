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

package com.azero.sampleapp.activity.playerinfo.news;

import android.content.Intent;
import android.graphics.Rect;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.azero.platforms.iface.MediaPlayer;
import com.azero.sampleapp.R;
import com.azero.sampleapp.activity.playerinfo.BasePlayerInfoActivity;
import com.azero.sampleapp.activity.playerinfo.news.bean.NewsInfo;
import com.azero.sampleapp.util.DisplayUtils;
import com.azero.sdk.AzeroManager;
import com.azero.sdk.event.Command;
import com.azero.sdk.impl.MediaPlayer.MediaPlayerHandler;
import com.azero.sdk.util.Constant;
import com.azero.sdk.util.log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NewsActivity extends BasePlayerInfoActivity {
    private List<NewsInfo> mDataList = new ArrayList<>();

    private List<Integer> operateQueue = new LinkedList<>();

    private RecyclerView mRecyclerView;

    private PagerSnapHelper mSnapHelper;

    private NewsAdapter mAdapter;

    private int mCurrentPosition;

    private boolean mShouldPlayOnStart;

    @Override
    protected int getLayoutResId() {
        return R.layout.card_news_templete;
    }

    @Override
    protected void initView() {
        findViewById(R.id.news_back_btn).setOnClickListener(v -> finish());

        mRecyclerView = findViewById(R.id.news_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        mAdapter = new NewsAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new NewsItemDecoration());

        mSnapHelper = new PagerSnapHelper();
        mSnapHelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    return;
                }

                View targetView = mSnapHelper.findSnapView(recyclerView.getLayoutManager());
                if (targetView == null) return;
                int position = recyclerView.getChildAdapterPosition(
                        mSnapHelper.findSnapView(recyclerView.getLayoutManager()));
                if (mCurrentPosition != RecyclerView.NO_POSITION && mCurrentPosition != position) {
                    int offset = position - mCurrentPosition;
                    if (offset > 0) {
                        for (int i = 0; i < offset; i++) {
                            AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_NEXT);
                            operateQueue.add(++mCurrentPosition);
                        }
                    } else if (offset < 0) {
                        for (int i = 0; i < -offset; i++) {
                            AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_PREVIOUS);
                            operateQueue.add(--mCurrentPosition);
                        }
                    }
                }
            }
        });

        MediaPlayerHandler mediaPlayerHandler = (MediaPlayerHandler) AzeroManager.getInstance().getHandler(AzeroManager.AUDIO_HANDLER);
        mediaPlayerHandler.addOnMediaStateChangeListener(new MediaPlayer.OnMediaStateChangeListener() {
            @Override
            public void onMediaError(String playerName, String msg, MediaPlayer.MediaError mediaError) {
                log.e("playerName: " + playerName + " reason: " + msg + " Error:" + mediaError.toString());
            }

            @Override
            public void onMediaStateChange(String playerName, MediaPlayer.MediaState mediaState) {

                switch (mediaState) {
                    case STOPPED:
                        mShouldPlayOnStart = true;
                        break;
                    case PLAYING:
                        mShouldPlayOnStart = false;
                        break;
                    case BUFFERING:
                        break;
                }
            }

            @Override
            public void onPositionChange(String s, long l, long l1) {
                // do nothing
            }
        });
    }

    @Override
    protected void initData(Intent intent) {
        try {
            // 1.尝试更新数据列表
            JSONObject playerInfo = new JSONObject(intent.getStringExtra(Constant.EXTRA_TEMPLATE));
            JSONArray contents = playerInfo.getJSONArray("contents");
            List<NewsInfo> newList = new ArrayList<>();
            int len = contents.length();
            for (int i = 0; i < len; i++) {
                JSONObject content = contents.getJSONObject(i);
                NewsInfo newsInfo = new NewsInfo();
                newsInfo.setTitle(content.getString("title"));
                JSONObject art = content.getJSONObject("art");
                JSONObject source = art.getJSONArray("sources").getJSONObject(0);
                String url = source.getString("url");
                newsInfo.setPicUrl(url);
                JSONObject provider = content.getJSONObject("provider");
                newsInfo.setSource(provider.getString("name"));
                newList.add(newsInfo);
            }
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new NewsAdapter.NewsDiffCallback(mDataList, newList));
            mAdapter.setDataList(newList);
            diffResult.dispatchUpdatesTo(mAdapter);
            mDataList = newList;

            // 2.定位当前新闻的位置
            JSONObject content = playerInfo.getJSONObject("content");
            int position = content.getInt("position");
            checkOperate(position);
            if (position != mCurrentPosition && operateQueue.isEmpty()) {
                mCurrentPosition = position;
                mRecyclerView.smoothScrollToPosition(position);
            }
        } catch (JSONException e) {
            log.e(e.getMessage());
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mShouldPlayOnStart) {
            AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_PLAY);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_PAUSE);
    }

    private void checkOperate(int position) {
        if (!operateQueue.isEmpty() && operateQueue.get(0) == position) {
            operateQueue.remove(0);
        }
    }

    class NewsItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (parent.getAdapter().getItemCount() == 1) {
                outRect.left = DisplayUtils.dp2px(57f);
                outRect.right = DisplayUtils.dp2px(57f);
                return;
            }
            int index = parent.getChildAdapterPosition(view);
            if (index == 0) {
                outRect.left = DisplayUtils.dp2px(57f);
                outRect.right = DisplayUtils.dp2px(16.5f);
            } else if (index == parent.getAdapter().getItemCount() - 1) {
                outRect.left = DisplayUtils.dp2px(16.5f);
                outRect.right = DisplayUtils.dp2px(57f);
            } else {
                outRect.left = DisplayUtils.dp2px(16.5f);
                outRect.right = DisplayUtils.dp2px(16.5f);
            }
        }
    }
}
