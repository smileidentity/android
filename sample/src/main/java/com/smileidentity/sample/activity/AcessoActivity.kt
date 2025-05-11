package com.smileidentity.sample.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.acesso.acessobio_android.AcessoBioListener
import com.acesso.acessobio_android.iAcessoBioSelfie
import com.acesso.acessobio_android.onboarding.AcessoBio
import com.acesso.acessobio_android.onboarding.AcessoBioConfigDataSource
import com.acesso.acessobio_android.onboarding.IAcessoBioBuilder
import com.acesso.acessobio_android.onboarding.camera.CameraListener
import com.acesso.acessobio_android.onboarding.camera.UnicoCheckCamera
import com.acesso.acessobio_android.onboarding.camera.UnicoCheckCameraOpener
import com.acesso.acessobio_android.onboarding.models.Environment
import com.acesso.acessobio_android.services.dto.ErrorBio
import com.acesso.acessobio_android.services.dto.ResultCamera
import com.smileidentity.sample.R
import timber.log.Timber

class AcessoActivity : AppCompatActivity() {

    private val callback = object : AcessoBioListener {

        override fun onErrorAcessoBio(errorBio: ErrorBio) {
            Timber.d("Acesso onErrorAcessoBio $errorBio")
        }

        override fun onUserClosedCameraManually() {
            Timber.d("Acesso onUserClosedCameraManually")
        }

        override fun onSystemClosedCameraTimeoutSession() {
            Timber.d("Acesso onSystemClosedCameraTimeoutSession ")
        }

        override fun onSystemChangedTypeCameraTimeoutFaceInference() {
            Timber.d("Acesso onSystemChangedTypeCameraTimeoutFaceInference")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_acesso)

        val acessoBioBuilder: IAcessoBioBuilder = AcessoBio(this, callback)

        acessoBioBuilder.setEnvironment(Environment.UAT)

        findViewById<View>(R.id.button_selfie)
            .setOnClickListener {
                val unicoCheckCamera: UnicoCheckCamera = acessoBioBuilder
                    .setAutoCapture(true)
                    .setSmartFrame(true)
                    .build()

                val cameraListener: iAcessoBioSelfie = object : iAcessoBioSelfie {
                    override fun onSuccessSelfie(result: ResultCamera) {
                        Timber.d("Acesso onSuccessSelfie $result")
                    }

                    override fun onErrorSelfie(errorBio: ErrorBio) {
                        Timber.d("Acesso onErrorSelfie $errorBio")
                    }
                }

                unicoCheckCamera.prepareCamera(
                    object : AcessoBioConfigDataSource {
                        override fun getBundleIdentifier() = "com.smileidentity.sample"

                        override fun getHostKey() = ""
                    },
                    object : CameraListener {
                        override fun onCameraReady(cameraOpener: UnicoCheckCameraOpener.Camera) {
                            cameraOpener.open(cameraListener)
                        }

                        override fun onCameraFailed(message: String) {
                            Timber.d("Acesso onCameraFailed $message")
                            Timber.e(message)
                        }
                    },
                )
            }
    }
}
