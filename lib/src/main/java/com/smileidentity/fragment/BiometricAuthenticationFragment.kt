package com.smileidentity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.smileidentity.SmileID
import com.smileidentity.compose.BiometricAuthentication
import com.smileidentity.fragment.BiometricAuthenticationFragment.Companion.KEY_REQUEST
import com.smileidentity.fragment.BiometricAuthenticationFragment.Companion.KEY_RESULT
import com.smileidentity.fragment.BiometricAuthenticationFragment.Companion.newInstance
import com.smileidentity.fragment.BiometricAuthenticationFragment.Companion.resultFromBundle
import com.smileidentity.fragment.SmartSelfieAuthenticationFragment.Companion.KEY_REQUEST
import com.smileidentity.fragment.SmartSelfieAuthenticationFragment.Companion.resultFromBundle
import com.smileidentity.fragment.SmartSelfieEnrollmentFragment.Companion.KEY_REQUEST
import com.smileidentity.fragment.SmartSelfieEnrollmentFragment.Companion.resultFromBundle
import com.smileidentity.models.v2.SmartSelfieResponse
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.getParcelableCompat
import com.smileidentity.util.getSerializableCompat
import kotlinx.collections.immutable.toImmutableMap

/**
 * Perform a Biometric Authentication
 *
 * [Docs](https://docs.usesmileid.com/products/for-individuals-kyc/biometric-authentication)
 *
 * A [DialogFragment] wrapper for the [BiometricAuthentication] to be used if not using Jetpack
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
 * TODO: Test
 */
class BiometricAuthenticationFragment : DialogFragment() {
    companion object {
        const val KEY_REQUEST = "BiometricAuthenticationRequest"

        /**
         * This is internal to prevent partners from accidentally using the wrong key. They only
         * need [KEY_REQUEST]. Partners should use [resultFromBundle] to extract the result.
         */

        internal const val KEY_RESULT = "BiometricAuthenticationResult"

        /**
         * Create a new instance of the [BiometricAuthenticationFragment] which wraps the
         * [SmileID.BiometricAuthentication] Composable under the hood
         *
         * @param userId The user ID to associate with the SmartSelfie™ Authentication. Most often,
         * this will correspond to a unique User ID within your own system. If not provided, a
         * random user ID will be generated.
         * @param extraPartnerParams Custom values specific to partners
         */
        @JvmStatic
        @JvmOverloads
        fun newInstance(userId: String, extraPartnerParams: HashMap<String, String>? = null) =
            BiometricAuthenticationFragment().apply {
                arguments = Bundle().apply {
                    this.userId = userId
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
            SmileID.BiometricAuthentication(
                userId = args.userId,
                extraPartnerParams = (args.extraPartnerParams ?: mapOf()).toImmutableMap(),
                onResult = {
                    setFragmentResult(KEY_REQUEST, Bundle().apply { smileIdResult = it })
                },
            )
        }
    }
}

private const val KEY_USER_ID = "userId"
private var Bundle.userId: String
    get() = getString(KEY_USER_ID)!!
    set(value) = putString(KEY_USER_ID, value)

private const val KEY_EXTRA_PARTNER_PARAMS = "extraPartnerParams"
private var Bundle.extraPartnerParams: HashMap<String, String>?
    get() = getSerializableCompat(KEY_EXTRA_PARTNER_PARAMS)
    set(value) = putSerializable(KEY_EXTRA_PARTNER_PARAMS, value)

private var Bundle.smileIdResult: SmileIDResult<SmartSelfieResponse>
    get() = getParcelableCompat(KEY_RESULT)!!
    set(value) = putParcelable(KEY_RESULT, value)
