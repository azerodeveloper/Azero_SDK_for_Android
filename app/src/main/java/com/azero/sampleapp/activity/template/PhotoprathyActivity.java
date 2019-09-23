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

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;

import com.azero.sampleapp.R;
import com.azero.sampleapp.activity.template.BaseDisplayCardActivity;
import com.azero.sdk.util.Constant;
import com.azero.sdk.util.log;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Description ：
 * Created by SoundAI jiesean on 2019/8/30.
 */
public class PhotoprathyActivity extends BaseDisplayCardActivity {

    private TextView mMainTitle;
    private TextView mPhotoTitle;
    private TextView mAuthorTitile;
    private TextView mPhotoDate;
    private ImageView photo;
    private ImageView mQrcode;

    private boolean isFirstLanuch = true;


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_photography;
    }

    @Override
    protected void initView() {
        mMainTitle = findViewById(R.id.main_title_textView);
        mAuthorTitile = findViewById(R.id.author_textView);
        mPhotoDate = findViewById(R.id.date_textView);
        mPhotoTitle = findViewById(R.id.title_textView);
        photo = findViewById(R.id.photo_imageView);
        mQrcode = findViewById(R.id.qrcode);
    }

    @Override
    protected void onResume() {
        super.onResume();

        log.e("aaaa: onResume is running");
//        if (!isFirstLanuch) {
//            restartActivity(PhotoprathyActivity.this);
//        }
//
//        isFirstLanuch = false;
    }

    public static void restartActivity(Activity activity){
        log.e("aaaa: restartActivity");

        Intent intent = new Intent();
        intent.setClass(activity, activity.getClass());
        activity.startActivity(intent);
        activity.overridePendingTransition(0,0);
        activity.finish();
    }


    @Override
    protected void initData(Intent intent) {

        try {
            JSONObject template = new JSONObject(intent.getStringExtra(Constant.EXTRA_TEMPLATE));
            if (template.has("title")) {
                JSONObject title = template.getJSONObject("title");
                if (title.has("mainTitle")) {
                    String mainTitle = title.getString("mainTitle");
                    mMainTitle.setText(mainTitle);
                }
            }
            if (template.has("photoInfo")) {
                JSONObject photoInfo = template.getJSONObject("photoInfo");
                String title = photoInfo.getString("title");
                String author = photoInfo.getString("author");
                String date = photoInfo.getString("date");
                String desc = photoInfo.getString("description");
                String qrcode = photoInfo.getString("qrcode");

                mAuthorTitile.setText(author);
                mPhotoTitle.setText(title);
                mPhotoDate.setText(date);
                Glide.with(getApplicationContext())
                        .load(qrcode)
//                                .apply(options)
                        .into(mQrcode);

            }

            if (template.has("textField")) {
                String textField = template.getString("textField");
//                ba.getTextField().setText(textField);
//                ba.getTextField().setMovementMethod(ScrollingMovementMethod.getInstance());
            }

            setBackground(template);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void setBackground(JSONObject template){
        if (template.has("backgroundImage")) {
            JSONObject backgroundImage = null;
            try {
                backgroundImage = template.getJSONObject("backgroundImage");
                if (backgroundImage.has("sources")) {
                    JSONArray sources = backgroundImage.getJSONArray("sources");
                    JSONObject source = (JSONObject) sources.get(0);
                    if (source.has("url")) {
                        String url = source.getString("url");
                        log.e("url :" + url);
                        RequestOptions options = new RequestOptions().override(1920, 1080).centerCrop();
                        Glide.with(getApplicationContext())
                                .load(url)
//                                .apply(options)
                                .into(photo);
//                                .into(new SimpleTarget<Drawable>() {
//                                    @Override
//                                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
//                                        log.e("resource  :" + resource);
//                                        photo.setImageDrawable(resource);
//                                    }
//                                });
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
