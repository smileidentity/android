package com.smileidentity.sample.activity;

import static com.smileidentity.UtilKt.randomUserId;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.smileidentity.fragment.SmartSelfieAuthenticationFragment;
import com.smileidentity.fragment.SmartSelfieRegistrationFragment;
import com.smileidentity.models.JobStatusResponse;
import com.smileidentity.results.SmartSelfieResult;
import com.smileidentity.results.SmileIDResult;
import com.smileidentity.sample.R;

import java.io.File;
import java.util.List;

import timber.log.Timber;

/**
 * This is an example of how to use the Smile ID SDK in a Java Activity. It is not actively used in
 * the sample app, but is provided as a reference.
 */
public class JavaActivity extends FragmentActivity {
    private View productFragmentContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java);
        Toast.makeText(this, "Java Activity", Toast.LENGTH_LONG).show();
        productFragmentContainer = findViewById(R.id.fragment_container);
        hideProductFragment();
        findViewById(R.id.button_smart_selfie_authentication)
            .setOnClickListener(v -> doSmartSelfieAuthentication());
        findViewById(R.id.button_smart_selfie_registration)
            .setOnClickListener(v -> doSmartSelfieRegistration());
    }

    private void doSmartSelfieRegistration() {
        SmartSelfieRegistrationFragment smartSelfieFragment = SmartSelfieRegistrationFragment
            .newInstance();
        getSupportFragmentManager().setFragmentResultListener(
            SmartSelfieRegistrationFragment.KEY_REQUEST,
            this,
            (requestKey, result) -> {
                SmileIDResult<SmartSelfieResult> smartSelfieResult =
                    SmartSelfieRegistrationFragment.resultFromBundle(result);
                Timber.v("SmartSelfieRegistration Result: %s", smartSelfieResult);
                if (smartSelfieResult instanceof SmileIDResult.Success<SmartSelfieResult> successResult) {
                    File selfieFile = successResult.getData().getSelfieFile();
                    List<File> livenessFiles = successResult.getData().getLivenessFiles();
                    JobStatusResponse jobStatusResponse = successResult.getData().getJobStatusResponse();
                    // Note: Although the API submission is successful, the job status response
                    // may indicate that the job is still in progress or failed. You should
                    // check the job status response to determine the final status of the job.
                    if (jobStatusResponse.getJobSuccess()) {
                        Timber.v("SmartSelfieRegistration Job Success");
                    } else if (!jobStatusResponse.getJobComplete()) {
                        Timber.v("SmartSelfieRegistration Job Not Complete");
                    } else {
                        Timber.v("SmartSelfieRegistration Job Failed");
                    }
                } else if (smartSelfieResult instanceof SmileIDResult.Error error) {
                    Throwable throwable = error.getThrowable();
                    Timber.v("SmartSelfieRegistration Error: %s", throwable.getMessage());
                }
                getSupportFragmentManager()
                    .beginTransaction()
                    .remove(smartSelfieFragment)
                    .commit();
                hideProductFragment();
            }
        );

        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, smartSelfieFragment)
            .commit();
        showProductFragment();
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
                SmileIDResult<SmartSelfieResult> smartSelfieResult =
                    SmartSelfieAuthenticationFragment.resultFromBundle(result);
                Timber.v("SmartSelfieAuthentication Result: %s", smartSelfieResult);
                getSupportFragmentManager()
                    .beginTransaction()
                    .remove(smartSelfieFragment)
                    .commit();
                hideProductFragment();
            }
        );

        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, smartSelfieFragment)
            .commit();
        showProductFragment();
    }

    private void showProductFragment() {
        productFragmentContainer.setVisibility(View.VISIBLE);
    }

    private void hideProductFragment() {
        productFragmentContainer.setVisibility(View.GONE);
    }
}
