package com.example.myfireflydigital.domain.usecase

import com.example.myfireflydigital.domain.model.Cita
import com.example.myfireflydigital.domain.repository.CitaRepository
import javax.inject.Inject

class UpsertCitaUseCase @Inject constructor(private val repository: CitaRepository) {
    suspend operator fun invoke(cita : Cita) : Result<Boolean> = repository.upsertCita(cita)
}