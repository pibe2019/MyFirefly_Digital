package com.example.myfireflydigital.ui.modeloui

import com.example.myfireflydigital.domain.model.AppMessage

sealed interface UiEffect {
    data class ShowSnackbar(val message: AppMessage) : UiEffect
    //data object DismissSheet : CitasUiEffect
}