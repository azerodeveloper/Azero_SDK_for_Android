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

package com.azero.sampleapp.activity.alert;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.azero.sampleapp.R;
import com.azero.sampleapp.activity.alert.bean.AlertInfo;

import java.util.List;

/**
 * @Author: sjy
 * @Date: 2019/8/31
 */

public class AlertsAdapter extends BaseRecyclerViewAdapter<AlertInfo> {

    private OnDeleteClickListener mOnDeleteClickListener;
    private Context mContext;
    private List<AlertInfo> mData;
    private int mLayoutId;

    private long mLastClickTime = 0;
    public static final long TIME_INTERVAL = 1000L;

    public AlertsAdapter(Context context, List<AlertInfo> data) {
        super(context, data, R.layout.card_alert_template_item);
    }

    @Override
    protected void onBindData(RecyclerViewHolder holder, AlertInfo bean, int position) {
        View deleteView = holder.getView(R.id.alert_delete);
        if (!deleteView.hasOnClickListeners()) {
            deleteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    long nowTime = System.currentTimeMillis();
                    if (nowTime - mLastClickTime > TIME_INTERVAL) {
                        if (mOnDeleteClickListener != null) {
                            mLastClickTime = nowTime;
                            mOnDeleteClickListener.onDeleteClick(bean.getAlertId());
                        }
                    }
                }
            });
        }
        holder.getView(R.id.alert_item_view).setBackgroundResource(R.color.alert_item_background);
        holder.getView(R.id.alert_item_view).getBackground().setAlpha(0);
        ((TextView) holder.getView(R.id.textView_index)).setText(bean.getIndex().toString());
        ((TextView) holder.getView(R.id.textView_event)).setText(bean.getEvent().toString());
        ((TextView) holder.getView(R.id.textView_triggerTime)).setText(bean.getTriggerTime().toString());

    }

    public void setOnDeleteClickListener(OnDeleteClickListener onDeleteClickListener) {
        mOnDeleteClickListener = onDeleteClickListener;
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(String alertToken);
    }
}
