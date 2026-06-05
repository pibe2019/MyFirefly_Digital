package com.example.myfireflydigital.domain.usecase

import com.example.myfireflydigital.domain.model.Cita
import com.example.myfireflydigital.domain.model.result.EstadoCita
import com.example.myfireflydigital.domain.repository.CitaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCitasObserverUseCase @Inject constructor(private val repository: CitaRepository) {
    operator fun invoke() : Flow<List<Cita>> = repository.getCitasObserve() //.map { it.filter { cita -> cita.estado != EstadoCita.CANCELADO } }
}