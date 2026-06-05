package com.example.myfireflydigital.ui.modeloui

import androidx.activity.result.IntentSenderRequest
import com.example.myfireflydigital.domain.model.AppMessage

sealed interface UiEffect {
    data class ShowSnackbar(val message: AppMessage) : UiEffect
    data class RequestGpsEnable(val intentSenderRequest: IntentSenderRequest) : UiEffect
    //data object DismissSheet : CitasUiEffect
}