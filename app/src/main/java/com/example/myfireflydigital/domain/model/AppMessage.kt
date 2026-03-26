package com.example.myfireflydigital.domain.model

import com.example.myfireflydigital.domain.util.UiText

sealed interface AppMessage {
    val messageApp: UiText?
    data class Error(override val messageApp: UiText, val action: (()->Unit)?=null) : AppMessage
    data class Warning(override val messageApp: UiText) : AppMessage
    data class Success(override val messageApp: UiText?=null) : AppMessage
    data class Info(override val messageApp: UiText) : AppMessage
    val isSuccess get() = this is Success
    val isError get() = this is Error
}