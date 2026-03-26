package com.example.myfireflydigital.domain.exceptions

class LocalStorageException(val messageStorExcept: String="Error al interactuar con datos locales") : Exception(messageStorExcept)