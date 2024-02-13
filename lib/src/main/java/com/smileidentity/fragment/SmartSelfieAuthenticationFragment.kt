package com.smileidentity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.smileidentity.SmileID
import com.smileidentity.compose.SmartSelfieAuthentication
import com.smileidentity.fragment.SmartSelfieAuthenticationFragment.Companion.KEY_REQUEST
import com.smileidentity.fragment.SmartSelfieAuthenticationFragment.Companion.KEY_RESULT
import com.smileidentity.fragment.SmartSelfieAuthenticationFragment.Companion.newInstance
import com.smileidentity.fragment.SmartSelfieAuthenticationFragment.Companion.resultFromBundle
import com.smileidentity.fragment.SmartSelfieEnrollmentFragment.Companion.KEY_REQUEST
import com.smileidentity.fragment.SmartSelfieEnrollmentFragment.Companion.resultFromBundle
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.getParcelableCompat
import com.smileidentity.util.getSerializableCompat
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import kotlinx.collections.immutable.toImmutableMap

/**
 * Perform a SmartSelfie™ Authentication
 *
 * [Docs](https://docs.usesmileid.com/products/for-individuals-kyc/biometric-authentication)
 *
 * A [Fragment] wrapper for the [SmartSelfieAuthentication] to be used if not using Jetpack
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
 * SmartSelfieAuthenticationFragment smartSelfieFragment = SmartSelfieAuthenticationFragment
 *  .newInstance();
 * getSupportFragmentManager().setFragmentResultListener(
 *   SmartSelfieAuthenticationFragment.KEY_REQUEST,
 *   this,
 *   (requestKey, result) -> {
 *     SmartSelfieResult smartSelfieResult = SmartSelfieAuthenticationFragment
 *       .resultFromBundle(result);
 *     getSupportFragmentManager()
 *       .beginTransaction()
 *       .remove(smartSelfieFragment)
 *       .commit();
 *     }
 *   );
 * ```
 */
class SmartSelfieAuthenticationFragment : Fragment() {
    companion object {
        const val KEY_REQUEST = "SmartSelfieAuthenticationRequest"

        /**
         * This is internal to prevent partners from accidentally using the wrong key. They only
         * need [KEY_REQUEST]. Partners should use [resultFromBundle] to extract the result.
         */
        internal const val KEY_RESULT = "SmartSelfieAuthenticationResult"

        /**
         * Creates a new instance of [SmartSelfieAuthenticationFragment] which wraps the
         * [SmileID.SmartSelfieAuthentication] Composable under the hood
         *
         * @param userId The user ID to associate with the SmartSelfie™ Authentication. Most often,
         * this will correspond to a unique User ID within your own system. If not provided, a
         * random user ID will be generated.
         * @param jobId The job ID to associate with the SmartSelfie™ Authentication. Most often, this
         * will correspond to a unique Job ID within your own system. If not provided, a random job ID
         * will be generated.
         * @param allowAgentMode Whether to allow Agent Mode or not. If allowed, a switch will be
         * displayed allowing toggling between the back camera and front camera. If not allowed,
         * only the front camera will be used.
         * @param showAttribution Whether to show the Smile ID attribution or not.
         */
        @JvmStatic
        @JvmOverloads
        fun newInstance(
            userId: String = randomUserId(),
            jobId: String = randomJobId(),
            allowNewEnroll: Boolean = false,
            allowAgentMode: Boolean = false,
            showAttribution: Boolean = true,
            showInstructions: Boolean = true,
            extraPartnerParams: HashMap<String, String>? = null,
        ) = SmartSelfieAuthenticationFragment().apply {
            arguments = Bundle().apply {
                this.userId = userId
                this.jobId = jobId
                this.allowNewEnroll = allowNewEnroll
                this.allowAgentMode = allowAgentMode
                this.showAttribution = showAttribution
                this.showInstructions = showInstructions
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
    ) = ComposeView(requireContext()).apply {
        // Dispose of the Composition when the view's LifecycleOwner is destroyed. see:
        // https://developer.android.com/jetpack/compose/interop/interop-apis#compose-in-fragments
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        val args = requireArguments()
        setContent {
            SmileID.SmartSelfieAuthentication(
                userId = args.userId,
                jobId = args.jobId,
                allowNewEnroll = args.allowNewEnroll,
                allowAgentMode = args.allowAgentMode,
                showAttribution = args.showAttribution,
                showInstructions = args.showInstructions,
                extraPartnerParams = (args.extraPartnerParams ?: mapOf()).toImmutableMap(),
                onResult = {
                    setFragmentResult(KEY_REQUEST, Bundle().apply { smileIdResult = it })
                },
            )
        }
    }
}

private const val KEY_ALLOW_AGENT_MODE = "allowAgentMode"
private var Bundle.allowAgentMode: Boolean
    get() = getBoolean(KEY_ALLOW_AGENT_MODE)
    set(value) = putBoolean(KEY_ALLOW_AGENT_MODE, value)

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

private const val KEY_SHOW_INSTRUCTIONS = "showInstructions"
private var Bundle.showInstructions: Boolean
    get() = getBoolean(KEY_SHOW_INSTRUCTIONS)
    set(value) = putBoolean(KEY_SHOW_INSTRUCTIONS, value)

private const val KEY_EXTRA_PARTNER_PARAMS = "extraPartnerParams"
private var Bundle.extraPartnerParams: HashMap<String, String>?
    get() = getSerializableCompat(KEY_EXTRA_PARTNER_PARAMS)
    set(value) = putSerializable(KEY_EXTRA_PARTNER_PARAMS, value)

private var Bundle.smileIdResult: SmileIDResult<SmartSelfieResult>
    get() = getParcelableCompat(KEY_RESULT)!!
    set(value) = putParcelable(KEY_RESULT, value)
