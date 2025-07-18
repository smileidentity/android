package com.smileidentity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.compose.content
import com.smileidentity.SmileID
import com.smileidentity.compose.BiometricKYC
import com.smileidentity.fragment.BiometricKYCFragment.Companion.KEY_REQUEST
import com.smileidentity.fragment.BiometricKYCFragment.Companion.KEY_RESULT
import com.smileidentity.fragment.BiometricKYCFragment.Companion.newInstance
import com.smileidentity.fragment.BiometricKYCFragment.Companion.resultFromBundle
import com.smileidentity.models.ConsentInformation
import com.smileidentity.models.IdInfo
import com.smileidentity.results.BiometricKycResult
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.getParcelableCompat
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import com.squareup.moshi.Types
import kotlinx.collections.immutable.toImmutableMap

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

        /**
         * This is internal to prevent partners from accidentally using the wrong key. They only
         * need [KEY_REQUEST]. Partners should use [resultFromBundle] to extract the result.
         */
        internal const val KEY_RESULT = "BiometricKYCResult"

        @JvmStatic
        @JvmOverloads
        fun newInstance(
            idInfo: IdInfo,
            consentInformation: ConsentInformation? = null,
            userId: String = randomUserId(),
            jobId: String = randomJobId(),
            allowNewEnroll: Boolean = false,
            allowAgentMode: Boolean = false,
            showAttribution: Boolean = true,
            showInstructions: Boolean = true,
            useStrictMode: Boolean = false,
            extraPartnerParams: HashMap<String, String>? = null,
        ) = BiometricKYCFragment().apply {
            arguments = Bundle().apply {
                this.idInfo = idInfo
                this.consentInformation = consentInformation
                this.userId = userId
                this.jobId = jobId
                this.allowNewEnroll = allowNewEnroll
                this.allowAgentMode = allowAgentMode
                this.showAttribution = showAttribution
                this.showInstructions = showInstructions
                this.useStrictMode = useStrictMode
                this.extraPartnerParams = extraPartnerParams
            }
        }

        @JvmStatic
        fun resultFromBundle(bundle: Bundle) = bundle.smileIDResult
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = content {
        val args = requireArguments()
        SmileID.BiometricKYC(
            idInfo = args.idInfo,
            consentInformation = args.consentInformation,
            userId = args.userId,
            jobId = args.jobId,
            allowNewEnroll = args.allowNewEnroll,
            allowAgentMode = args.allowAgentMode,
            showAttribution = args.showAttribution,
            showInstructions = args.showInstructions,
            useStrictMode = args.useStrictMode,
            extraPartnerParams = (args.extraPartnerParams ?: mapOf()).toImmutableMap(),
            onResult = {
                setFragmentResult(KEY_REQUEST, Bundle().apply { smileIDResult = it })
            },
        )
    }
}

private val moshi = SmileID.moshi

private const val KEY_ID_INFO = "idInfo"
private var Bundle.idInfo: IdInfo
    get() = getParcelableCompat(KEY_ID_INFO)!!
    set(value) = putParcelable(KEY_ID_INFO, value)

private const val KEY_CONSENT_INFORMATION = "consentInformation"
private var Bundle.consentInformation: ConsentInformation?
    get() = getParcelableCompat(KEY_CONSENT_INFORMATION)!!
    set(value) = putParcelable(KEY_CONSENT_INFORMATION, value)

private const val KEY_USER_ID = "userId"
private var Bundle.userId: String
    get() = getString(KEY_USER_ID)!!
    set(value) = putString(KEY_USER_ID, value)

private const val KEY_JOB_ID = "jobId"
private var Bundle.jobId: String
    get() = getString(KEY_JOB_ID)!!
    set(value) = putString(KEY_JOB_ID, value)

private const val KEY_ALLOW_NEW_ENROLL = "allowNewEnroll"
private var Bundle.allowNewEnroll: Boolean
    get() = getBoolean(KEY_ALLOW_NEW_ENROLL)
    set(value) = putBoolean(KEY_ALLOW_NEW_ENROLL, value)

private const val KEY_SHOW_ATTRIBUTION = "showAttribution"
private var Bundle.showAttribution: Boolean
    get() = getBoolean(KEY_SHOW_ATTRIBUTION)
    set(value) = putBoolean(KEY_SHOW_ATTRIBUTION, value)

private const val KEY_ALLOW_AGENT_MODE = "allowAgentMode"
private var Bundle.allowAgentMode: Boolean
    get() = getBoolean(KEY_ALLOW_AGENT_MODE)
    set(value) = putBoolean(KEY_ALLOW_AGENT_MODE, value)

private const val KEY_SHOW_INSTRUCTIONS = "showInstructions"
private var Bundle.showInstructions: Boolean
    get() = getBoolean(KEY_SHOW_INSTRUCTIONS)
    set(value) = putBoolean(KEY_SHOW_INSTRUCTIONS, value)

private const val KEY_USE_STRICT_MODE = "useStrictMode"
private var Bundle.useStrictMode: Boolean
    get() = getBoolean(KEY_USE_STRICT_MODE)
    set(value) = putBoolean(KEY_USE_STRICT_MODE, value)

private const val KEY_EXTRA_PARTNER_PARAMS = "extraPartnerParams"
private val type = Types.newParameterizedType(
    Map::class.java,
    String::class.java,
    String::class.java,
)
private val adapter = moshi.adapter<Map<String, String>>(type)
private var Bundle.extraPartnerParams: Map<String, String>?
    get() = getString(KEY_EXTRA_PARTNER_PARAMS)?.let { adapter.fromJson(it) }
    set(value) = putString(KEY_EXTRA_PARTNER_PARAMS, value?.let { adapter.toJson(it) })

private var Bundle.smileIDResult: SmileIDResult<BiometricKycResult>
    get() = getParcelableCompat(KEY_RESULT)!!
    set(value) = putParcelable(KEY_RESULT, value)
