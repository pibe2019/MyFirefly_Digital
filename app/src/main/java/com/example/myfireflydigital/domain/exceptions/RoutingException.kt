package com.example.myfireflydigital.domain.exceptions

class RouteNotFoundException(val messageRoutingNotFound: String = "No se encontro ruta disponible") : Exception(messageRoutingNotFound)
class RouteServiceException(val messageRoutingService: String = "Error al obtener la ruta") : Exception(messageRoutingService)