package com.smileidentity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.smileidentity.SmileID
import com.smileidentity.compose.SmartSelfieAuthenticationScreen
import com.smileidentity.fragment.SmartSelfieAuthenticationFragment.Companion.KEY_REQUEST
import com.smileidentity.fragment.SmartSelfieAuthenticationFragment.Companion.KEY_RESULT
import com.smileidentity.fragment.SmartSelfieAuthenticationFragment.Companion.newInstance
import com.smileidentity.fragment.SmartSelfieAuthenticationFragment.Companion.resultFromBundle
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.getParcelableCompat
import com.smileidentity.util.randomUserId

/**
 * Perform a SmartSelfie™ Authentication
 *
 * [Docs](https://docs.smileidentity.com/products/for-individuals-kyc/biometric-authentication)
 *
 * A [Fragment] wrapper for the [SmartSelfieAuthenticationScreen] to be used if not using Jetpack
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
        const val KEY_RESULT = "SmartSelfieAuthenticationResult"

        /**
         * Creates a new instance of [SmartSelfieAuthenticationFragment] which wraps the
         * [SmileID.SmartSelfieAuthenticationScreen] Composable under the hood
         *
         * @param userId The user ID to associate with the SmartSelfie™ Authentication. Most often,
         * this will correspond to a unique User ID within your own system. If not provided, a
         * random user ID will be generated.
         * @param allowAgentMode Whether to allow Agent Mode or not. If allowed, a switch will be
         * displayed allowing toggling between the back camera and front camera. If not allowed,
         * only the front camera will be used.
         * @param showAttribution Whether to show the Smile ID attribution or not.
         */
        @JvmStatic
        @JvmOverloads
        fun newInstance(
            userId: String = randomUserId(),
            allowAgentMode: Boolean = false,
            showAttribution: Boolean = true,
        ) = SmartSelfieAuthenticationFragment().apply {
            arguments = Bundle().apply {
                this.userId = userId
                this.allowAgentMode = allowAgentMode
                this.showAttribution = showAttribution
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
            SmileID.SmartSelfieAuthenticationScreen(
                userId = args.userId,
                allowAgentMode = args.allowAgentMode,
                showAttribution = args.showAttribution,
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

private const val KEY_SHOW_ATTRIBUTION = "showAttribution"
private var Bundle.showAttribution: Boolean
    get() = getBoolean(KEY_SHOW_ATTRIBUTION)
    set(value) = putBoolean(KEY_SHOW_ATTRIBUTION, value)

private var Bundle.smileIdResult: SmileIDResult<SmartSelfieResult>
    get() = getParcelableCompat(KEY_RESULT)!!
    set(value) = putParcelable(KEY_RESULT, value)
