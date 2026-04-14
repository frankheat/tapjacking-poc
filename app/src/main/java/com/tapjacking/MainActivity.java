package com.tapjacking;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private EditText inputPackage, inputActivity, inputDeepLink;
    private RadioButton radioStartActivity, radioDeepLink, radioFull, radioPartial;
    private Button btnBrowse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestNotificationPermission();

        radioStartActivity = findViewById(R.id.radioStartActivity);
        radioDeepLink = findViewById(R.id.radioDeepLink);
        radioFull = findViewById(R.id.radioFull);
        radioPartial = findViewById(R.id.radioPartial);
        Button btnStart = findViewById(R.id.btnStart);
        Button btnStop = findViewById(R.id.btnStop);

        inputPackage = findViewById(R.id.inputPackage);
        inputActivity = findViewById(R.id.inputActivity);
        inputDeepLink = findViewById(R.id.inputDeepLink);
        btnBrowse = findViewById(R.id.btnBrowse);

        // Start Activity / Deep Link option
        RadioGroup launchOptionGroup = findViewById(R.id.launchOptionGroup);
        launchOptionGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioStartActivity) {
                inputPackage.setVisibility(View.VISIBLE);
                inputActivity.setVisibility(View.VISIBLE);
                btnBrowse.setVisibility(View.VISIBLE);
                inputDeepLink.setVisibility(View.GONE);
            } else {
                inputPackage.setVisibility(View.GONE);
                inputActivity.setVisibility(View.GONE);
                btnBrowse.setVisibility(View.GONE);
                inputDeepLink.setVisibility(View.VISIBLE);
            }
        });

        // Browse button
        btnBrowse.setOnClickListener(v -> new AppPickerDialog(this, (pkg, act) -> {
            inputPackage.setText(pkg);
            inputActivity.setText(act);
        }).show());

        // START button
        btnStart.setOnClickListener(v -> {
            if (!checkFields()) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            } else {
                if (!Settings.canDrawOverlays(this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                    return;
                }

                String pkg = inputPackage.getText().toString().trim();
                String act = inputActivity.getText().toString().trim();
                String link = inputDeepLink.getText().toString().trim();

                if (radioStartActivity.isChecked()) {
                    Intent intent = new Intent();
                    intent.setClassName(pkg, act);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(this, "Unable to start the requested activity.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else if (radioDeepLink.isChecked()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(this, "Unable to start the deep link.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (radioFull.isChecked()) {
                    startService(new Intent(this, OverlayService.class));
                    btnStop.setVisibility(View.VISIBLE);
                }

                if (radioPartial.isChecked()) {
                    startService(new Intent(this, SelectionOverlayService.class));
                }
            }
        });

        // Stop button
        btnStop.setOnClickListener(v -> {
            stopService(new Intent(getApplicationContext(), OverlayService.class));
            stopService(new Intent(getApplicationContext(), PartialOverlayService.class));
            stopService(new Intent(getApplicationContext(), SelectionOverlayService.class));
            btnStop.setVisibility(View.GONE);
        });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
        }
    }

    private boolean checkFields() {
        boolean firstStep = false;

        if (radioStartActivity.isChecked()
                && !inputPackage.getText().toString().isEmpty()
                && !inputActivity.getText().toString().isEmpty()) {
            firstStep = true;
        } else if (radioDeepLink.isChecked() && !inputDeepLink.getText().toString().isEmpty()) {
            firstStep = true;
        }

        boolean secondStep = radioFull.isChecked() || radioPartial.isChecked();

        return firstStep && secondStep;
    }
}
