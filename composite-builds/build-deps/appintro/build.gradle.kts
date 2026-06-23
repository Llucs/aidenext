plugins {
    id("com.android.library")
    id("com.aidenext.build")
}

android {
    namespace = "com.github.appintro"
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.fragment.ktx)
}