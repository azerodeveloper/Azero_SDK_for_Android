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

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Hashtable;

public class QRCUtils {

    /**
     * @param content              字符串内容
     * @param width                二维码宽度
     * @param height               二维码高度
     * @param charSet             编码方式（一般为UTF-8）
     * @param errCorrectionLevel 容错率（L:7% M:15% Q:25% H:35%）
     * @param margin               空白边距
     * @param colorBlack          黑色色块
     * @param colorWhite          白色色块
     */
    public static Bitmap createQrCodeBitmap(String content, int width, int height,
                                            String charSet, String errCorrectionLevel,
                                            String margin, int colorBlack, int colorWhite) {

        if (TextUtils.isEmpty(content)) {
            return null;
        }
        if (width < 0 || height < 0) {
            return null;
        }
        try {
            //1.设置二维码相关配置
            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
            //字符串转码格式
            if (!TextUtils.isEmpty(charSet)) {
                hints.put(EncodeHintType.CHARACTER_SET, charSet);
            }
            if (!TextUtils.isEmpty(errCorrectionLevel))
                hints.put(EncodeHintType.ERROR_CORRECTION, errCorrectionLevel);
            if (!TextUtils.isEmpty(margin))
                hints.put(EncodeHintType.MARGIN, margin);
            //2.将配置参数传入到QRCodeWriter的encode方法生成BitMatrix 对象
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            //3.创建像素数组，并根据BitMatrix对象为数组元素赋值颜色
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y))
                        pixels[y * width + x] = colorBlack;
                    else
                        pixels[y * width + x] = colorWhite;
                }
            }
            //4.创建bitmap对象，根据像素数组设置Bitmap每个像素点的颜色值，并返回bitmap对象
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
