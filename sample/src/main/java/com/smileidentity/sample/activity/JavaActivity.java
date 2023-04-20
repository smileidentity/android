package com.smileidentity.sample.activity;

import static com.smileidentity.UtilKt.randomUserId;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.smileidentity.fragment.SmartSelfieAuthenticationFragment;
import com.smileidentity.fragment.SmartSelfieRegistrationFragment;
import com.smileidentity.models.JobStatusResponse;
import com.smileidentity.results.SmartSelfieResult;

import java.io.File;
import java.util.List;

import timber.log.Timber;

/**
 * This is an example of how to use the Smile ID SDK in a Java Activity. It is not actively used in
 * the sample app, but is provided as a reference.
 */
@SuppressLint("LogNotTimber") // Don't use Timber in Documentation or Examples
public class JavaActivity extends FragmentActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "Java Activity", Toast.LENGTH_LONG).show();
        // TODO: Show buttons to start SmartSelfieRegistration and SmartSelfieAuthentication
    }

    private void doSmartSelfieRegistration() {
        SmartSelfieRegistrationFragment smartSelfieFragment = SmartSelfieRegistrationFragment
            .newInstance();
        getSupportFragmentManager().setFragmentResultListener(
            SmartSelfieRegistrationFragment.KEY_REQUEST,
            this,
            (requestKey, result) -> {
                SmartSelfieResult smartSelfieResult = SmartSelfieRegistrationFragment
                    .resultFromBundle(result);
                Log.v("SmartSelfieRegistration", "Result: " + smartSelfieResult);
                if (smartSelfieResult instanceof SmartSelfieResult.Success successResult) {
                    File selfieFile = successResult.getSelfieFile();
                    List<File> livenessFiles = successResult.getLivenessFiles();
                    JobStatusResponse jobStatusResponse = successResult.getJobStatusResponse();
                    // Note: Although the API submission is successful, the job status response
                    // may indicate that the job is still in progress or failed. You should
                    // check the job status response to determine the final status of the job.
                    if (jobStatusResponse.getJobSuccess()) {
                        Log.v("SmartSelfieRegistration", "Job Success");
                    } else if (!jobStatusResponse.getJobComplete()) {
                        Log.v("SmartSelfieRegistration", "Job Not Complete");
                    } else {
                        Log.v("SmartSelfieRegistration", "Job Failed");
                    }
                } else if (smartSelfieResult instanceof SmartSelfieResult.Error error) {
                    Throwable throwable = error.getThrowable();
                    Log.v("SmartSelfieRegistration", "Error: " + throwable.getMessage());
                }
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
