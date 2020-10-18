package com.samansepahvand.myapplication;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class Application extends android.app.Application {


    @Override
    public void onCreate() {
        super.onCreate();
            CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                    .setDefaultFontPath("font/fonts.ttf")
                    .setFontAttrId(R.attr.fontPath)
                    .build()
            );

        }

}
