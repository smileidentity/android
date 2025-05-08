package com.smileidentity.sample.activity;

import static com.smileidentity.util.UtilKt.randomJobId;
import static com.smileidentity.util.UtilKt.randomUserId;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.smileidentity.SmileID;
import com.smileidentity.fragment.BiometricKYCFragment;
import com.smileidentity.fragment.DocumentVerificationFragment;
import com.smileidentity.fragment.EnhancedDocumentVerificationFragment;
import com.smileidentity.fragment.EnhancedSmartSelfieAuthenticationFragment;
import com.smileidentity.fragment.EnhancedSmartSelfieAuthenticationFragmentKt;
import com.smileidentity.fragment.EnhancedSmartSelfieEnrollmentFragment;
import com.smileidentity.fragment.SmartSelfieAuthenticationFragment;
import com.smileidentity.fragment.SmartSelfieEnrollmentFragment;
import com.smileidentity.models.ConsentInformation;
import com.smileidentity.models.ConsentedInformation;
import com.smileidentity.models.IdInfo;
import com.smileidentity.results.BiometricKycResult;
import com.smileidentity.results.DocumentVerificationResult;
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
        SmileID.initialize(this);
        setContentView(R.layout.activity_java);
        Toast.makeText(this, "Java Activity", Toast.LENGTH_LONG).show();
        productFragmentContainer = findViewById(R.id.fragment_container);
        hideProductFragment();
        findViewById(R.id.button_smart_selfie_authentication)
            .setOnClickListener(v -> doSmartSelfieAuthentication());
        findViewById(R.id.button_enhanced_smart_selfie_authentication)
            .setOnClickListener(v -> doEnhancedSmartSelfieAuthentication());
        findViewById(R.id.button_smart_selfie_enrollment)
            .setOnClickListener(v -> doSmartSelfieEnrollment());
        findViewById(R.id.button_enhanced_smart_selfie_enrollment)
            .setOnClickListener(v -> doEnhancedSmartSelfieEnrollment());
        findViewById(R.id.button_biometric_authentication)
            .setOnClickListener(v -> doBiometricKYC());
        findViewById(R.id.button_document_verification)
            .setOnClickListener(v -> doDocumentVerification());
        findViewById(R.id.button_enhanced_document_verification)
            .setOnClickListener(v -> doEnhancedDocumentVerification());
    }

    private void doBiometricKYC() {
        IdInfo idInfo = new IdInfo("GH", "PASSPORT", "1234567890",
            null, null, null, null, null, null);
        ConsentedInformation consentedInfo = new ConsentedInformation(
            "", true, true, true
        );
        ConsentInformation consentInformation = new ConsentInformation(consentedInfo);
        BiometricKYCFragment biometricKYCFragment = BiometricKYCFragment
            .newInstance(idInfo, consentInformation);
        getSupportFragmentManager().setFragmentResultListener(
            BiometricKYCFragment.KEY_REQUEST,
            this,
            (requestKey, result) -> {
                SmileIDResult<BiometricKycResult> biometricKycResult =
                    BiometricKYCFragment.resultFromBundle(result);
                Timber.v("BiometricKYCFragment Result: %s", biometricKycResult);
                if (biometricKycResult instanceof SmileIDResult.Success<BiometricKycResult> successResult) {
                    File selfieFile = successResult.getData().getSelfieFile();
                    List<File> livenessFiles = successResult.getData().getLivenessFiles();
                    boolean jobSubmitted = successResult.getData().getDidSubmitBiometricKycJob();
                    // When offline mode is enabled, the job is saved offline and can be submitted later.
                    if (jobSubmitted) {
                        Timber.v("BiometricKYCFragment Job Submitted");
                    } else {
                        Timber.v("BiometricKYCFragment Job saved offline.");
                    }
                } else if (biometricKycResult instanceof SmileIDResult.Error error) {
                    Throwable throwable = error.getThrowable();
                    Timber.v("BiometricKYCFragment Error: %s", throwable.getMessage());
                }
                getSupportFragmentManager()
                    .beginTransaction()
                    .remove(biometricKYCFragment)
                    .commit();
                hideProductFragment();
            }
        );

        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, biometricKYCFragment)
            .commit();
        showProductFragment();
    }

    private void doSmartSelfieEnrollment() {
        SmartSelfieEnrollmentFragment smartSelfieFragment = SmartSelfieEnrollmentFragment
            .newInstance();
        getSupportFragmentManager().setFragmentResultListener(
            SmartSelfieEnrollmentFragment.KEY_REQUEST,
            this,
            (requestKey, result) -> {
                SmileIDResult<SmartSelfieResult> smartSelfieResult =
                    SmartSelfieEnrollmentFragment.resultFromBundle(result);
                Timber.v("SmartSelfieEnrollment Result: %s", smartSelfieResult);
                if (smartSelfieResult instanceof SmileIDResult.Success<SmartSelfieResult> successResult) {
                    File selfieFile = successResult.getData().getSelfieFile();
                    List<File> livenessFiles = successResult.getData().getLivenessFiles();
                    boolean jobSubmitted = successResult.getData().getApiResponse() != null;
                    // When offline mode is enabled, the job is saved offline and can be submitted later.
                    if (jobSubmitted) {
                        Timber.v("SmartSelfieEnrollment Job Submitted");
                    } else {
                        Timber.v("SmartSelfieEnrollment Job saved offline.");
                    }
                } else if (smartSelfieResult instanceof SmileIDResult.Error error) {
                    Throwable throwable = error.getThrowable();
                    Timber.v("SmartSelfieEnrollment Error: %s", throwable.getMessage());
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

    private void doEnhancedSmartSelfieEnrollment() {
        EnhancedSmartSelfieEnrollmentFragment smartSelfieFragment = EnhancedSmartSelfieEnrollmentFragment
            .newInstance();
        getSupportFragmentManager().setFragmentResultListener(
            EnhancedSmartSelfieEnrollmentFragment.KEY_REQUEST,
            this,
            (requestKey, result) -> {
                SmileIDResult<SmartSelfieResult> smartSelfieResult =
                    EnhancedSmartSelfieEnrollmentFragment.resultFromBundle(result);
                Timber.v("EnhancedSmartSelfieEnrollment Result: %s", smartSelfieResult);
                if (smartSelfieResult instanceof SmileIDResult.Success<SmartSelfieResult> successResult) {
                    File selfieFile = successResult.getData().getSelfieFile();
                    List<File> livenessFiles = successResult.getData().getLivenessFiles();
                    boolean jobSubmitted = successResult.getData().getApiResponse() != null;
                    // When offline mode is enabled, the job is saved offline and can be submitted later.
                    if (jobSubmitted) {
                        Timber.v("EnhancedSmartSelfieEnrollment Job Submitted");
                    } else {
                        Timber.v("EnhancedSmartSelfieEnrollment Job saved offline.");
                    }
                } else if (smartSelfieResult instanceof SmileIDResult.Error error) {
                    Throwable throwable = error.getThrowable();
                    Timber.v("EnhancedSmartSelfieEnrollment Error: %s", throwable.getMessage());
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
        String jobId = randomJobId();
        boolean allowAgentMode = false;
        SmartSelfieAuthenticationFragment smartSelfieFragment = SmartSelfieAuthenticationFragment
            .newInstance(userId, jobId, allowAgentMode);
        getSupportFragmentManager().setFragmentResultListener(
            SmartSelfieAuthenticationFragment.KEY_REQUEST,
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

    private void doEnhancedSmartSelfieAuthentication() {
        String userId = randomUserId();
        EnhancedSmartSelfieAuthenticationFragment smartSelfieFragment = EnhancedSmartSelfieAuthenticationFragment
            .newInstance(userId);
        getSupportFragmentManager().setFragmentResultListener(
            EnhancedSmartSelfieAuthenticationFragment.KEY_REQUEST,
            this,
            (requestKey, result) -> {
                SmileIDResult<SmartSelfieResult> smartSelfieResult =
                    EnhancedSmartSelfieAuthenticationFragment.resultFromBundle(result);
                Timber.v("EnhancedSmartSelfieAuthentication Result: %s", smartSelfieResult);
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

    private void doDocumentVerification() {
        DocumentVerificationFragment documentVerificationFragment = DocumentVerificationFragment
            .newInstance("GH", "DRIVERS_LICENSE");
        getSupportFragmentManager().setFragmentResultListener(
            DocumentVerificationFragment.KEY_REQUEST,
            this,
            (requestKey, result) -> {
                SmileIDResult<DocumentVerificationResult> documentVerificationResult =
                    DocumentVerificationFragment.resultFromBundle(result);
                Timber.v("DocumentVerification Result: %s", documentVerificationResult);
                getSupportFragmentManager()
                    .beginTransaction()
                    .remove(documentVerificationFragment)
                    .commit();
                hideProductFragment();
            }
        );

        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, documentVerificationFragment)
            .commit();
        showProductFragment();
    }

    private void doEnhancedDocumentVerification() {
        ConsentedInformation consentedInfo = new ConsentedInformation(
            "", true, true, true
        );
        ConsentInformation consentInformation = new ConsentInformation(consentedInfo);
        EnhancedDocumentVerificationFragment enhancedDocVFragment = EnhancedDocumentVerificationFragment
            .newInstance("GH", consentInformation, "DRIVERS_LICENSE");
        getSupportFragmentManager().setFragmentResultListener(
            EnhancedDocumentVerificationFragment.KEY_REQUEST,
            this,
            (requestKey, result) -> {
                SmileIDResult<DocumentVerificationResult> enhancedDocVResult =
                    DocumentVerificationFragment.resultFromBundle(result);
                Timber.v("EnhancedDocumentVerification Result: %s", enhancedDocVResult);
                getSupportFragmentManager()
                    .beginTransaction()
                    .remove(enhancedDocVFragment)
                    .commit();
                hideProductFragment();
            }
        );

        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, enhancedDocVFragment)
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
