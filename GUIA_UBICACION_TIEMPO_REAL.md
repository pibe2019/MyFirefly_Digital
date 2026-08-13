# Guía: Ubicación en tiempo real (`locationUpdates`)

> Ábrela en Android Studio y desplázate libremente.
> Resumen del análisis de tu implementación + lo que falta para conectarla end-to-end.

---

## 1. Estado actual

Tu método `locationUpdates()` en `data/source/LocationProvider.kt` está **bien escrito en sí**,
pero quedó **huérfano**: no lo conectaste con el resto de las capas. Por eso aparecen los avisos.

```
LocationProvider.locationUpdates()        ✅ existe (hay que corregirlo)
   → PlacesRepository.observeLocation()        ❌ falta (interfaz)
   → PlacesRepositoryImpl.observeLocation()    ❌ falta (impl)
   → ObserveLocationUseCase                    ❌ falta (nuevo archivo)
   → MapCitasViewModel (colectar Flow)         ❌ falta
```

---

## 2. Los problemas que te marca

### 2.1. Error rojo (lint) — falta `@SuppressLint("MissingPermission")`
En `LocationProvider.kt`, tanto `requestLocationUpdates(...)` como `removeLocationUpdates(...)`
exigen el permiso de ubicación. Tu `fetchCurrentLocation()` sí tiene la anotación, pero
`locationUpdates()` **no** → Android Studio te subraya en rojo:

> *Missing permissions required by FusedLocationProviderClient.requestLocationUpdates:*
> *android.permission.ACCESS_FINE_LOCATION*

### 2.2. Aviso gris — "function `locationUpdates` is never used"
Porque **no lo expusiste en la interfaz `PlacesRepository`**, ni hay use case, ni el ViewModel
lo colecta. Hoy es código muerto. Además, llamarlo directo desde una capa superior
**rompería Clean Architecture** (domain/ui no pueden tocar `data/source`).

### 2.3. (No te lo marca, pero está mal) — `intervalMS = 500` es agresivo
500 ms = hasta 2 lecturas GPS por segundo → **drena batería**.
Para seguir a una persona, lo sano son ~3–5 s con un mínimo de distancia.

---

## 3. Los 5 cambios para conectarlo (Clean Architecture)

### Paso 1 — Corregir `data/source/LocationProvider.kt`

```kotlin
@SuppressLint("MissingPermission")
fun locationUpdates(intervalMS: Long = 4000): Flow<LatLng> = callbackFlow {
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMS)
        .setMinUpdateIntervalMillis(2000)   // no más rápido que cada 2 s
        .setMinUpdateDistanceMeters(5f)     // solo si se movió ≥5 m
        .build()
    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { trySend(LatLng(it.latitude, it.longitude)) }
        }
    }
    fusedLocationProviderClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
    awaitClose { fusedLocationProviderClient.removeLocationUpdates(callback) }
}.flowOn(ioDispatcher)
```

### Paso 2 — `domain/repository/PlacesRepository.kt` (agregar a la interfaz)

```kotlin
fun observeLocation(): Flow<LatLng>   // sin suspend: devuelve un Flow
```

Import necesario:
```kotlin
import kotlinx.coroutines.flow.Flow
```

### Paso 3 — `data/repository/PlacesRepositoryImpl.kt` (implementar)

```kotlin
override fun observeLocation(): Flow<LatLng> = locationProvider.locationUpdates()
```

### Paso 4 — NUEVO archivo: `domain/usecase/ObserveLocationUseCase.kt`

```kotlin
package com.example.myfireflydigital.domain.usecase

import com.example.myfireflydigital.domain.repository.PlacesRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLocationUseCase @Inject constructor(
    private val placesRepository: PlacesRepository
) {
    operator fun invoke(): Flow<LatLng> = placesRepository.observeLocation()
}
```

### Paso 5 — `ui/mapcitas/MapCitasViewModel.kt` (inyectar y colectar)

Inyectar en el constructor:
```kotlin
private val observeLocationUseCase: ObserveLocationUseCase,
```

Nuevo Job para poder frenar el rastreo:
```kotlin
private var locationJob: Job? = null
```

Funciones de arranque / parada:
```kotlin
private fun startLocationTracking() {
    if (locationJob != null) return            // evita duplicar la colecta
    locationJob = viewModelScope.launch {
        observeLocationUseCase()
            .catch { _effect.send(UiEffect.ShowSnackbar(AppMessage.Error(it.toUiText()))) }
            .collect { latLng ->
                _uiMapState.update { it.copy(userLocation = latLng) }   // sin tocar el tick
            }
    }
}

private fun stopLocationTracking() {
    locationJob?.cancel()
    locationJob = null
}
```

Imports necesarios:
```kotlin
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
```

Parar el rastreo al destruir el ViewModel (evita fuga de batería):
```kotlin
override fun onCleared() {
    super.onCleared()
    stopLocationTracking()
}
```

---

## 4. Decisión de diseño importante

Tu `onMyLocation()` (one-shot) hace **dos cosas**: trae la ubicación **y centra la cámara**
(`locationUpdateTick`). El rastreo continuo **NO debe** centrar la cámara en cada emisión,
o el mapa "salta" sin parar y el usuario no podrá moverlo.

Por eso lo lógico es:

| Modo                | Acción                                              |
|---------------------|-----------------------------------------------------|
| One-shot (botón 📍) | Centra la cámara una vez (lo que ya tienes)         |
| Continuo            | Solo mueve el *marcador* "Mi ubicación"             |

(Por eso en `startLocationTracking()` actualizo `userLocation` **sin** incrementar
`locationUpdateTick`.)

**¿Cuándo arrancar / parar?**
- Arrancar `startLocationTracking()` después del primer fix en `onMyLocation()`,
  o cuando se concede el permiso.
- Parar en `onCleared()` del ViewModel.

---

## 5. Cumplimiento de arquitectura

- **Clean Architecture**: el `Flow` nace en `data/source`, sube por la interfaz (`domain`),
  pasa por un use case y el ViewModel solo orquesta estado. ✅
- **MVVM/MVI**: el estado sigue centralizado en `MapUiState`; los efectos one-time por `Channel`. ✅
- **SOLID**: cada pieza tiene una sola responsabilidad; depende de la interfaz, no de la impl. ✅
- **Punto a cuidar**: el ciclo de vida del `Job` (arranque/parada) para no fugar batería —
  responsabilidad del ViewModel, correcto.
- Complejidad ciclomática: todas las funciones nuevas quedan muy por debajo del límite (≤ 10-15). ✅

---

## 6. Pregunta pendiente

¿Prefieres **reemplazar** el modo one-shot por el continuo, o **mantener ambos**?
Recomendación: **mantener ambos** (one-shot para centrar cámara con el botón 📍,
continuo solo para mover el marcador).

---

## 7. Cómo gestionar los estados de la UI

### Regla de oro: 3 tipos de "estado", 3 lugares distintos

El error típico es meter todo en `MapUiState`. En MVI/MVVM cada cosa va en su sitio:

| Qué                                   | Dónde                                  | Por qué                                      |
|---------------------------------------|----------------------------------------|----------------------------------------------|
| Posición actual + si estoy siguiendo  | `MapUiState` (data class)              | Estado persistente que la UI pinta           |
| El `Job` del rastreo                  | Campo privado del ViewModel (`locationJob`) | Detalle de implementación, NO estado de UI |
| Errores del GPS durante el rastreo    | `UiEffect.ShowSnackbar` (Channel)      | Es un evento one-time, no un estado          |

### 7.1. Qué agregar a `MapUiState`

Solo **un** campo nuevo. `userLocation` y `locationUpdateTick` ya existen:

```kotlin
data class MapUiState (
    // ...lo que ya tienes...
    val userLocation: LatLng? = null,        // el rastreo continuo lo actualiza (mueve el marcador)
    val locationUpdateTick: Int = 0,         // SOLO one-shot lo incrementa (centra la cámara)
    val isTrackingLocation: Boolean = false  // NUEVO: ¿estoy siguiendo en tiempo real?
)
```

**La clave del diseño:** son DOS campos separados con DOS responsabilidades:
- `userLocation` → cambia en cada emisión → **mueve el marcador**.
- `locationUpdateTick` → **no** se toca en el rastreo continuo → la cámara **no salta**.
  Solo el botón 📍 lo incrementa para centrar una vez.

`isTrackingLocation` sirve para pintar el botón de "seguir" activo/inactivo
(Material 3: un `FilledIconToggleButton`).

### 7.2. Qué NO poner en el state

- El `locationJob: Job?` → campo privado del ViewModel.
- Los errores → van por `UiEffect.ShowSnackbar` (Channel), consistente con el resto de la app.

### 7.3. Nuevo evento en `MapCitasEvent`

```kotlin
data object OnToggleTracking : MapCitasEvent
```

### 7.4. ViewModel: el state se actualiza en start/stop

```kotlin
MapCitasEvent.OnToggleTracking -> onToggleTracking()

private fun onToggleTracking() {
    if (_uiMapState.value.isTrackingLocation) stopLocationTracking() else startLocationTracking()
}

private fun startLocationTracking() {
    if (locationJob != null) return
    _uiMapState.update { it.copy(isTrackingLocation = true) }        // estado ON
    locationJob = viewModelScope.launch {
        observeLocationUseCase()
            .catch {
                _uiMapState.update { it.copy(isTrackingLocation = false) }   // estado OFF ante error
                _effect.send(UiEffect.ShowSnackbar(AppMessage.Error(it.toUiText())))
            }
            .collect { latLng -> _uiMapState.update { it.copy(userLocation = latLng) } } // solo marcador
    }
}

private fun stopLocationTracking() {
    locationJob?.cancel()
    locationJob = null
    _uiMapState.update { it.copy(isTrackingLocation = false) }       // estado OFF
}
```

### 7.5. Cumplimiento de arquitectura

- Estado centralizado e inmutable (`copy`). ✅
- Efectos one-time por `Channel` (no en el state). ✅
- El `combine()` + `stateIn()` que ya tienes propaga el nuevo campo sin cambios. ✅
- El `Job` fuera del state = separación de responsabilidades (SOLID). ✅
