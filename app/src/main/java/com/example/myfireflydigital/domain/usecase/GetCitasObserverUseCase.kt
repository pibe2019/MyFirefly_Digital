package com.example.myfireflydigital.domain.usecase

import com.example.myfireflydigital.domain.model.Cita
import com.example.myfireflydigital.domain.repository.CitaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCitasObserverUseCase @Inject constructor(private val repository: CitaRepository) {
    operator fun invoke() : Flow<List<Cita>> = repository.getCitasObserve()
}