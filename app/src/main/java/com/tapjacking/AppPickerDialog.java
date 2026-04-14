package com.tapjacking;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AppPickerDialog {

    public interface OnSelectedListener {
        void onSelected(String packageName, String activityName);
    }

    private final Context context;
    private final OnSelectedListener listener;
    private final PackageManager pm;
    private List<AppItem> cachedApps;

    public AppPickerDialog(Context context, OnSelectedListener listener) {
        this.context = context;
        this.listener = listener;
        this.pm = context.getPackageManager();
    }

    public void show() {
        if (cachedApps != null) {
            showAppList(cachedApps);
            return;
        }

        AlertDialog loading = new AlertDialog.Builder(context)
                .setMessage("Loading apps...")
                .setCancelable(false)
                .create();
        loading.show();

        new Thread(() -> {
            List<AppItem> apps = loadApps();
            new Handler(Looper.getMainLooper()).post(() -> {
                loading.dismiss();
                cachedApps = apps;
                showAppList(apps);
            });
        }).start();
    }

    private List<AppItem> loadApps() {
        List<ApplicationInfo> installed = pm.getInstalledApplications(0);
        List<AppItem> result = new ArrayList<>();

        for (ApplicationInfo info : installed) {
            try {
                PackageInfo pkgInfo = pm.getPackageInfo(info.packageName, PackageManager.GET_ACTIVITIES);
                if (pkgInfo.activities == null) continue;

                boolean hasExported = false;
                for (ActivityInfo act : pkgInfo.activities) {
                    if (act.exported) { hasExported = true; break; }
                }
                if (!hasExported) continue;

            } catch (PackageManager.NameNotFoundException e) {
                continue;
            }

            String name = pm.getApplicationLabel(info).toString();
            Drawable icon = pm.getApplicationIcon(info);
            result.add(new AppItem(name, info.packageName, icon));
        }

        result.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
        return result;
    }

    private void showAppList(List<AppItem> apps) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_picker, null);
        EditText search = view.findViewById(R.id.searchField);
        ListView listView = view.findViewById(R.id.listView);

        AppAdapter adapter = new AppAdapter(context, apps);
        listView.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Select app")
                .setView(view)
                .setNegativeButton("Cancel", null)
                .create();

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
        });

        listView.setOnItemClickListener((parent, v, position, id) -> {
            String packageName = adapter.getItem(position).packageName;
            dialog.dismiss();
            showActivityList(packageName);
        });

        dialog.show();
    }

    private void showActivityList(String packageName) {
        List<String> activities = new ArrayList<>();
        try {
            PackageInfo pkgInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            if (pkgInfo.activities != null) {
                for (ActivityInfo act : pkgInfo.activities) {
                    if (act.exported) activities.add(act.name);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            return;
        }

        if (activities.isEmpty()) return;

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_picker, null);
        EditText search = view.findViewById(R.id.searchField);
        ListView listView = view.findViewById(R.id.listView);

        ActivityAdapter adapter = new ActivityAdapter(context, activities);
        listView.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(packageName)
                .setView(view)
                .setNegativeButton("Back", (d, w) -> showAppList(cachedApps))
                .create();

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
        });

        listView.setOnItemClickListener((parent, v, position, id) -> {
            String activityName = adapter.getItem(position);
            dialog.dismiss();
            listener.onSelected(packageName, activityName);
        });

        dialog.show();
    }

    // --- Models & Adapters ---

    static class AppItem {
        final String name, packageName;
        final Drawable icon;
        AppItem(String name, String packageName, Drawable icon) {
            this.name = name;
            this.packageName = packageName;
            this.icon = icon;
        }
    }

    static class AppAdapter extends ArrayAdapter<AppItem> {
        private final List<AppItem> all;

        AppAdapter(Context ctx, List<AppItem> items) {
            super(ctx, 0, new ArrayList<>(items));
            this.all = new ArrayList<>(items);
        }

        void filter(String query) {
            clear();
            if (query.isEmpty()) {
                addAll(all);
            } else {
                String q = query.toLowerCase();
                for (AppItem item : all) {
                    if (item.name.toLowerCase().contains(q) || item.packageName.toLowerCase().contains(q)) {
                        add(item);
                    }
                }
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_app, parent, false);
            }
            AppItem item = getItem(position);
            ((ImageView) convertView.findViewById(R.id.appIcon)).setImageDrawable(item.icon);
            ((TextView) convertView.findViewById(R.id.appName)).setText(item.name);
            ((TextView) convertView.findViewById(R.id.packageName)).setText(item.packageName);
            return convertView;
        }
    }

    static class ActivityAdapter extends ArrayAdapter<String> {
        private final List<String> all;

        ActivityAdapter(Context ctx, List<String> items) {
            super(ctx, android.R.layout.simple_list_item_2, new ArrayList<>(items));
            this.all = new ArrayList<>(items);
        }

        void filter(String query) {
            clear();
            if (query.isEmpty()) {
                addAll(all);
            } else {
                String q = query.toLowerCase();
                for (String item : all) {
                    if (item.toLowerCase().contains(q)) add(item);
                }
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            }
            String fullName = getItem(position);
            String shortName = fullName.contains(".") ? fullName.substring(fullName.lastIndexOf('.') + 1) : fullName;
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(shortName);
            ((TextView) convertView.findViewById(android.R.id.text2)).setText(fullName);
            return convertView;
        }
    }
}
