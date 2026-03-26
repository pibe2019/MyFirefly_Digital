package com.example.myfireflydigital.domain.usecase

import com.example.myfireflydigital.domain.model.Cita
import com.example.myfireflydigital.domain.repository.CitaRepository
import javax.inject.Inject

class GetCitaByIdUseCase @Inject constructor(private val repository: CitaRepository) {
    suspend operator fun invoke(id: Int) : Result<Cita?> = repository.getCitaById(id)
}