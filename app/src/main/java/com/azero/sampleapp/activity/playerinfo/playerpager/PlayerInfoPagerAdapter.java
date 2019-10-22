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
package com.azero.sampleapp.activity.playerinfo.playerpager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class PlayerInfoPagerAdapter extends FragmentStatePagerAdapter {
    private static  int PAGE_COUNT = 1;
    public BasePlayerInfoFragment[] fragments = new BasePlayerInfoFragment[2];
    private List<BasePlayerInfoFragment> fragmentList = new ArrayList<>();
    private String template;
    public PlayerInfoPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setFragments(List<BasePlayerInfoFragment> fragmentList){
        this.fragmentList = fragmentList;
    }

    public void setPageCount(int pageCount){
        PAGE_COUNT = pageCount;
    }

    public void setTemplate(String template){
        this.template = template;
    }

    @Override
    public Fragment getItem(int position) {
       if(fragments[position]==null){
         switch (position){
             case 0:
                 fragments[position] = PlayerInfoFragment.newInstance(template);
                 break;
             case 1:
                 fragments[position] = PlayerInfoLyricFragment.newInstance(template);
                 break;
         }
        }
        return fragments[position];
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public int getItemPosition(Object object) {
        if(PAGE_COUNT==2){
            return POSITION_UNCHANGED;
        }else{
            return POSITION_NONE;
        }
    }
}
