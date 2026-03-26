plugins {
    alias(libs.plugins.android.application)
    //alias(libs.plugins.kotlin.android)//agregado
    alias(libs.plugins.kotlin.compose)
    //serializacion, para la navegacion
    alias(libs.plugins.jetbrainsKotlinSerialization)
    //ID: degger hilt
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.dagger.hilt)
    // Aplicamos el plugin de secretos
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.example.myfireflydigital"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.myfireflydigital"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    /*kotlinOptions {
        jvmTarget = "11"
        // Habilita advertencias de deprecación en Kotlin
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-Xjsr305=strict", // Para nulabilidad estricta
            "-opt-in=kotlin.RequiresOptIn", // Si usas APIs experimentales
            "-Werror"// Convierte TODOS los warnings en errores (alternativa a -Xerror=warnings)
        )
    }*/

    buildFeatures {
        compose = true
        buildConfig = true//para plugin de secretos- // Habilitar BuildConfig para poder acceder a las variables desde código Kotlin:
    }
}
// ✅ Nueva forma en AGP 9.0.1, reemplaza kotlinOptions
kotlin {
    jvmToolchain(11)
}
secrets{//OPCIONAL
    //ignoreList.add("") // ignorara lo que pongas(IRA AL LOCAL.PROPERTIES, siempre y alli vera que ignora)
}
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    //traer iconos
    implementation(libs.compose.material.icons.extended)
    //ID : DAGGER-HILT
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    //HILT - NAVEGATION
    implementation(libs.androidx.hilt.navigation.compose)
    //para la serializacion en la navegacion
    implementation(libs.kotlinx.serialization.json)
    //NAVIGATION VERSON 3
    implementation(libs.bundles.navigation3)
    //ROOM
    implementation (libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    //RETROFit  VERSION 3
    implementation(libs.bundles.retrofit3)
    //para obtener las respuestas de la api de haber problemas
    implementation(libs.logging.interceptor)
    //DATASTORE
    implementation (libs.datastore.preferences)
    //HUBICACION, GEOCODING
    implementation(libs.play.services.location)
    //GOOGLE MAPS - con JetpackCompose(mapa en Compose)
    implementation(libs.maps.compose)
    //El SDK base de GOOGLE-PLAY SERVICES para MAPS (requerido por maps-compose)
    implementation(libs.play.services.maps)
    // Google Places SDK (autocomplete + detalles de lugar)
    implementation("com.google.android.libraries.places:places:4.1.0"){
        exclude(group = "androidx.vectordrawable", module = "vectordrawable-animated")
    }
    //Accompanist Permissions - manera profesional de pedir permisos
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")


    // UTILITY LIBRARY - Para polilíneas, clustering, etc. OPCIONALLLLLL
    //implementation("com.google.maps.android:maps-utils-ktx:5.1.0")
    // Coroutines play-services (.await() sobre Task<T>)
    //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    //test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}