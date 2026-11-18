pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // GeckoView (silnik Firefoxa)
        maven("https://maven.mozilla.org/maven2/")
    }
}

rootProject.name = "TubeAUTO"
