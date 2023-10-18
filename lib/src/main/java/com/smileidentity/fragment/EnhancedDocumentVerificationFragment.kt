package com.smileidentity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.smileidentity.SmileID
import com.smileidentity.compose.EnhancedDocumentVerificationScreen
import com.smileidentity.fragment.EnhancedDocumentVerificationFragment.Companion.KEY_REQUEST
import com.smileidentity.fragment.EnhancedDocumentVerificationFragment.Companion.KEY_RESULT
import com.smileidentity.fragment.EnhancedDocumentVerificationFragment.Companion.newInstance
import com.smileidentity.fragment.EnhancedDocumentVerificationFragment.Companion.resultFromBundle
import com.smileidentity.results.EnhancedDocumentVerificationResult
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.getParcelableCompat
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId

/**
 * Perform Enhanced Document Verification
 *
 * [Docs](https://docs.usesmileid.com/products/for-individuals-kyc/enhanced-document-verification)
 *
 * A [Fragment] wrapper for the [EnhancedDocumentVerification] to be used if not using Jetpack Compose.
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
 * EnhancedDocumentVerificationFragment enhancedDocVFragment = EnhancedDocumentVerificationFragment
 *     .newInstance(document);
 * getSupportFragmentManager().setFragmentResultListener(
 *     EnhancedDocumentVerificationFragment.KEY_REQUEST,
 *     this,
 *     (requestKey, result) -> {
 *         SmileIDResult<DocumentVerificationResult> enhancedDocVResult =
 *             DocumentVerificationFragment.resultFromBundle(result);
 *         Timber.v("EnhancedDocumentVerification Result: %s", enhancedDocVResult);
 *         getSupportFragmentManager()
 *             .beginTransaction()
 *             .remove(enhancedDocVFragment)
 *             .commit();
 *         hideProductFragment();
 *     }
 * );
 * ```
 */
class EnhancedDocumentVerificationFragment : Fragment() {
    companion object {
        const val KEY_REQUEST = "EnhancedDocumentVerificationRequest"
        const val KEY_RESULT = "EnhancedDocumentVerificationResult"

        @JvmStatic
        @JvmOverloads
        fun newInstance(
            countryCode: String,
            documentType: String? = null,
            userId: String = randomUserId(),
            jobId: String = randomJobId(),
            showAttribution: Boolean = true,
            allowGalleryUpload: Boolean = false,
            idAspectRatio: Float? = null,
            captureBothSides: Boolean = false,
        ) = EnhancedDocumentVerificationFragment().apply {
            arguments = Bundle().apply {
                this.userId = userId
                this.jobId = jobId
                this.showAttribution = showAttribution
                this.allowGalleryUpload = allowGalleryUpload
                this.countryCode = countryCode
                this.documentType = documentType
                this.idAspectRatio = idAspectRatio ?: -1f
                this.captureBothSides = captureBothSides
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
            val aspectRatio = args.idAspectRatio
            SmileID.EnhancedDocumentVerificationScreen(
                countryCode = args.countryCode,
                documentType = args.documentType,
                userId = args.userId,
                jobId = args.jobId,
                showAttribution = args.showAttribution,
                allowGalleryUpload = args.allowGalleryUpload,
                idAspectRatio = if (aspectRatio > 0) aspectRatio else null,
                onResult = {
                    setFragmentResult(KEY_REQUEST, Bundle().apply { smileIDResult = it })
                },
            )
        }
    }
}

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

private const val KEY_ALLOW_GALLERY_UPLOAD = "allowGalleryUpload"
private var Bundle.allowGalleryUpload: Boolean
    get() = getBoolean(KEY_ALLOW_GALLERY_UPLOAD)
    set(value) = putBoolean(KEY_ALLOW_GALLERY_UPLOAD, value)

private const val KEY_COUNTRY_CODE = "countryCode"

private var Bundle.countryCode: String
    get() = getString(KEY_COUNTRY_CODE)!!
    set(value) = putString(KEY_COUNTRY_CODE, value)

private const val KEY_DOCUMENT_TYPE = "documentType"

private var Bundle.documentType: String?
    get() = getString(KEY_DOCUMENT_TYPE)
    set(value) = putString(KEY_DOCUMENT_TYPE, value)

private const val KEY_ID_ASPECT_RATIO = "idAspectRatio"

private var Bundle.idAspectRatio: Float
    get() = getFloat(KEY_ID_ASPECT_RATIO)
    set(value) = putFloat(KEY_ID_ASPECT_RATIO, value)

private const val KEY_CAPTURE_BOTH_SIDES = "captureBothSides"
private var Bundle.captureBothSides: Boolean
    get() = getBoolean(KEY_CAPTURE_BOTH_SIDES)
    set(value) = putBoolean(KEY_CAPTURE_BOTH_SIDES, value)

private var Bundle.smileIDResult: SmileIDResult<EnhancedDocumentVerificationResult>
    get() = getParcelableCompat(DocumentVerificationFragment.KEY_RESULT)!!
    set(value) = putParcelable(DocumentVerificationFragment.KEY_RESULT, value)
