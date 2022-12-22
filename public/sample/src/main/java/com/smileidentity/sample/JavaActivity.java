package com.smileidentity.sample;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.smileidentity.ui.SelfieCaptureResult;
import com.smileidentity.ui.SelfieCaptureResultCallback;
import com.smileidentity.ui.SelfieFragment;

import timber.log.Timber;

public class JavaActivity extends AppCompatActivity implements SelfieCaptureResultCallback {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "Java Activity", Toast.LENGTH_SHORT).show();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, SelfieFragment.newInstance(this), "SelfieFragment")
                .commit();
    }

    @Override
    public void onResult(@NonNull SelfieCaptureResult result) {
        Timber.v("SelfieCaptureResult: %s", result);
        Toast.makeText(this, "SelfieCaptureResult " + result, Toast.LENGTH_SHORT).show();
    }
}
