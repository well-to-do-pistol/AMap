plugins {
    alias(libs.plugins.androidApplication)

}

android {
    namespace = "com.llw.mapdemo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.llw.mapdemo"
        minSdk = 21
        targetSdk = 34
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

//    sourceSets{
//        main{
//            jniLibs.srcDirs = ['libs']
//        }
//    }
    sourceSets {
        getByName("main") {
            java.srcDir("src/main/java")
            resources.srcDir("src/main/resources")
            jniLibs.srcDirs("libs")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar,*.so"))))

//Google推荐的EasyPermission库
    implementation("pub.devrel:easypermissions:3.0.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(files("src\\main\\java\\libs\\AMap3DMap_10.0.700_AMapSearch_9.7.2_AMapLocation_6.4.5_20240508.jar"))
    implementation(files("src\\main\\java\\libs\\arm64-v8a\\libAMapSDK_MAP_v10_0_700.so"))
    implementation(files("src\\main\\java\\libs\\armeabi-v7a\\libAMapSDK_MAP_v10_0_700.so"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}