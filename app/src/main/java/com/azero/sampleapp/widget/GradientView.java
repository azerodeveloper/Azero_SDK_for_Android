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

package com.azero.sampleapp.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.azero.sampleapp.R;

public class GradientView extends View {

    public GradientView(Context context) {
        super(context);
    }

    public GradientView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GradientView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //获取控件宽高
        int width = getWidth();
        int height = getHeight();

        //设置渐变色
        int colorStart = getResources().getColor(R.color.gradientColor1);
        int color1 = getResources().getColor(R.color.gradientColor2);
        int color2 = getResources().getColor(R.color.gradientColor2);
        int colorEnd = getResources().getColor(R.color.gradientColor3);

        Paint paint = new Paint();
        LinearGradient backGradient = new LinearGradient(0, 0, width, 0, new int[]{colorStart, color1, color2, colorEnd}, new float[]{0f, 0.3f, 0.7f, 1.0f}, Shader.TileMode.CLAMP);
        paint.setShader(backGradient);
        canvas.drawRect(0, 0, width, height, paint);
    }
}
