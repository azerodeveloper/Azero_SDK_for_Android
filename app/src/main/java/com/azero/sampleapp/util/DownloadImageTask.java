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
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.azero.sampleapp.R;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    private static final String sTag = "CLI";
    private final WeakReference<ImageView> mImage;

    public DownloadImageTask(ImageView image ) { mImage = new WeakReference<>( image ); }

    protected Bitmap doInBackground( String... urls ) {
        String urlDisplay = urls[ 0 ];
        Bitmap mIcon11 = null;

        try ( InputStream in = new java.net.URL( urlDisplay ).openStream() ) {
            mIcon11 = BitmapFactory.decodeStream( in );
        } catch ( IOException e ) {
            Log.e( sTag, e.getMessage() );
        }

        return mIcon11;
    }

    protected void onPostExecute( Bitmap result )
    {
        if(result!=null){
            mImage.get().setImageBitmap( result );
        }else{
            mImage.get().setImageResource(R.mipmap.default_img);
        }

    }
}
