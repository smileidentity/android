package com.smileidentity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.smileidentity.SmileID
import com.smileidentity.compose.DocumentVerification
import com.smileidentity.fragment.DocumentVerificationFragment.Companion.KEY_REQUEST
import com.smileidentity.fragment.DocumentVerificationFragment.Companion.KEY_RESULT
import com.smileidentity.fragment.DocumentVerificationFragment.Companion.newInstance
import com.smileidentity.fragment.DocumentVerificationFragment.Companion.resultFromBundle
import com.smileidentity.results.DocumentVerificationResult
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.getParcelableCompat
import com.smileidentity.util.getSerializableCompat
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import java.io.File

/**
 * Perform a Document Verification
 *
 * [Docs](https://docs.usesmileid.com/products/for-individuals-kyc/document-verification)
 *
 * A [Fragment] wrapper for the [DocumentVerification] to be used if not using Jetpack Compose.
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
 * DocumentVerificationFragment documentVerificationFragment = DocumentVerificationFragment
 *     .newInstance(document);
 * getSupportFragmentManager().setFragmentResultListener(
 *     DocumentVerificationFragment.KEY_REQUEST,
 *     this,
 *     (requestKey, result) -> {
 *         SmileIDResult<DocumentVerificationResult> documentVerificationResult =
 *             DocumentVerificationFragment.resultFromBundle(result);
 *         Timber.v("DocumentVerification Result: %s", documentVerificationResult);
 *         getSupportFragmentManager()
 *             .beginTransaction()
 *             .remove(documentVerificationFragment)
 *             .commit();
 *         hideProductFragment();
 *     }
 * );
 * ```
 */
class DocumentVerificationFragment : Fragment() {
    companion object {
        const val KEY_REQUEST = "DocumentVerificationRequest"
        const val KEY_RESULT = "DocumentVerificationResult"

        @JvmStatic
        @JvmOverloads
        fun newInstance(
            userId: String = randomUserId(),
            jobId: String = randomJobId(),
            showAttribution: Boolean = true,
            allowGalleryUpload: Boolean = false,
            countryCode: String,
            documentType: String,
            idAspectRatio: Float? = null,
            captureBothSides: Boolean = false,
            bypassSelfieCaptureWithFile: File? = null,
        ) = DocumentVerificationFragment().apply {
            arguments = Bundle().apply {
                this.userId = userId
                this.jobId = jobId
                this.showAttribution = showAttribution
                this.allowGalleryUpload = allowGalleryUpload
                this.countryCode = countryCode
                this.documentType = documentType
                this.idAspectRatio = idAspectRatio ?: -1f
                this.captureBothSides = captureBothSides
                this.bypassSelfieCaptureWithFile = bypassSelfieCaptureWithFile
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
        setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
        val args = requireArguments()
        setContent {
            val aspectRatio = args.idAspectRatio
            SmileID.DocumentVerification(
                userId = args.userId,
                jobId = args.jobId,
                showAttribution = args.showAttribution,
                allowGalleryUpload = args.allowGalleryUpload,
                countryCode = args.countryCode,
                documentType = args.documentType,
                idAspectRatio = if (aspectRatio > 0) aspectRatio else null,
                captureBothSides = args.captureBothSides,
                bypassSelfieCaptureWithFile = args.bypassSelfieCaptureWithFile,
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
private const val KEY_DOCUMENT_TYPE = "documentType"
private var Bundle.countryCode: String
    get() = getString(KEY_COUNTRY_CODE)!!
    set(value) = putString(KEY_COUNTRY_CODE, value)

private var Bundle.documentType: String
    get() = getString(KEY_DOCUMENT_TYPE)!!
    set(value) = putString(KEY_DOCUMENT_TYPE, value)

private const val KEY_ID_ASPECT_RATIO = "idAspectRatio"

private var Bundle.idAspectRatio: Float
    get() = getFloat(KEY_ID_ASPECT_RATIO)
    set(value) = putFloat(KEY_ID_ASPECT_RATIO, value)

private const val KEY_CAPTURE_BOTH_SIDES = "captureBothSides"
private var Bundle.captureBothSides: Boolean
    get() = getBoolean(KEY_CAPTURE_BOTH_SIDES)
    set(value) = putBoolean(KEY_CAPTURE_BOTH_SIDES, value)

private const val KEY_BYPASS_SELFIE_CAPTURE_WITH_FILE = "bypassSelfieCaptureWithFile"
private var Bundle.bypassSelfieCaptureWithFile: File?
    get() = getSerializableCompat(KEY_BYPASS_SELFIE_CAPTURE_WITH_FILE) as File?
    set(value) = putSerializable(KEY_BYPASS_SELFIE_CAPTURE_WITH_FILE, value)

private var Bundle.smileIDResult: SmileIDResult<DocumentVerificationResult>
    get() = getParcelableCompat(KEY_RESULT)!!
    set(value) = putParcelable(KEY_RESULT, value)
