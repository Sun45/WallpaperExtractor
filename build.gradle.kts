// 配置日志级别以抑制模块相关的警告
gradle.startParameter.showStacktrace = org.gradle.api.logging.configuration.ShowStacktrace.INTERNAL_EXCEPTIONS
gradle.startParameter.warningMode = org.gradle.api.logging.configuration.WarningMode.None

// 配置Java编译器选项以抑制模块相关的警告
tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(
        listOf(
            "-Xlint:none",
            "-Xdoclint:none",
            "-nowarn"
        )
    )
    options.isWarnings = false
}

// 配置模块相关的任务日志级别
tasks.withType<org.gradle.jvm.tasks.Jar> {
    logging.captureStandardOutput(LogLevel.INFO)
    logging.captureStandardError(LogLevel.INFO)
}

plugins {
    java
    application
    id("org.javamodularity.moduleplugin") version "1.8.15"
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "3.0.1"
}

group = "cn.Sun45_"
version = "1.1.0"

repositories {
    mavenCentral()
    maven {
        url = uri("https://sandec.jfrog.io/artifactory/repo/")
    }
}

val junitVersion = "5.12.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("cn.sun45_.wallpaperextractor")
    mainClass.set("cn.sun45_.wallpaperextractor.WallpaperExtractorApp")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=GBK")
}

javafx {
    version = "21.0.6"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.web", "javafx.swing")
}

dependencies {
    implementation("org.controlsfx:controlsfx:11.2.2")
    implementation("com.dlsc.formsfx:formsfx-core:11.6.0") {
        exclude(group = "org.openjfx")
    }
    implementation("net.synedra:validatorfx:0.6.1") {
        exclude(group = "org.openjfx")
    }
    implementation("org.kordamp.ikonli:ikonli-javafx:12.3.1")
    implementation("org.kordamp.ikonli:ikonli-fontawesome5-pack:12.3.1")
    implementation("org.kordamp.bootstrapfx:bootstrapfx-core:0.4.0")
    implementation("eu.hansolo:tilesfx:21.0.9") {
        exclude(group = "org.openjfx")
    }
    implementation("com.dlsc.gemsfx:gemsfx:2.16.0")
    implementation("com.dustinredmond.fxtrayicon:FXTrayIcon:4.2.3")
    implementation("one.jpro.jproutils:tree-showing:0.2.2")
    implementation("org.locationtech.jts:jts-core:1.19.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jlink {
    imageZip.set(layout.buildDirectory.file("/distributions/app-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "app"
    }
}

tasks.register<Exec>("jpackageExe") {
    dependsOn("jlink")

    val iconPath = "assets/icon.ico"
    val imageDir = "build/image"
    val outputDir = "build/exe"

    commandLine = listOf<String>(
        "jpackage",
        "--type", "exe",
        "--dest", outputDir,
        "--module", "${application.mainModule.get()}/${application.mainClass.get()}",
        "--runtime-image", imageDir,
        "--win-shortcut",
        "--win-shortcut-prompt",
        "--win-menu",
        "--win-dir-chooser",
        "--name", "Wallpaper Extractor",
        "--vendor", "Sun45",
        "--app-version", version.toString(),
        "--icon", iconPath,
        "--install-dir", "WallpaperExtractor",
        "--java-options", "-Dfile.encoding=GBK"
//        , "--win-console"
    )

    doFirst {
        val outputDirFile = file(outputDir)
        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs()
        }
    }
}
