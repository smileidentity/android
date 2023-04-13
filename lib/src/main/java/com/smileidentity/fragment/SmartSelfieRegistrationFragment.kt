package com.smileidentity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.smileidentity.SmileIdentity
import com.smileidentity.compose.SmartSelfieRegistrationScreen
import com.smileidentity.fragment.SmartSelfieRegistrationFragment.Companion.KEY_REQUEST
import com.smileidentity.fragment.SmartSelfieRegistrationFragment.Companion.KEY_RESULT
import com.smileidentity.fragment.SmartSelfieRegistrationFragment.Companion.newInstance
import com.smileidentity.randomUserId
import com.smileidentity.results.SmartSelfieResult

/**
 * A [Fragment] wrapper for the [SmartSelfieRegistrationScreen] to be used if not using Jetpack
 * Compose. New instances *must* be created via [newInstance]. Results are communicated back to the
 * caller via [setFragmentResult]. Therefore, the caller must use
 * [androidx.fragment.app.FragmentManager.setFragmentResultListener] to listen for the result. If
 * using parent/child fragments, the caller must use the appropriate child/parent FragmentManager.
 * The result key is [KEY_REQUEST] and the result is a [SmartSelfieResult] in the bundle under the
 * key [KEY_RESULT].
 *
 * To theme the UI, override the si_color_* resources
 *
 * Usage example:
 * ```java
 * SmartSelfieRegistrationFragment smartSelfieFragment = SmartSelfieRegistrationFragment
 *  .newInstance();
 * getSupportFragmentManager().setFragmentResultListener(
 *   SmartSelfieRegistrationFragment.KEY_REQUEST,
 *   this,
 *   (requestKey, result) -> {
 *     SmartSelfieResult smartSelfieResult = SmartSelfieRegistrationFragment
 *       .resultFromBundle(result);
 *     getSupportFragmentManager()
 *       .beginTransaction()
 *       .remove(smartSelfieFragment)
 *       .commit();
 *     }
 *   );
 * ```
 */
class SmartSelfieRegistrationFragment : Fragment() {
    companion object {
        const val KEY_REQUEST = "SmartSelfieRegistrationRequest"
        const val KEY_RESULT = "SmartSelfieRegistrationResult"

        @JvmStatic
        @JvmOverloads
        fun newInstance(
            userId: String = randomUserId(),
            allowAgentMode: Boolean = false,
        ) = SmartSelfieRegistrationFragment().apply {
            arguments = Bundle().apply {
                this.userId = userId
                this.allowAgentMode = allowAgentMode
            }
        }

        @JvmStatic
        fun resultFromBundle(bundle: Bundle): SmartSelfieResult {
            return bundle.getParcelable(KEY_RESULT)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner is destroyed. see:
            // https://developer.android.com/jetpack/compose/interop/interop-apis#compose-in-fragments
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val args = requireArguments()
                SmileIdentity.SmartSelfieRegistrationScreen(
                    userId = args.userId,
                    allowAgentMode = args.allowAgentMode,
                    onResult = {
                        setFragmentResult(KEY_REQUEST, bundleOf(KEY_RESULT to it))
                    },
                )
            }
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
