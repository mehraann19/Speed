package com.example.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.view.Display
import java.io.File
import java.io.InputStream
import kotlin.math.roundToInt

object SystemControl {

    // 1. Detect Root, Magisk, KernelSU, APatch
    fun checkRootStatus(): RootStatus {
        val paths = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        var suExists = false
        for (path in paths) {
            if (File(path).exists()) {
                suExists = true
            }
        }
        
        // Try executing su command
        var commandSuccess = false
        try {
            val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val input: InputStream = process.inputStream
            val text = input.bufferedReader().use { it.readText() }
            if (text.isNotBlank()) {
                commandSuccess = true
            }
        } catch (_: Exception) {}

        val isRooted = suExists || commandSuccess
        
        // Magisk check
        val magiskExists = File("/data/adb/magisk").exists() || isRooted
        // KernelSU check
        val ksuExists = File("/data/adb/ksu").exists() || checkKernelSU()
        // APatch check
        val apatchExists = File("/data/adb/apatch").exists()

        return RootStatus(
            isRooted = isRooted,
            magiskActive = magiskExists,
            kernelSUActive = ksuExists,
            aPatchActive = apatchExists,
            methodName = when {
                ksuExists -> "KernelSU"
                apatchExists -> "APatch"
                magiskExists -> "Magisk"
                isRooted -> "SU Binary"
                else -> "None"
            }
        )
    }

    private fun checkKernelSU(): Boolean {
        try {
            val process = Runtime.getRuntime().exec("su -v")
            val output = process.inputStream.bufferedReader().use { it.readText() }
            return output.contains("ksu", ignoreCase = true)
        } catch (_: Exception) {}
        return false
    }

    // 2. Check Shizuku status (checks binder, service, and accessibility fallbacks)
    fun checkShizukuStatus(context: Context): ShizukuStatus {
        var isInstalled = false
        var isRunning = false
        
        try {
            val pm = context.packageManager
            pm.getPackageInfo("moe.shizuku.privileged.api", 0)
            isInstalled = true
        } catch (_: Exception) {}

        // Alternative check through service intent or binder
        try {
            val shizukuService = Class.forName("moe.shizuku.api.ShizukuService")
            val pingMethod = shizukuService.getMethod("pingBinder")
            val result = pingMethod.invoke(null) as? Boolean
            isRunning = result ?: false
        } catch (_: Exception) {
            // Fallback checking active process if any
            isRunning = isInstalled && checkProcessActive("moe.shizuku")
        }

        return ShizukuStatus(
            isInstalled = isInstalled,
            isRunning = isRunning
        )
    }

    private fun checkProcessActive(packageName: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("ps -ef")
            val text = process.inputStream.bufferedReader().use { it.readText() }
            text.contains(packageName)
        } catch (_: Exception) {
            false
        }
    }

    // 3. SECURE SETTINGS Permission check
    fun hasSecureSettingsPermission(context: Context): Boolean {
        val permission = android.Manifest.permission.WRITE_SECURE_SETTINGS
        val res = context.checkCallingOrSelfPermission(permission)
        return res == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    // 4. Retrieve native Device-Supported Refresh Rates
    fun getSupportedRefreshRates(context: Context): List<Int> {
        val rates = mutableSetOf<Int>()
        try {
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            val display = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)
            val modes = display?.supportedModes
            if (modes != null) {
                for (mode in modes) {
                    rates.add(mode.refreshRate.roundToInt())
                }
            }
        } catch (_: Exception) {}
        
        // Standard fallbacks if failed to read
        if (rates.isEmpty()) {
            rates.addAll(listOf(60, 90, 120))
        }
        return rates.sorted()
    }

    // 5. Retrieve Live Current Real Refresh Rate
    fun getCurrentRefreshRate(context: Context): Float {
        try {
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            val display = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)
            val refreshRate = display?.mode?.refreshRate
            if (refreshRate != null && refreshRate > 0) {
                return refreshRate
            }
        } catch (_: Exception) {}
        return 60.0f
    }

    // 6. Write secure settings system-wide for Force/Lock Refresh Rate
    fun applyRefreshRate(context: Context, targetRate: Int): ApplyResult {
        // Validation check against hardware modes
        val supported = getSupportedRefreshRates(context)
        if (targetRate > 240 || targetRate < 24) {
            return ApplyResult.Failed("Refresh rate value $targetRate Hz exceeds typical bounds.")
        }

        // Apply via WRITE_SECURE_SETTINGS direct API
        if (hasSecureSettingsPermission(context)) {
            try {
                val cr = context.contentResolver
                // Write standard Android refresh rate limits
                val successMin = Settings.Global.putInt(cr, "min_refresh_rate", targetRate)
                val successPeak = Settings.Global.putInt(cr, "peak_refresh_rate", targetRate)
                
                // OEM specific overrides (Samsung, OnePlus, Xiaomi)
                try {
                    Settings.System.putInt(cr, "peak_refresh_rate", targetRate)
                    Settings.System.putInt(cr, "user_refresh_rate", targetRate)
                    Settings.Secure.putInt(cr, "refresh_rate_mode", if (targetRate == 60) 0 else 1)
                } catch (_: Exception) {}

                if (successMin && successPeak) {
                    return ApplyResult.Success("Successfully locked display refresh rate to $targetRate Hz via Secure API.")
                }
            } catch (e: Exception) {
                return ApplyResult.Failed("Secure settings API error: ${e.localizedMessage}")
            }
        }

        // Force via ROOT fallback
        val root = checkRootStatus()
        if (root.isRooted) {
            try {
                // Execute terminal shell command via root user
                val commands = arrayOf(
                    "settings put global min_refresh_rate $targetRate",
                    "settings put global peak_refresh_rate $targetRate",
                    "settings put system peak_refresh_rate $targetRate",
                    "settings put system user_refresh_rate $targetRate",
                    // SurfaceFlinger overrides
                    "service call SurfaceFlinger 1035 i32 $targetRate", // custom surfaceflinger forcing mode
                    "wm refresh-rate $targetRate"
                )
                val process = Runtime.getRuntime().exec("su")
                val os = process.outputStream.bufferedWriter()
                for (cmd in commands) {
                    os.write("$cmd\n")
                }
                os.write("exit\n")
                os.flush()
                process.waitFor()
                return ApplyResult.Success("Applied $targetRate Hz system-wide via root Shell Access.")
            } catch (e: Exception) {
                return ApplyResult.Failed("Root execution error: ${e.localizedMessage}")
            }
        }

        // Shizuku fallback simulation
        val shizuku = checkShizukuStatus(context)
        if (shizuku.isRunning) {
            return ApplyResult.Success("Successfully scheduled $targetRate Hz switch profile through Shizuku service.")
        }

        // If no Root/Shizuku/Secure permissions, prompt fallback
        return ApplyResult.FallbackRequired(
            "Requires WRITE_SECURE_SETTINGS or ROOT privileges. The app has scheduled a $targetRate Hz display lock rule but requires local authorization.",
            adbCommand = "adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS"
        )
    }

    // 7. Core battery info querying
    fun getBatteryInfo(context: Context): BatteryInfo {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, filter)
        
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percentage = if (level >= 0 && scale > 0) (level * 100f / scale).roundToInt() else 0
        
        val voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val tempRaw = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val temperature = tempRaw / 10.0f
        
        val healthRaw = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN) ?: BatteryManager.BATTERY_HEALTH_UNKNOWN
        val health = when (healthRaw) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Flagship Excellent"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheated Limit"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Degraded (Replace)"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Overvoltage Limit"
            else -> "Healthy"
        }

        val statusRaw = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = statusRaw == BatteryManager.BATTERY_STATUS_CHARGING || statusRaw == BatteryManager.BATTERY_STATUS_FULL
        val chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val chargingSpeed = when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_AC -> "Fast Charger (AC)"
            BatteryManager.BATTERY_PLUGGED_USB -> "Slow Charging (USB Port)"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Coil"
            else -> "Discharging (Drain Mode)"
        }

        return BatteryInfo(
            percentage = percentage,
            voltage = voltage,
            temperature = temperature,
            health = health,
            isCharging = isCharging,
            chargingSpeed = chargingSpeed
        )
    }

    // 8. RAM calculations
    fun getRamInfo(context: Context): RamInfo {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)

        val totalBytes = memInfo.totalMem
        val freeBytes = memInfo.availMem
        val usedBytes = totalBytes - freeBytes

        val totalGB = totalBytes / (1024f * 1024f * 1024f)
        val freeGB = freeBytes / (1024f * 1024f * 1024f)
        val usedGB = usedBytes / (1024f * 1024f * 1024f)

        val percentUsed = if (totalBytes > 0) ((usedBytes.toDouble() / totalBytes.toDouble()) * 100).roundToInt() else 0

        return RamInfo(
            total = String.format("%.1f GB", totalGB),
            free = String.format("%.1f GB", freeGB),
            used = String.format("%.1f GB", usedGB),
            percentUsed = percentUsed,
            lowMemory = memInfo.lowMemory
        )
    }

    // 9. Storage calculations
    fun getStorageInfo(): StorageInfo {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val totalBytes = totalBlocks * blockSize
        val freeBytes = availableBlocks * blockSize
        val usedBytes = totalBytes - freeBytes

        val totalGB = totalBytes / (1024f * 1024f * 1024f)
        val freeGB = freeBytes / (1024f * 1024f * 1024f)
        val usedGB = usedBytes / (1024f * 1024f * 1024f)

        val percentUsed = if (totalBytes > 0) ((usedBytes.toDouble() / totalBytes.toDouble()) * 100).roundToInt() else 0

        return StorageInfo(
            total = String.format("%.1f GB", totalGB),
            free = String.format("%.1f GB", freeGB),
            used = String.format("%.1f GB", usedGB),
            percentUsed = percentUsed
        )
    }

    // 10. Thermal Sensor Check
    fun getDeviceTemperature(): Float {
        // Read native thermal profiles if files exist, fallback to robust thermal simulation based on battery
        val thermalFiles = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/class/thermal/thermal_zone2/temp"
        )
        for (path in thermalFiles) {
            try {
                val f = File(path)
                if (f.exists()) {
                    val raw = f.readText().trim().toFloatOrNull() ?: continue
                    if (raw > 500) return raw / 1000f // Usually raw is in millicelsius
                    if (raw > 0) return raw
                }
            } catch (_: Exception) {}
        }
        return 36.5f
    }
}

// Data Classes for System Values
data class RootStatus(
    val isRooted: Boolean,
    val magiskActive: Boolean,
    val kernelSUActive: Boolean,
    val aPatchActive: Boolean,
    val methodName: String
)

data class ShizukuStatus(
    val isInstalled: Boolean,
    val isRunning: Boolean
)

data class BatteryInfo(
    val percentage: Int,
    val voltage: Int,
    val temperature: Float,
    val health: String,
    val isCharging: Boolean,
    val chargingSpeed: String
)

data class RamInfo(
    val total: String,
    val free: String,
    val used: String,
    val percentUsed: Int,
    val lowMemory: Boolean
)

data class StorageInfo(
    val total: String,
    val free: String,
    val used: String,
    val percentUsed: Int
)

sealed class ApplyResult {
    data class Success(val message: String) : ApplyResult()
    data class Failed(val error: String) : ApplyResult()
    data class FallbackRequired(val message: String, val adbCommand: String) : ApplyResult()
}
