package com.example.myfireflydigital.domain.exceptions

import com.example.myfireflydigital.R

class MapServiceException(val messageMapExcept: String="Error al interactuar con el servicio de mapas") : Exception(messageMapExcept)
class PlaceServiceException(val messagePlaceExcept: String="Error al interactuar con el servicio de lugares") : Exception(messagePlaceExcept)
class NoResultException(val messageNoResult: String= "") : Exception(messageNoResult)
class GeocoderNotAvailableException(val messageGeocoderNotAvailable: String = "") : Exception(messageGeocoderNotAvailable)
class GeocoderException(val messageGeocoder: String = "") : Exception(messageGeocoder)
