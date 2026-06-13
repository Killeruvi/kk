package com.CrimsonVeilDev.ui;

import android.content.pm.ApplicationInfo;

public class AppModel {
    public ApplicationInfo appInfo;
    public boolean isInstalled;

    public AppModel(ApplicationInfo appInfo, boolean isInstalled) {
        this.appInfo = appInfo;
        this.isInstalled = isInstalled;
    }
}
