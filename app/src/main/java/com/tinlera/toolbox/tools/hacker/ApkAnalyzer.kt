package com.tinlera.toolbox.tools.hacker

import com.tinlera.toolbox.core.RootBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ApkInfo(
    val packageName: String,
    val versionName: String = "",
    val versionCode: String = "",
    val permissions: List<String> = emptyList(),
    val activities: List<String> = emptyList(),
    val services: List<String> = emptyList(),
    val receivers: List<String> = emptyList(),
    val providers: List<String> = emptyList(),
    val nativeLibs: List<String> = emptyList(),
    val minSdk: String = "",
    val targetSdk: String = "",
    val signatures: List<String> = emptyList()
)

object ApkAnalyzer {

    /**
     * Get APK path for a package.
     */
    suspend fun getApkPath(packageName: String): String = withContext(Dispatchers.IO) {
        val result = RootBridge.execute("pm path $packageName").output
        result.lines().firstOrNull()?.removePrefix("package:")?.trim() ?: ""
    }

    /**
     * Dump full manifest info from an installed package using aapt/dumpsys.
     */
    suspend fun analyzePackage(packageName: String): ApkInfo = withContext(Dispatchers.IO) {
        val dump = RootBridge.execute("dumpsys package $packageName").output

        val permissions = mutableListOf<String>()
        val activities = mutableListOf<String>()
        val services = mutableListOf<String>()
        val receivers = mutableListOf<String>()
        val providers = mutableListOf<String>()
        var versionName = ""
        var versionCode = ""
        var minSdk = ""
        var targetSdk = ""

        var section = ""
        dump.lines().forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("versionName=") -> versionName = trimmed.removePrefix("versionName=")
                trimmed.startsWith("versionCode=") -> versionCode = trimmed.split(" ").first().removePrefix("versionCode=")
                trimmed.startsWith("minSdk=") -> minSdk = trimmed.removePrefix("minSdk=")
                trimmed.startsWith("targetSdk=") -> targetSdk = trimmed.removePrefix("targetSdk=")
                trimmed.contains("requested permissions:") -> section = "permissions"
                trimmed.contains("Activity Resolver Table:") -> section = "activities"
                trimmed.contains("Service Resolver Table:") -> section = "services"
                trimmed.contains("Receiver Resolver Table:") -> section = "receivers"
                trimmed.contains("ContentProvider") -> section = "providers"
                trimmed.startsWith("android.permission.") && section == "permissions" -> permissions.add(trimmed)
                trimmed.contains("/$packageName") && section == "activities" -> {
                    val act = trimmed.substringAfter("$packageName/").substringBefore(" ")
                    if (act.isNotBlank()) activities.add(act)
                }
            }
        }

        // Get native libs
        val apkPath = getApkPath(packageName)
        val libDir = apkPath.substringBeforeLast("/") + "/lib"
        val nativeLibs = RootBridge.execute("ls -R $libDir 2>/dev/null").output
            .lines().filter { it.endsWith(".so") }

        ApkInfo(
            packageName = packageName,
            versionName = versionName,
            versionCode = versionCode,
            permissions = permissions.distinct(),
            activities = activities.distinct(),
            services = services.distinct(),
            receivers = receivers.distinct(),
            providers = providers.distinct(),
            nativeLibs = nativeLibs,
            minSdk = minSdk,
            targetSdk = targetSdk
        )
    }

    /**
     * Search for strings inside an APK file.
     */
    suspend fun searchStrings(packageName: String, query: String): List<String> = withContext(Dispatchers.IO) {
        val apkPath = getApkPath(packageName)
        if (apkPath.isEmpty()) return@withContext emptyList()
        val result = RootBridge.execute("strings $apkPath 2>/dev/null | grep -i '$query' | head -100").output
        result.lines().filter { it.isNotBlank() }
    }

    /**
     * Detect known trackers by package name patterns.
     */
    fun detectTrackers(permissions: List<String>, activities: List<String>): List<String> {
        val trackerPatterns = mapOf(
            "com.google.firebase.analytics" to "Firebase Analytics",
            "com.google.android.gms.analytics" to "Google Analytics",
            "com.facebook.appevents" to "Facebook Analytics",
            "com.adjust.sdk" to "Adjust SDK",
            "com.appsflyer" to "AppsFlyer",
            "io.branch" to "Branch.io",
            "com.crashlytics" to "Crashlytics",
            "com.amplitude" to "Amplitude",
            "com.mixpanel" to "Mixpanel",
            "com.segment" to "Segment",
            "com.applovin" to "AppLovin (Ads)",
            "com.unity3d.ads" to "Unity Ads",
            "com.google.ads" to "Google Ads/AdMob",
            "com.mopub" to "MoPub Ads",
            "com.chartboost" to "Chartboost Ads",
            "com.ironsource" to "ironSource Ads"
        )

        val allStrings = (activities).joinToString(" ")
        return trackerPatterns.filter { (pattern, _) ->
            allStrings.contains(pattern, ignoreCase = true)
        }.values.toList()
    }

    /**
     * List all exported activities (launchable from outside).
     */
    suspend fun getExportedActivities(packageName: String): List<String> = withContext(Dispatchers.IO) {
        val result = RootBridge.execute("dumpsys package $packageName | grep -A1 'exported=true' | grep -oP '\\S+Activity'").output
        result.lines().filter { it.isNotBlank() }.distinct()
    }
}
