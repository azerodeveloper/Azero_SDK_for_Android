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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.azero.sampleapp.R;
import com.azero.sampleapp.activity.playerinfo.news.bean.NewsInfo;
import com.bumptech.glide.Glide;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private List<NewsInfo> mDataList;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_news_content, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        Glide.with(viewHolder.newsPic.getContext())
                .load(mDataList.get(position).getPicUrl())
                .into(viewHolder.newsPic);
        viewHolder.newsTitle.setText(mDataList.get(position).getTitle());
        viewHolder.newsSource.setText(mDataList.get(position).getSource());
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public void setDataList(List<NewsInfo> dataList) {
        mDataList = dataList;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView newsPic;
        TextView newsTitle;
        TextView newsSource;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            newsPic = itemView.findViewById(R.id.news_item_pic);
            newsTitle = itemView.findViewById(R.id.news_item_title);
            newsSource = itemView.findViewById(R.id.news_item_source);
        }
    }

    public static class NewsDiffCallback extends DiffUtil.Callback {
        private List<NewsInfo> oldList;
        private List<NewsInfo> newList;

        public NewsDiffCallback(List<NewsInfo> oldList, List<NewsInfo> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return true;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getTitle()
                    .equals(newList.get(newItemPosition).getTitle());
        }
    }
}
