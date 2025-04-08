plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources.excludes.addAll(
            listOf(
                "META-INF/proguard/androidx-annotations.pro",
                "google/protobuf/type.proto",
                "google/protobuf/descriptor.proto",
                "META-INF/LICENSE",
                "META-INF/NOTICE",
                "META-INF/INDEX.LIST",
                "META-INF/DEPENDENCIES",
                "about.html",
                "com/graphhopper/custom_models/cargo_bike.json",
                "com/graphhopper/util/hu_HU.txt",
                "com/graphhopper/util/bn_BN.txt",
                "com/graphhopper/util/in_ID.txt",
                "com/graphhopper/util/hsb.txt",
                "com/graphhopper/util/bg.txt",
                "com/graphhopper/util/fa.txt",
                "com/graphhopper/custom_models/truck.json",
                "com/graphhopper/util/mn.txt",
                "com/graphhopper/util/eo.txt",
                "com/graphhopper/util/sr_RS.txt",
                "com/graphhopper/util/uk.txt",
                "com/graphhopper/util/lt.txt",
                "com/graphhopper/util/it.txt",
                "com/graphhopper/util/es.txt",
                "com/graphhopper/util/pl_PL.txt"
            )
        )
    }
}

dependencies {
    implementation(project(":routing-module"))

    implementation("com.google.guava:guava:32.1.3-android")

    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-appcheck")  // BOM 버전 사용

    // OSMDroid 라이브러리
    implementation("org.osmdroid:osmdroid-android:6.1.14")
    implementation("org.osmdroid:osmdroid-mapsforge:6.1.14")
    implementation("org.osmdroid:osmdroid-geopackage:6.1.14")

    // SLF4J 로깅
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("org.slf4j:slf4j-android:1.7.30")

    // Protobuf 라이브러리 통일
    implementation("com.google.protobuf:protobuf-javalite:3.21.12")

    // AndroidX 및 Material Design 라이브러리
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("androidx.webkit:webkit:1.7.0")

    // Desugaring 라이브러리
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs_configuration:2.0.4")

    implementation ("org.nanohttpd:nanohttpd:2.3.1")

    // 테스트용 라이브러리
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

// 전역 설정에서 중복된 클래스 및 파일 제외
configurations.all {
    resolutionStrategy {
        // Guava 버전 강제 지정
        force("com.google.guava:guava:32.1.3-android")
        force("com.google.protobuf:protobuf-javalite:3.21.12")
        force ("org.slf4j:slf4j-api:1.7.30")

        // 중복 모듈 제외
        exclude(group = "com.google.errorprone", module = "error_prone_annotations")
        exclude(group = "com.google.protobuf", module = "protobuf-javalite")
        exclude(group = "com.google.protobuf", module = "protobuf-java")
        exclude(group = "com.google.protobuf", module = "protolite-well-known-types")
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-builtin")
        exclude(group = "org.slf4j", module = "slf4j-simple")
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }
}
