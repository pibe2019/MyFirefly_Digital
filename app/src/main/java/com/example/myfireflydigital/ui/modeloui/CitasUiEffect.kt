package com.example.myfireflydigital.ui.modeloui

import com.example.myfireflydigital.domain.model.AppMessage

sealed interface CitasUiEffect {
    data class ShowSnackbar(val message: AppMessage) : CitasUiEffect
    //data object DismissSheet : CitasUiEffect
}