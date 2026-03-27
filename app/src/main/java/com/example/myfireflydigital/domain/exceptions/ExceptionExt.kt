package com.example.myfireflydigital.domain.exceptions

import com.example.myfireflydigital.R
import com.example.myfireflydigital.domain.util.UiText

fun Throwable.toUiText(): UiText = when(this) {
    //DATA LOCAL
    is LocalStorageException -> UiText.StringResource(R.string.error_database,messageStorExcept)
    //SERVICESS EXTERNOS
    is MapServiceException -> UiText.StringResource(R.string.error_map_service,messageMapExcept)
    is PlaceServiceException -> UiText.DynamicString(messagePlaceExcept)
    is NoResultException -> UiText.StringResource(R.string.error_no_result)
    is GeocoderNotAvailableException -> UiText.StringResource(R.string.error_geocoder_not_available)
    is GeocoderException -> UiText.DynamicString(messageGeocoder)
    //GEOLOCALIZATION
    is GeoLocationPermissionDeniedException -> UiText.StringResource(R.string.error_geo_location_permission_denied)
    is GeoLocationDisableException -> UiText.StringResource(R.string.error_geo_location_disable)
    is GeoLocationUnknownException -> UiText.DynamicString(messageGeoLocation)
    is RouteNotFoundException -> UiText.StringResource(R.string.error_route_not_found)
    is RouteServiceException -> UiText.StringResource(R.string.error_route_service,messageRoutingService)
    //DESCONOCIDO
    else -> {
        message?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.error_unknown)
    }
}