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

    // Protobuf 충돌 해결을 위한 파일 제외 설정
    packagingOptions {
        exclude("META-INF/proguard/androidx-annotations.pro")
        exclude("google/protobuf/type.proto") // Protobuf 관련 중복 파일 제외
        exclude("google/protobuf/descriptor.proto") // 추가적으로 descriptor.proto도 제외
        exclude("META-INF/LICENSE")
        exclude("META-INF/NOTICE")
    }
    packaging {
        resources.excludes.add("google/protobuf/type.proto")
        resources.excludes.add("google/protobuf/descriptor.proto")
        resources.excludes.add("META-INF/proguard/androidx-annotations.pro")
        resources.excludes.add("META-INF/LICENSE")
        resources.excludes.add("META-INF/NOTICE")
    }
}

dependencies {
    // Firebase BOM (중복 방지)
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // OSMDroid 및 GraphHopper 의존성
    implementation("org.osmdroid:osmdroid-android:6.1.14")
    implementation("org.osmdroid:osmdroid-mapsforge:6.1.14")
    implementation("org.osmdroid:osmdroid-geopackage:6.1.14")
    implementation("com.graphhopper:graphhopper-core:10.2")

    // SLF4J (로그 처리)
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("org.slf4j:slf4j-android:1.7.30")

    // Protobuf 라이브러리 통일
    implementation("com.google.protobuf:protobuf-javalite:3.21.12") // protobuf-java 대신 javalite 사용

    // AndroidX Activity KTX (EdgeToEdge 포함)
    implementation("androidx.activity:activity-ktx:1.9.0")

    // Material Design 라이브러리 추가
    implementation("com.google.android.material:material:1.9.0")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")





    // 테스트용 라이브러리
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

// Protobuf 버전 강제 통일
configurations.all {
    resolutionStrategy {
        force("com.google.protobuf:protobuf-javalite:3.21.12") // 모든 의존성에서 동일한 버전 사용
        exclude(group = "com.google.protobuf", module = "protobuf-java") // protobuf-java 제외
    }
}




