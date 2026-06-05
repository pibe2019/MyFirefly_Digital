package com.example.myfireflydigital.domain.exceptions

import android.content.IntentSender

class GeoLocationPermissionDeniedException(val messageGeoLocationPermissionDenied : String = "Permiso de ubicación denegado") : Exception(messageGeoLocationPermissionDenied)
class GeoLocationDisableException(val messageGeoLocationDisable : String = "GPS deshabilitada") : Exception(messageGeoLocationDisable)
class GeoLocationUnknownException(val messageGeoLocation : String = "no se obtuvo ubicación") : Exception(messageGeoLocation)

class GeoLocationResolvableException(val intentSender: IntentSender) : Exception("GPS deshabilitado — resoluble")

