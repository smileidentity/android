package com.smileidentity.sample;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.smileidentity.ui.core.SmartSelfieResult;
import com.smileidentity.ui.core.SmartSelfieCallback;
import com.smileidentity.ui.fragment.SmartSelfieFragment;

import timber.log.Timber;

public class JavaActivity extends AppCompatActivity implements SmartSelfieCallback {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "Java Activity", Toast.LENGTH_SHORT).show();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, SmartSelfieFragment.newInstance(this))
                .commit();
    }

    @Override
    public void onResult(@NonNull SmartSelfieResult result) {
        Timber.v("SmartSelfieResult: %s", result);
        Toast.makeText(this, "SmartSelfieResult " + result, Toast.LENGTH_SHORT).show();
    }
}
