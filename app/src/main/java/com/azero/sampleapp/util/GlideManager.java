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

package com.azero.sampleapp.util;

import android.content.Context;
import android.widget.ImageView;

import com.azero.sampleapp.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import jp.wasabeef.glide.transformations.BlurTransformation;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class GlideManager {

    public static void loadImg(Context context, String url, ImageView imageView){
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.img_load_default)
                .error(R.drawable.img_load_default)
                .override(300,300)
                .centerInside()
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
        Glide.with(context).load(url).apply(options).into(imageView);
    }
    public static void loadImg(Context context, String url, ImageView imageView,int width,int height){
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.img_load_default)
                .error(R.drawable.img_load_default)
                .override(width,height)
                .centerInside()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
        Glide.with(context).load(url).apply(options).into(imageView);
    }

    /**
     * 高斯模糊
     * @param context
     * @param url
     * @param imageView
     */
    public static void loadImgWithBlur(Context context, String url, ImageView imageView){
        RequestOptions options = new RequestOptions()
                .override(300, 300)
                .centerInside()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
            //加载背景，虚化值从1到25，值越大虚化程度越高
            Glide.with(context).load(url).apply(options)
                    .apply(bitmapTransform(new BlurTransformation(4, 23))).into(imageView);
    }

    public static void loadImgWithBlur(Context context, int resId, ImageView imageView){
        RequestOptions options = new RequestOptions()
                .override(300,300)
                .centerInside()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
        //加载背景，虚化值从1到25，值越大虚化程度越高
        Glide.with(context).load(resId).apply(options)
                .apply(bitmapTransform(new BlurTransformation(4, 23))).into(imageView);
    }

}
