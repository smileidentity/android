package com.smileidentity.unico

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme

class UnicoVerificationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                UnicoVerificationScreen(
                    duiType = UnicoProcessManager.DUI_TYPE_US_SSN,
                    duiValue = "525769301",
                    friendlyName = "Evandro",
                    callbackUri = "https://webhook.site/688c2801-d554-4111-b49b-22e72b2def51",
                    flow = UnicoProcessManager.FLOW_ID_LIVE_TRUST,
                    purpose = UnicoProcessManager.PURPOSE_CREDIT_PROCESS,
                    onSuccess = {
                        setResult(RESULT_OK)
                        finish()
                    },
                    onFailure = {
                        setResult(RESULT_CANCELED)
                        finish()
                    },
                    onClose = {
                        setResult(RESULT_CANCELED)
                        finish()
                    },
                )
            }
        }
    }
}
