@file:Suppress("SpellCheckingInspection", "ConvertToStringTemplate", "PropertyName")

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import de.undercouch.gradle.tasks.download.Download
import org.ajoberstar.grgit.Grgit
import java.time.format.DateTimeFormatter
import org.gradle.internal.os.OperatingSystem

plugins {
    kotlin("jvm")
    // kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.github.gmazzo.buildconfig") version "6.0.9"
    id("org.ajoberstar.grgit") version "5.3.3"
    id("de.undercouch.download") version "5.7.0"
}

group = "com.zoffcc.applications.undereat_material"
version = "1.0.9"
val appName = "undereat_material"

val build_with_appimage = false

var os: OperatingSystem? = null
var os_arch: String? = null
var os_java_home: String? = null
var os_java_runtime_version: String? = null
var os_java_vm_version: String? = null

try
{
    os = OperatingSystem.current()
    os_arch = System.getProperty("os.arch")
    os_java_home = System.getProperty("java.home")
    os_java_runtime_version = System.getProperty("java.runtime.version")
    os_java_vm_version = System.getProperty("java.vm.version")

    System.err.println("*** Building on ${os!!.familyName} / ${os!!.name} / ${os!!.version} / ${System.getProperty("os.arch")}.")
    System.err.println("*** os_java_home: $os_java_home.")
    System.err.println("*** os_java_runtime_version: $os_java_runtime_version.")
    System.err.println("*** os_java_vm_version: $os_java_vm_version.")
}
catch(_: Exception)
{
    System.err.println("some Error detecting OS for Java")
}

repositories {
    flatDir {
        dirs("customlibs")
    }
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

buildConfig {
    buildConfigField("String", "APP_NAME", "\"${project.name}\"")
    buildConfigField("String", "APP_VERSION", provider { "\"${project.version}\"" })
    buildConfigField("String", "PROJECT_VERSION", "\"${project.version}\"")
    buildConfigField("String", "KOTLIN_VERSION", "\"${kotlin.coreLibrariesVersion}\"")
    buildConfigField("String", "COMPOSE_VERSION", "\"${project.findProperty("compose.version")}\"")
    try
    {
        val grgit = if (extra.has("grgit")) null else the<Grgit>()
        try
        {
            buildConfigField("String", "GIT_BRANCH", "\"" + grgit!!.branch.current().fullName + "\"")
        }
        catch (e: Exception)
        {
            buildConfigField("String", "GIT_BRANCH", "\"" + "????" + "\"")
        }
        buildConfigField("String", "GIT_COMMIT_HASH", "\"" + grgit!!.head().abbreviatedId + "\"")
        buildConfigField("String", "GIT_COMMIT_DATE", "\"" + grgit.head().dateTime.
          format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\"")
        buildConfigField("String", "GIT_COMMIT_MSG", "\"" + grgit.head().shortMessage.
          replace("\"", "_").replace("\n", "_").
          replace("\\", "_").
          replace("\r", "_").take(40) + "\"")
    }
    catch (e: Exception)
    {
        try
        {
            buildConfigField("String", "GIT_BRANCH", "\"" + "????" + "\"")
        }
        catch (_: Exception)
        {
        }
        buildConfigField("String", "GIT_COMMIT_HASH", "\"" + "????" + "\"")
        buildConfigField("String", "GIT_COMMIT_DATE", "\"" + "????" + "\"")
        buildConfigField("String", "GIT_COMMIT_MSG", "\"" + "????" + "\"")
    }
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation(compose.desktop.common)
    implementation(compose.ui)
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material)
    implementation(compose.material3)
    @Suppress("OPT_IN_IS_NOT_ENABLED")
    @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
    implementation(compose.components.resources)
    //
    implementation(compose.materialIconsExtended)
    //
    //
    implementation("org.xerial:sqlite-jdbc:3.53.0.0")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("ca.gosyer:kotlin-multiplatform-appdirs:1.2.0")
    implementation("com.google.code.gson:gson:2.14.0")
    ///***///implementation("io.github.vinceglb:filekit-core:0.13.0")
    //implementation("io.github.vinceglb:filekit-dialogs:0.12.0")
    ///***///implementation("io.github.vinceglb:filekit-dialogs-compose:0.13.0")
    // implementation("io.github.vinceglb:filekit-coil:0.12.0")
}

val main_class_name = "UndereatMainKt"

val USE_JDK_VERSION = 17

compose.desktop {
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(USE_JDK_VERSION))
        }
    }

    kotlin {
        jvmToolchain(USE_JDK_VERSION)
    }

    application {

        mainClass = main_class_name
        // jvmArgs += listOf("-Xmx2G")
        // args += listOf("-customArgument")
        jvmArgs += listOf("-Dcom.apple.mrj.application.apple.menu.about.name=Undereat")
        jvmArgs += listOf("-Dapple.awt.application.name=Undereat")

        try
        {
            if (os!!.isLinux)
            {
                // on Linux set this for a possible skiko bug fix
                println("Linux -> skiko bug fix")
                jvmArgs += listOf("-Dskiko.vsync.enabled=false")
            }
        }
        catch(_: Exception)
        {
            println("error detecting OS -> for skiko bug fix")
        }

        buildTypes.release.proguard {
            optimize.set(false)
            obfuscate.set(false)
            configurationFiles.from("proguard-rules.pro")
        }

        nativeDistributions {
            packageName = appName
            packageVersion = "${project.version}"
            println("packageVersion=$packageVersion")
            description = "Undereat Material App"
            copyright = "© 2025 Zoff. All rights reserved."
            vendor = "Zoxcore"
            licenseFile.set(project.file("LICENSE"))
            println("licenseFile=" + project.file("LICENSE"))
            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))

            if (build_with_appimage)
            {
                System.err.println("#### build with AppImage ####")
                targetFormats(
                    TargetFormat.Msi, TargetFormat.Exe,
                    TargetFormat.Dmg,
                    TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.AppImage
                )
            }
            else
            {
                System.err.println("==== build without AppImage ====")
                targetFormats(
                    TargetFormat.Msi, TargetFormat.Exe,
                    TargetFormat.Dmg,
                    TargetFormat.Deb, TargetFormat.Rpm
                )
            }

            nativeDistributions {
                modules("java.instrument", "java.net.http", "java.prefs", "java.sql", "jdk.unsupported", "jdk.security.auth")
                // includeAllModules = true
            }

            val iconsRoot = project.file("resources")
            println("iconsRoot=$iconsRoot")
            macOS {
                // --- scrimage needs this set ONLY for macos arm
                // --- scrimage needs this set ONLY for macos arm
                // jvmArgs += listOf("-Dcom.sksamuel.scrimage.webp.platform=mac-arm64")
                // --- scrimage needs this set ONLY for macos arm
                // --- scrimage needs this set ONLY for macos arm
                println("iconFile=" + iconsRoot.resolve("icon-mac.icns"))
                iconFile.set(iconsRoot.resolve("icon-mac.icns"))
                bundleID = "com.zoffcc.applications.undereatmaterial"
                // HINT: https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Signing_and_notarization_on_macOS/README.md
                signing {
                    sign.set(false)
                    identity.set("Rupert Key")
                    keychain.set("keychain/macos_keychain")
                }
                //notarization {
                //    val providers = project.providers
                //    appleID.set(providers.environmentVariable("NOTARIZATION_APPLE_ID"))
                //    password.set(providers.environmentVariable("NOTARIZATION_PASSWORD"))
                //}
                runtimeEntitlementsFile.set(iconsRoot.resolve("runtime-entitlements.plist"))
                // dockName = ""
            }
            windows {
                iconFile.set(iconsRoot.resolve("icon-windows.ico"))
                println("iconFile=" + iconsRoot.resolve("icon-windows.ico"))
                menuGroup = "Undereat Material"
                // see https://wixtoolset.org/documentation/manual/v3/howtos/general/generate_guids.html
                // and https://www.guidgen.com/
                upgradeUuid = "6568ac85-3d27-48dc-a945-773bcdb694c4"
            }
            linux {
                iconFile.set(iconsRoot.resolve("icon-linux.png"))
                println("iconFile=" + iconsRoot.resolve("icon-linux.png"))
            }

            println("targetFormats=" + targetFormats)

            // XX // jvmArgs += "-splash:resources/splash_screen.png"
            // XX // jvmArgs += "-splash:${'$'}APPDIR/app/resources/splash_screen.png"
            // XX // jvmArgs += "-splash:" + iconsRoot.resolve("splash_screen.png")
            // -----------------------------------------------------------------
            // --> for .deb -->
            jvmArgs += "-splash:${'$'}APPDIR/resources/splash_screen.png"
            // --> for gradlew run --> // jvmArgs += "-splash:resources/splash_screen.png"
            // -----------------------------------------------------------------
            println("jvmArgs=" + jvmArgs)
            // val ENV = System.getenv()
            // println("ENV_all=" + ENV.keys)
        }
    }
}

val appImageTool = project.file("deps/appimagetool.AppImage")
val linuxAppDir = project.file("build/compose/binaries/main/app")
val desktopFile = project.file("resources/undereat_material.desktop")
val linuxIconFile = project.file("resources/icon-linux.png")

tasks.withType<org.gradle.jvm.tasks.Jar> {
    manifest {
        attributes["SplashScreen-Image"] = "splash_screen.png"
    }
}

tasks {
    val downloadAppImageBuilder by registering(Download::class) {
        src("https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-x86_64.AppImage")
        dest(appImageTool)
        overwrite(false)
        doFirst {
            exec {
                commandLine("mkdir", "-p", "deps/")
            }
        }
        doLast {
            exec {
                commandLine("chmod", "+x", "deps/appimagetool.AppImage")
            }
        }
    }

    val copyAppimageDesktopfile by registering(Exec::class) {
        environment("ARCH", "x86_64")
        commandLine("cp", "-v", desktopFile, linuxAppDir)
    }

    val copyAppimageIconfile by registering(Exec::class) {
        environment("ARCH", "x86_64")
        println("iconFile_src=" + linuxIconFile)
        println("iconFile_dst=" + "${linuxAppDir}/undereat_material.png")
        @Suppress("RemoveSingleExpressionStringTemplate", "RemoveCurlyBracesFromTemplate")
        println("appName=" + "${appName}")
        commandLine("cp", "-v", linuxIconFile, "${linuxAppDir}/undereat_material.png")
    }

    val setAppimageRunfile by registering(Exec::class) {
        workingDir = linuxAppDir
        commandLine("ln", "-sf", "undereat_material/bin/undereat_material", "AppRun")
    }

    val executeAppImageBuilder by registering(Exec::class) {
        dependsOn(downloadAppImageBuilder)
        dependsOn(copyAppimageDesktopfile)
        dependsOn(copyAppimageIconfile)
        dependsOn(setAppimageRunfile)
        environment("ARCH", "x86_64")
        @Suppress("RemoveCurlyBracesFromTemplate")
        println("cmd: " + "${appImageTool} ${linuxAppDir} $appName-${project.version}-x86_64.AppImage")
        commandLine(appImageTool, linuxAppDir, "$appName-${project.version}-x86_64.AppImage")
    }
}

// HINT: enable the witness checker
project.extensions.extraProperties["noChecksumWitness"] = "org.jetbrains.skiko:skiko-awt-runtime-linux"

apply(from = "gradle_witness_ng_desktop.gradle")

