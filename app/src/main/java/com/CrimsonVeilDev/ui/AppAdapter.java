package com.CrimsonVeilDev.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.CrimsonVeilDev.R;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.entity.pm.InstallOption;

import java.util.ArrayList;
import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> implements Filterable {

    private final Context context;
    private List<AppModel> appList;
    private List<AppModel> appListFull;
    private final PackageManager pm;

    public AppAdapter(Context context, List<AppModel> appList) {
        this.context = context;
        this.appList = appList;
        this.appListFull = new ArrayList<>(appList);
        this.pm = context.getPackageManager();
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_app_list, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppModel appModel = appList.get(position);
        holder.appName.setText(appModel.appInfo.loadLabel(pm));
        holder.appIcon.setImageDrawable(appModel.appInfo.loadIcon(pm));
        
        if (appModel.isInstalled) {
            holder.btnInstall.setVisibility(View.GONE);
            holder.btnLaunch.setVisibility(View.VISIBLE);
            holder.btnUninstall.setVisibility(View.VISIBLE);

            holder.btnLaunch.setOnClickListener(v -> {
                // Correct: Use BActivityManager directly
                Intent intent = pm.getLaunchIntentForPackage(appModel.appInfo.packageName);
                if (intent != null) {
                    top.niunaijun.blackbox.fake.frameworks.BActivityManager.get().startActivity(intent, 0);
                    Toast.makeText(context, "Launching in virtual: " + appModel.appInfo.loadLabel(pm), Toast.LENGTH_SHORT).show();
                }
            });

            holder.btnUninstall.setOnClickListener(v -> {
                // Uninstall via BlackBoxCore
                BlackBoxCore.get().uninstallPackageAsUser(appModel.appInfo.packageName, 0);
                appModel.isInstalled = false;
                notifyItemChanged(position);
                Toast.makeText(context, "Uninstalled from virtual: " + appModel.appInfo.loadLabel(pm), Toast.LENGTH_SHORT).show();
            });
        } else {
            holder.btnInstall.setVisibility(View.VISIBLE);
            holder.btnLaunch.setVisibility(View.GONE);
            holder.btnUninstall.setVisibility(View.GONE);

            holder.btnInstall.setOnClickListener(v -> {
                String path = appModel.appInfo.sourceDir;
                // Correct: Access the BPackageManager service
                BlackBoxCore.get().getBPackageManager().installPackageAsUser(path, InstallOption.installByStorage(), 0);
                appModel.isInstalled = true;
                notifyItemChanged(position);
                Toast.makeText(context, "Installed " + appModel.appInfo.loadLabel(pm), Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    @Override
    public Filter getFilter() {
        return appFilter;
    }

    private final Filter appFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<AppModel> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(appListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (AppModel item : appListFull) {
                    if (item.appInfo.loadLabel(pm).toString().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            appList.clear();
            appList.addAll((List<AppModel>) results.values);
            notifyDataSetChanged();
        }
    };

    static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        Button btnLaunch, btnUninstall, btnInstall;

        AppViewHolder(View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.appIcon);
            appName = itemView.findViewById(R.id.appName);
            btnLaunch = itemView.findViewById(R.id.btnLaunch);
            btnUninstall = itemView.findViewById(R.id.btnUninstall);
            btnInstall = itemView.findViewById(R.id.btnInstall); // Need to add this in XML
        }
    }
}
