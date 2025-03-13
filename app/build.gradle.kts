plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }

    // Protobuf 충돌 해결을 위한 파일 제외 설정
    packagingOptions {
        exclude ("google/protobuf/type.proto")
        exclude ("META-INF/proguard/androidx-annotations.pro")
    }
}

dependencies {

    // Firebase BOM 사용 (중복 방지)
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // Android 기본 라이브러리
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // OSMDroid 및 GraphHopper 의존성
    implementation("org.osmdroid:osmdroid-android:6.1.14")
    implementation("org.osmdroid:osmdroid-mapsforge:6.1.14")
    implementation("org.osmdroid:osmdroid-geopackage:6.1.14")

    // GraphHopper 최신 버전 사용
    implementation("com.graphhopper:graphhopper-core:10.2")

    // SLF4J (로그 처리)
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("org.slf4j:slf4j-android:1.7.30")

    // Protobuf 버전 통일 (중복 방지)
    implementation("com.google.protobuf:protobuf-java:3.21.12")

    // 테스트용 라이브러리
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

// Protobuf 버전 강제 통일 (중복 방지)
configurations.all {
    resolutionStrategy {
        force ("com.google.protobuf:protobuf-java:3.21.12")
        force ("com.google.protobuf:protobuf-javalite:3.21.12")
    }
}
