package com.smileidentity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.smileidentity.SmileID
import com.smileidentity.compose.BiometricKYC
import com.smileidentity.fragment.BiometricKYCFragment.Companion.KEY_REQUEST
import com.smileidentity.fragment.BiometricKYCFragment.Companion.KEY_RESULT
import com.smileidentity.fragment.BiometricKYCFragment.Companion.newInstance
import com.smileidentity.fragment.BiometricKYCFragment.Companion.resultFromBundle
import com.smileidentity.models.IdInfo
import com.smileidentity.results.BiometricKycResult
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.getParcelableCompat
import com.smileidentity.util.getSerializableCompat
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import java.net.URL

/**
 * Perform a Biometric KYC: Verify the ID information of your user and confirm that the ID actually
 * belongs to the user. This is achieved by comparing the user's SmartSelfie™ to the user's photo in
 * an ID authority database
 *
 * [Docs](https://docs.smileidentity.com/products/for-individuals-kyc/biometric-kyc)
 *
 * A [Fragment] wrapper for the [BiometricKYC] to be used if not using Jetpack Compose.
 * New instances *must* be created via [newInstance]. Results are communicated back to the caller
 * via [setFragmentResult]. Therefore, the caller must use
 * [androidx.fragment.app.FragmentManager.setFragmentResultListener] to listen for the result. If
 * using parent/child fragments, the caller must use the appropriate child/parent FragmentManager.
 * The result key is [KEY_REQUEST] and the result is a [SmileIDResult] in the bundle under the
 * key [KEY_RESULT]. A convenience method [resultFromBundle] is provided to extract the result from
 * the bundle.
 *
 * To theme the UI, override the si_color_* resources
 *
 * Usage example:
 * ```java
 * Document document = new Document("GH", "DRIVERS_LICENSE", 1.6f);
 * BiometricKYCFragment biometricKycFragment = BiometricKYCFragment
 *     .newInstance(document, R.drawable.my_logo, "My Partner", "My Product",
 *     new URL("https://my-privacy-policy.com"));
 * getSupportFragmentManager().setFragmentResultListener(
 *     BiometricKYCFragment.KEY_REQUEST,
 *     this,
 *     (requestKey, result) -> {
 *         SmileIDResult<BiometricKYCResult> biometricKycResult =
 *             BiometricKYCResult.resultFromBundle(result);
 *         Timber.v("BiometricKYC Result: %s", biometricKycResult);
 *         getSupportFragmentManager()
 *             .beginTransaction()
 *             .remove(biometricKycFragment)
 *             .commit();
 *         hideProductFragment();
 *     }
 * );
 * ```
 */
class BiometricKYCFragment : Fragment() {
    companion object {
        const val KEY_REQUEST = "BiometricKYCRequest"
        const val KEY_RESULT = "BiometricKYCResult"

        @JvmStatic
        @JvmOverloads
        fun newInstance(
            idInfo: IdInfo,
            @DrawableRes partnerIcon: Int,
            partnerName: String,
            productName: String,
            partnerPrivacyPolicy: URL,
            userId: String = randomUserId(),
            jobId: String = randomJobId(),
            allowAgentMode: Boolean = false,
            showAttribution: Boolean = true,
        ) = BiometricKYCFragment().apply {
            arguments = Bundle().apply {
                this.idInfo = idInfo
                this.partnerIcon = partnerIcon
                this.partnerName = partnerName
                this.productName = productName
                this.partnerPrivacyPolicy = partnerPrivacyPolicy
                this.userId = userId
                this.jobId = jobId
                this.allowAgentMode = allowAgentMode
                this.showAttribution = showAttribution
            }
        }

        @JvmStatic
        fun resultFromBundle(bundle: Bundle) = bundle.smileIDResult
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = ComposeView(requireContext()).apply {
        // Dispose of the Composition when the view's LifecycleOwner is destroyed. see:
        // https://developer.android.com/jetpack/compose/interop/interop-apis#compose-in-fragments
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        val args = requireArguments()
        setContent {
            SmileID.BiometricKYC(
                idInfo = args.idInfo,
                partnerIcon = painterResource(id = args.partnerIcon),
                partnerName = args.partnerName,
                productName = args.productName,
                partnerPrivacyPolicy = args.partnerPrivacyPolicy,
                userId = args.userId,
                jobId = args.jobId,
                allowAgentMode = args.allowAgentMode,
                showAttribution = args.showAttribution,
                onResult = {
                    setFragmentResult(KEY_REQUEST, Bundle().apply { smileIDResult = it })
                },
            )
        }
    }
}

private const val KEY_ID_INFO = "idInfo"
private var Bundle.idInfo: IdInfo
    get() = getParcelableCompat(KEY_ID_INFO)!!
    set(value) = putParcelable(KEY_ID_INFO, value)

private const val KEY_PARTNER_ICON = "partnerIcon"
private var Bundle.partnerIcon: Int
    get() = getInt(KEY_PARTNER_ICON)
    set(value) = putInt(KEY_PARTNER_ICON, value)

private const val KEY_PARTNER_NAME = "partnerName"
private var Bundle.partnerName: String
    get() = getString(KEY_PARTNER_NAME)!!
    set(value) = putString(KEY_PARTNER_NAME, value)

private const val KEY_PRODUCT_NAME = "productName"
private var Bundle.productName: String
    get() = getString(KEY_PRODUCT_NAME)!!
    set(value) = putString(KEY_PRODUCT_NAME, value)

private const val KEY_PARTNER_PRIVACY_POLICY = "partnerPrivacyPolicy"
private var Bundle.partnerPrivacyPolicy: URL
    get() = getSerializableCompat(KEY_PARTNER_PRIVACY_POLICY)!!
    set(value) = putSerializable(KEY_PARTNER_PRIVACY_POLICY, value)

private const val KEY_USER_ID = "userId"
private var Bundle.userId: String
    get() = getString(KEY_USER_ID)!!
    set(value) = putString(KEY_USER_ID, value)

private const val KEY_JOB_ID = "jobId"
private var Bundle.jobId: String
    get() = getString(KEY_JOB_ID)!!
    set(value) = putString(KEY_JOB_ID, value)

private const val KEY_SHOW_ATTRIBUTION = "showAttribution"
private var Bundle.showAttribution: Boolean
    get() = getBoolean(KEY_SHOW_ATTRIBUTION)
    set(value) = putBoolean(KEY_SHOW_ATTRIBUTION, value)

private const val KEY_ALLOW_AGENT_MODE = "allowAgentMode"
private var Bundle.allowAgentMode: Boolean
    get() = getBoolean(KEY_ALLOW_AGENT_MODE)
    set(value) = putBoolean(KEY_ALLOW_AGENT_MODE, value)

private var Bundle.smileIDResult: SmileIDResult<BiometricKycResult>
    get() = getParcelableCompat(KEY_RESULT)!!
    set(value) = putParcelable(KEY_RESULT, value)
