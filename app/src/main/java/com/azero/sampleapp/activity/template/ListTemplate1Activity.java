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

package com.azero.sampleapp.activity.template;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.azero.sampleapp.R;
import com.azero.sdk.util.Constant;
import com.azero.sdk.util.log;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.constraintlayout.widget.ConstraintLayout;

public class ListTemplate1Activity extends BaseDisplayCardActivity{
    private TextView mMainTitle;
    private TextView mSubTitle;
    private LinearLayout mIndexList;
    private LinearLayout mContentList;
    private LayoutInflater mInf;

    @Override
    protected int getLayoutResId() {
        return R.layout.card_list_template1;
    }

    @Override
    protected void initView() {
        mMainTitle = findViewById( R.id.mainTitle );
        mSubTitle = findViewById( R.id.subTitle );
        ConstraintLayout listContainer = findViewById(R.id.listContainer);
        mIndexList = listContainer.findViewById( R.id.indexList );
        mContentList = listContainer.findViewById( R.id.contentList );
        mInf = getLayoutInflater();
    }

    @Override
    protected void initData(Intent intent) {
        try {
            JSONObject template = new JSONObject(intent.getStringExtra(Constant.EXTRA_TEMPLATE));
            ConfigureTemplateView.configureListTemplate1(this, template);
        } catch (JSONException e) {
            log.e(e.getMessage());
            finish();
        }
    }

    public void insertListItem( String index, String content ) {
        View indexItem = mInf.inflate( R.layout.card_list_template1_item_index, mIndexList, false );
        ( ( TextView ) indexItem.findViewById( R.id.index ) ).setText( index );
        mIndexList.addView( indexItem );

        View contentItem = mInf.inflate( R.layout.card_list_template1_item_content, mContentList, false );
        ( ( TextView ) contentItem.findViewById( R.id.content ) ).setText( content );
        mContentList.addView( contentItem );
    }

    public void clearLists() {
        mIndexList.removeAllViews();
        mContentList.removeAllViews();
    }

    public TextView getMainTitle() {
        return mMainTitle;
    }

    public TextView getSubTitle() {
        return mSubTitle;
    }
}
