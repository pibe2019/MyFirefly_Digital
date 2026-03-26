package com.example.myfireflydigital.domain.exceptions

class GeoLocationPermissionDeniedException(val messageGeoLocationPermissionDenied : String = "Permiso de ubicación denegado") : Exception(messageGeoLocationPermissionDenied)
class GeoLocationDisableException(val messageGeoLocationDisable : String = "GPS deshabilitada") : Exception(messageGeoLocationDisable)
class GeoLocationUnknownException(val messageGeoLocation : String = "no se obtuvo ubicación") : Exception(messageGeoLocation)