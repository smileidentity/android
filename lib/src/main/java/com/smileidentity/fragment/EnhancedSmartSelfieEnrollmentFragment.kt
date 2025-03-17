package com.smileidentity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.compose.content
import com.smileidentity.SmileID
import com.smileidentity.compose.SmartSelfieEnrollment
import com.smileidentity.compose.SmartSelfieEnrollmentEnhanced
import com.smileidentity.fragment.EnhancedSmartSelfieEnrollmentFragment.Companion.KEY_REQUEST
import com.smileidentity.fragment.EnhancedSmartSelfieEnrollmentFragment.Companion.KEY_RESULT
import com.smileidentity.fragment.EnhancedSmartSelfieEnrollmentFragment.Companion.newInstance
import com.smileidentity.fragment.EnhancedSmartSelfieEnrollmentFragment.Companion.resultFromBundle
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.getParcelableCompat
import com.smileidentity.util.randomUserId
import com.squareup.moshi.Types
import kotlinx.collections.immutable.toImmutableMap

/**
 * Perform a SmartSelfie™ Enrollment
 *
 * [Docs](https://docs.usesmileid.com/products/for-individuals-kyc/biometric-authentication)
 *
 * A [Fragment] wrapper for the [SmartSelfieEnrollment] to be used if not using Jetpack
 * Compose. New instances *must* be created via [newInstance]. Results are communicated back to the
 * caller via [setFragmentResult]. Therefore, the caller must use
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
 * SmartSelfieEnrollmentFragment smartSelfieFragment = SmartSelfieEnrollmentFragment
 *  .newInstance();
 * getSupportFragmentManager().setFragmentResultListener(
 *   SmartSelfieEnrollmentFragment.KEY_REQUEST,
 *   this,
 *   (requestKey, result) -> {
 *     SmartSelfieResult smartSelfieResult = SmartSelfieEnrollmentFragment
 *       .resultFromBundle(result);
 *     getSupportFragmentManager()
 *       .beginTransaction()
 *       .remove(smartSelfieFragment)
 *       .commit();
 *     }
 *   );
 * ```
 */
class EnhancedSmartSelfieEnrollmentFragment : Fragment() {
    companion object {
        const val KEY_REQUEST = "SmartSelfieEnrollmentRequest"

        /**
         * This is internal to prevent partners from accidentally using the wrong key. They only
         * need [KEY_REQUEST]. Partners should use [resultFromBundle] to extract the result.
         */
        internal const val KEY_RESULT = "SmartSelfieEnrollmentResult"

        /**
         * Creates a new instance of [EnhancedSmartSelfieEnrollmentFragment] which wraps the
         * [SmileID.SmartSelfieEnrollment] Composable under the hood
         *
         * @param userId The user ID to associate with the SmartSelfie™ Enrollment. Most often,
         * this will correspond to a unique User ID within your own system. If not provided, a
         * random user ID will be generated.
         * @param showAttribution Whether to show the Smile ID attribution or not.
         * @param showInstructions Whether to deactivate capture screen's instructions for
         * @param skipApiSubmission Whether to skip the API submission and return the result of capture only
         * SmartSelfie.
         */
        @JvmStatic
        @JvmOverloads
        fun newInstance(
            userId: String = randomUserId(),
            allowNewEnroll: Boolean = false,
            showAttribution: Boolean = true,
            showInstructions: Boolean = true,
            skipApiSubmission: Boolean = false,
            extraPartnerParams: HashMap<String, String>? = null,
        ) = EnhancedSmartSelfieEnrollmentFragment().apply {
            arguments = Bundle().apply {
                this.userId = userId
                this.allowNewEnroll = allowNewEnroll
                this.showAttribution = showAttribution
                this.showInstructions = showInstructions
                this.skipApiSubmission = skipApiSubmission
                this.extraPartnerParams = extraPartnerParams
            }
        }

        @JvmStatic
        fun resultFromBundle(bundle: Bundle) = bundle.smileIdResult
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = content {
        val args = requireArguments()
        SmileID.SmartSelfieEnrollmentEnhanced(
            userId = args.userId,
            allowNewEnroll = args.allowNewEnroll,
            showAttribution = args.showAttribution,
            showInstructions = args.showInstructions,
            skipApiSubmission = args.skipApiSubmission,
            extraPartnerParams = (args.extraPartnerParams ?: mapOf()).toImmutableMap(),
            onResult = {
                setFragmentResult(KEY_REQUEST, Bundle().apply { smileIdResult = it })
            },
        )
    }
}

private val moshi = SmileID.moshi

private const val KEY_USER_ID = "userId"
private var Bundle.userId: String
    get() = getString(KEY_USER_ID)!!
    set(value) = putString(KEY_USER_ID, value)

private const val KEY_ALLOW_NEW_ENROLL = "allowNewEnroll"
private var Bundle.allowNewEnroll: Boolean
    get() = getBoolean(KEY_ALLOW_NEW_ENROLL)
    set(value) = putBoolean(KEY_ALLOW_NEW_ENROLL, value)

private const val KEY_SHOW_ATTRIBUTION = "showAttribution"
private var Bundle.showAttribution: Boolean
    get() = getBoolean(KEY_SHOW_ATTRIBUTION)
    set(value) = putBoolean(KEY_SHOW_ATTRIBUTION, value)

private const val KEY_SHOW_INSTRUCTIONS = "showInstructions"
private var Bundle.showInstructions: Boolean
    get() = getBoolean(KEY_SHOW_INSTRUCTIONS)
    set(value) = putBoolean(KEY_SHOW_INSTRUCTIONS, value)

private const val KEY_SKIP_API_SUBMISSION = "skipApiSubmission"
private var Bundle.skipApiSubmission: Boolean
    get() = getBoolean(KEY_SKIP_API_SUBMISSION)
    set(value) = putBoolean(KEY_SKIP_API_SUBMISSION, value)

private const val KEY_EXTRA_PARTNER_PARAMS = "extraPartnerParams"
private val type =
    Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
private val adapter = moshi.adapter<Map<String, String>>(type)
private var Bundle.extraPartnerParams: Map<String, String>?
    get() = getString(KEY_EXTRA_PARTNER_PARAMS)?.let { adapter.fromJson(it) }
    set(value) = putString(KEY_EXTRA_PARTNER_PARAMS, value?.let { adapter.toJson(it) })

private var Bundle.smileIdResult: SmileIDResult<SmartSelfieResult>
    get() = getParcelableCompat(KEY_RESULT)!!
    set(value) = putParcelable(KEY_RESULT, value)
