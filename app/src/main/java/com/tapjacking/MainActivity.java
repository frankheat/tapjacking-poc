package com.tapjacking;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText inputPackage, inputActivity, inputDeepLink, inputHeight, inputWidth;
    private LinearLayout partialSizeLayout;
    private RadioButton radioStartActivity, radioDeepLink, radioFull, radioPartial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        radioStartActivity = findViewById(R.id.radioStartActivity);
        radioDeepLink = findViewById(R.id.radioDeepLink);
        radioFull = findViewById(R.id.radioFull);
        radioPartial = findViewById(R.id.radioPartial);
        Button btnStart = findViewById(R.id.btnStart);
        Button btnStop = findViewById(R.id.btnStop);

        inputPackage = findViewById(R.id.inputPackage);
        inputActivity = findViewById(R.id.inputActivity);
        inputDeepLink = findViewById(R.id.inputDeepLink);
        inputHeight = findViewById(R.id.inputHeight);
        inputWidth = findViewById(R.id.inputWidth);
        partialSizeLayout = findViewById(R.id.partialSizeLayout);

        // Start Activity / Deep Link option
        RadioGroup launchOptionGroup = findViewById(R.id.launchOptionGroup);
        launchOptionGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioStartActivity) {
                inputPackage.setVisibility(View.VISIBLE);
                inputActivity.setVisibility(View.VISIBLE);
                inputDeepLink.setVisibility(View.GONE);
            } else {
                inputPackage.setVisibility(View.GONE);
                inputActivity.setVisibility(View.GONE);
                inputDeepLink.setVisibility(View.VISIBLE);
            }
        });

        // FULL / PARTIAL
        RadioGroup overlayOptionGroup = findViewById(R.id.overlayOptionGroup);
        overlayOptionGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioPartial) {
                partialSizeLayout.setVisibility(View.VISIBLE);
            } else {
                partialSizeLayout.setVisibility(View.GONE);
            }
        });

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

                // Full-screen overlay service start-up
                if (radioFull.isChecked()) {
                    Intent overlayServiceIntent = new Intent(this, OverlayService.class);
                    startService(overlayServiceIntent);
                    btnStop.setVisibility(View.VISIBLE);
                }

                // Partial overlay service start-up
                if (radioPartial.isChecked()) {
                    int heightPercent = Integer.parseInt(inputHeight.getText().toString());
                    int widthPercent = Integer.parseInt(inputWidth.getText().toString());

                    Intent partialOverlayIntent = new Intent(this, PartialOverlayService.class);
                    partialOverlayIntent.putExtra("height", heightPercent);
                    partialOverlayIntent.putExtra("width", widthPercent);
                    startService(partialOverlayIntent);
                }

            }
        });

        // Stop button
        btnStop.setOnClickListener(v -> {
            stopService(new Intent(getApplicationContext(), OverlayService.class));
            btnStop.setVisibility(View.GONE);
        });
    }


    private boolean checkFields() {
        boolean firstStep = false;
        boolean secondStep = false;

        if (radioStartActivity.isChecked() && !(inputPackage.getText().toString().isEmpty() || inputActivity.getText().toString().isEmpty()) ) {
            firstStep = true;
        } else if (radioDeepLink.isChecked() && !inputDeepLink.getText().toString().isEmpty()) {
            firstStep = true;
        }

        if (radioFull.isChecked()) {
            secondStep = true;
        } else if (radioPartial.isChecked()) {
            if (!inputHeight.getText().toString().isEmpty() && !inputWidth.getText().toString().isEmpty()) {
                secondStep = true;
            }
        }

        return firstStep && secondStep;
    }
}

