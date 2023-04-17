package com.smileidentity.sample.activity;

import static com.smileidentity.UtilKt.randomUserId;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.smileidentity.fragment.SmartSelfieAuthenticationFragment;
import com.smileidentity.fragment.SmartSelfieRegistrationFragment;
import com.smileidentity.results.SmartSelfieResult;

import timber.log.Timber;

public class JavaActivity extends FragmentActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "Java Activity", Toast.LENGTH_LONG).show();
        // TODO: Show buttons to start SmartSelfieRegistration and SmartSelfieAuthentication
    }

    private void doSmartSelfieRegistration() {
        String userId = randomUserId();
        boolean allowAgentMode = false;
        SmartSelfieRegistrationFragment smartSelfieFragment = SmartSelfieRegistrationFragment.newInstance(
            userId, allowAgentMode
        );
        getSupportFragmentManager().setFragmentResultListener(
            SmartSelfieRegistrationFragment.KEY_REQUEST,
            this,
            (requestKey, result) -> {
                SmartSelfieResult smartSelfieResult = SmartSelfieRegistrationFragment.resultFromBundle(result);
                Timber.v("SmartSelfieRegistration Result: %s", smartSelfieResult);
                getSupportFragmentManager()
                    .beginTransaction()
                    .remove(smartSelfieFragment)
                    .commit();
            }
        );

        getSupportFragmentManager()
            .beginTransaction()
            .replace(android.R.id.content, smartSelfieFragment)
            .commit();
    }

    private void doSmartSelfieAuthentication() {
        String userId = randomUserId();
        boolean allowAgentMode = false;
        SmartSelfieAuthenticationFragment smartSelfieFragment = SmartSelfieAuthenticationFragment
            .newInstance(userId, allowAgentMode);
        getSupportFragmentManager().setFragmentResultListener(
            SmartSelfieRegistrationFragment.KEY_REQUEST,
            this,
            (requestKey, result) -> {
                SmartSelfieResult smartSelfieResult = SmartSelfieAuthenticationFragment.resultFromBundle(result);
                Timber.v("SmartSelfieAuthentication Result: %s", smartSelfieResult);
                getSupportFragmentManager()
                    .beginTransaction()
                    .remove(smartSelfieFragment)
                    .commit();
            }
        );

        getSupportFragmentManager()
            .beginTransaction()
            .replace(android.R.id.content, smartSelfieFragment)
            .commit();
    }
}
