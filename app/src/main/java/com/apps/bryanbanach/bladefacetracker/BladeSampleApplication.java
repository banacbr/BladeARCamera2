package com.apps.bryanbanach.bladefacetracker;

import com.vuzix.hud.resources.DynamicThemeApplication;

public class BladeSampleApplication extends DynamicThemeApplication {
    @Override
    protected int getNormalThemeResId(){
        return R.style.AppTheme;
    }

    @Override
    protected int getLightThemeResId(){
        return R.style.AppTheme_Light;
    }
}
