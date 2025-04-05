plugins {
    id("com.android.library") // application에서 library로 변경
}

android {
    namespace = "com.example.routing_module"
    compileSdk = 35 // 앱 모듈과 동일하게 변경

    defaultConfig {
        // applicationId는 library에서 제거
        minSdk = 33 // 앱 모듈과 동일하게 맞춤
        targetSdk = 35 // 앱 모듈과 동일하게 맞춤

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
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
        isCoreLibraryDesugaringEnabled = true // 추가
    }

    // Record 패키징 문제 해결
    packaging {
        resources.excludes.add("META-INF/INDEX.LIST")
        resources.excludes.add("META-INF/DEPENDENCIES")
    }
}

repositories {
    google()
    mavenCentral()
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
}

dependencies {
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation(platform("com.google.firebase:firebase-bom:33.9.0")) // 버전 통일

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")

    // GraphHopper 라이브러리
    implementation("com.graphhopper:graphhopper-core:10.0-SNAPSHOT") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }

    // SLF4J 및 Logback
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("org.slf4j:slf4j-android:1.7.30")

    // Desugaring 추가
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs_configuration:2.0.4")

    // 테스트 라이브러리
    testImplementation("junit:junit:4.13.2")
}


