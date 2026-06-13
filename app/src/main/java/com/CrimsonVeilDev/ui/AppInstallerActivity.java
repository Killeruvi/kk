package com.CrimsonVeilDev.ui;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.CrimsonVeilDev.R;
import top.niunaijun.blackbox.BlackBoxCore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppInstallerActivity extends AppCompatActivity {

    private AppAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_installer);

        RecyclerView appRecyclerView = findViewById(R.id.appRecyclerView);
        appRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        PackageManager hostPm = getPackageManager();
        PackageManager virtualPm = BlackBoxCore.get().getPackageManager();
        
        List<ApplicationInfo> hostApps = hostPm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<ApplicationInfo> virtualApps = virtualPm.getInstalledApplications(PackageManager.GET_META_DATA);
        
        Set<String> virtualPackageNames = new HashSet<>();
        for (ApplicationInfo info : virtualApps) {
            virtualPackageNames.add(info.packageName);
        }
        
        List<AppModel> appList = new ArrayList<>();
        for (ApplicationInfo appInfo : hostApps) {
            // Filter system apps
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                appList.add(new AppModel(appInfo, virtualPackageNames.contains(appInfo.packageName)));
            }
        }

        adapter = new AppAdapter(this, appList);
        appRecyclerView.setAdapter(adapter);

        SearchView searchView = findViewById(R.id.appSearchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }
}
