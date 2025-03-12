pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // 설정 저장소를 우선시
    repositories {
        google()
        mavenCentral()
        mavenLocal() // MavenLocal 저장소 추가
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "My Application"
include(":app")

 