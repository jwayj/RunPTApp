plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 26
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
    }

    // 중복 파일 및 리소스 제외 설정
    packagingOptions {
        exclude("META-INF/proguard/androidx-annotations.pro")
        exclude("google/protobuf/type.proto")
        exclude("google/protobuf/descriptor.proto")
        exclude("META-INF/LICENSE")
        exclude("META-INF/NOTICE")
        exclude("com/graphhopper/custom_models/cargo_bike.json") // GraphHopper 중복 파일 제외
        exclude("com/graphhopper/util/bn_BN.txt")
        exclude("com/graphhopper/util/in_ID.txt")
        exclude("com/graphhopper/util/hsb.txt")
        exclude("com/graphhopper/util/bg.txt")
        exclude("com/graphhopper/util/fa.txt")
        exclude("com/graphhopper/custom_models/truck.json")
        exclude("com/graphhopper/util/mn.txt")
        exclude("com/graphhopper/util/eo.txt")
        exclude("com/graphhopper/util/sr_RS.txt")
        exclude("kotlin/coroutines/coroutines.kotlin_builtins")
        exclude("com/graphhopper/util/uk.txt")
        exclude("com/graphhopper/util/lt.txt")
        exclude("com/graphhopper/util/it.txt")
        exclude("com/graphhopper/util/es.txt")
        exclude("com/graphhopper/util/pl_PL.txt")
        exclude("kotlin/kotlin_builtins")

    }
    packaging {
        resources.excludes.addAll(
            listOf(
                "META-INF/proguard/androidx-annotations.pro",
                "google/protobuf/type.proto",
                "google/protobuf/descriptor.proto",
                "META-INF/LICENSE",
                "META-INF/NOTICE",
                "com/graphhopper/custom_models/cargo_bike.json", // GraphHopper 중복 파일 제외
                "com/graphhopper/util/hu_HU.txt", // 추가적으로 발견된 중복 파일 제외
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
    // GraphHopper 라이브러리 추가
    implementation(files("libs/graphhopper-web-10.0.jar"))
    implementation(files("libs/graphhopper-core-10.0-SNAPSHOT.jar"))

    // Firebase BOM (중복 방지)
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // OSMDroid 라이브러리
    implementation("org.osmdroid:osmdroid-android:6.1.14")
    implementation("org.osmdroid:osmdroid-mapsforge:6.1.14")
    implementation("org.osmdroid:osmdroid-geopackage:6.1.14")

    // SLF4J 로깅
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("org.slf4j:slf4j-android:1.7.30")
    implementation("org.slf4j:slf4j-simple:1.7.30")

    // Protobuf 라이브러리 통일
    implementation("com.google.protobuf:protobuf-javalite:3.21.12")

    // AndroidX 및 Material Design 라이브러리
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

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
        // GraphHopper에서 포함된 Guava 제거
        exclude(group = "com.google.guava", module = "guava")
        exclude(group = "com.google.guava", module = "listenablefuture")
        exclude(group = "com.google.errorprone", module = "error_prone_annotations")
        force("com.google.protobuf:protobuf-javalite:3.21.12")
        // GraphHopper에서 포함된 Protobuf 제거
        exclude(group = "com.google.protobuf", module = "protobuf-javalite")
        exclude(group = "com.google.protobuf", module = "protobuf-java")
        exclude(group = "com.google.protobuf", module = "protolite-well-known-types")
        exclude(group = "com.google.protobuf", module = "protobuf-lite")

        exclude(group = "org.jetbrains.kotlin", module = "kotlin-builtin")
        exclude(group = "com.google.protobuf", module = "protobuf-java")
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
        force("com.google.protobuf:protobuf-javalite:3.21.12")
    }
}