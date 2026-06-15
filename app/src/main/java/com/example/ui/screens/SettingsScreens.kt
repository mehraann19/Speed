package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.*
import kotlin.random.Random
import com.example.ui.theme.*
import com.example.util.*
import com.example.viewmodel.DeviceInfoApp
import com.example.viewmodel.SpeedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// Navigation route constant values
object Routes {
    const val HOME = "settings_home"
    const val DISPLAY = "display_settings"
    const val PERFORMANCE = "performance_settings"
    const val GAMING = "gaming_center"
    const val REFRESH_RATE = "refresh_rate_forcing"
    const val DETAILED_MONITOR = "monitoring_dashboard"
    const val ROOT_TOOLS = "root_advanced_tools"
    const val SHIZUKU_TOOLS = "shizuku_tools"
    const val CUSTOM_PROFILES = "custom_profiles"
    const val ABOUT_PHONE = "about_phone"
}

@Composable
fun SettingsNavHost(
    viewModel: SpeedViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier.background(AmoledBlack)
    ) {
        composable(Routes.HOME) {
            SettingsHomeScreen(navController = navController, viewModel = viewModel)
        }
        composable(Routes.DISPLAY) {
            DisplaySettingsScreen(navController = navController, viewModel = viewModel)
        }
        composable(Routes.PERFORMANCE) {
            PerformanceSettingsScreen(navController = navController, viewModel = viewModel)
        }
        composable(Routes.GAMING) {
            GamingCenterScreen(navController = navController, viewModel = viewModel)
        }
        composable(Routes.REFRESH_RATE) {
            RefreshRateForcingScreen(navController = navController, viewModel = viewModel)
        }
        composable(Routes.DETAILED_MONITOR) {
            MonitoringDashboardScreen(navController = navController, viewModel = viewModel)
        }
        composable(Routes.ROOT_TOOLS) {
            RootToolsScreen(navController = navController, viewModel = viewModel)
        }
        composable(Routes.SHIZUKU_TOOLS) {
            ShizukuToolsScreen(navController = navController, viewModel = viewModel)
        }
        composable(Routes.CUSTOM_PROFILES) {
            CustomProfilesScreen(navController = navController, viewModel = viewModel)
        }
        composable(Routes.ABOUT_PHONE) {
            AboutPhoneScreen(navController = navController, viewModel = viewModel)
        }
    }
}

// -------------------------------------------------------------
// REUSABLE GLASSMORPHISM CARD COMPONENT
// -------------------------------------------------------------
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderGlow: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderColor = if (borderGlow) DarkCardBorderGlow else DarkCardBorder
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(DarkCardBg)
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Column {
            content()
        }
    }
}

// -------------------------------------------------------------
// CORE OVERRIDE DIALOG / NOTIFICATION FOR APPLYING SHELL EFFECTS
// -------------------------------------------------------------
@Composable
fun ApplyOverlayStatus(viewModel: SpeedViewModel) {
    val loading by viewModel.isAppliedLoading.collectAsState()
    val lastResult by viewModel.lastApplyResult.collectAsState()
    val localUriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    if (loading) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            containerColor = DarkCardBg,
            textContentColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        color = FlagshipCyan,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Speed Overlord System",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    "Calibrating system display drivers, adjusting SurfaceFlinger rendering pipeline, and resetting graphics lock frequencies. Please wait...",
                    color = SlateGrey,
                    fontSize = 14.sp
                )
            },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.border(1.dp, DarkCardBorderGlow, RoundedCornerShape(24.dp))
        )
    }

    lastResult?.let { result ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissLastResult() },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissLastResult() }) {
                    Text("Decline", color = SlateGrey)
                }
            },
            dismissButton = {
                if (result is ApplyResult.FallbackRequired) {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(result.adbCommand))
                            Toast.makeText(context, "ADB shell command copied completely!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FlagshipCyan, contentColor = Color.Black)
                    ) {
                        Text("Copy ADB", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { viewModel.dismissLastResult() },
                        colors = ButtonDefaults.buttonColors(containerColor = FlagshipCyan, contentColor = Color.Black)
                    ) {
                        Text("Acknowledge", fontWeight = FontWeight.Bold)
                    }
                }
            },
            containerColor = DarkCardBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when(result) {
                            is ApplyResult.Success -> Icons.Default.CheckCircle
                            is ApplyResult.Failed -> Icons.Default.Error
                            is ApplyResult.FallbackRequired -> Icons.Default.Lock
                        },
                        contentDescription = null,
                        tint = when(result) {
                            is ApplyResult.Success -> AccentSuccess
                            is ApplyResult.Failed -> AccentFailure
                            is ApplyResult.FallbackRequired -> FlagshipOrange
                        },
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when(result) {
                            is ApplyResult.Success -> "Shell Pipe Success"
                            is ApplyResult.Failed -> "Controller Blocked"
                            is ApplyResult.FallbackRequired -> "Permission Required"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = when(result) {
                            is ApplyResult.Success -> (result as ApplyResult.Success).message
                            is ApplyResult.Failed -> (result as ApplyResult.Failed).error
                            is ApplyResult.FallbackRequired -> (result as ApplyResult.FallbackRequired).message
                        },
                        color = SlateGrey,
                        fontSize = 14.sp
                    )
                    if (result is ApplyResult.FallbackRequired) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E1E24))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = (result as ApplyResult.FallbackRequired).adbCommand,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = FlagshipCyan,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Copy this command and run it in an ADB shell from your PC or wireless debugging terminal to unlock full 120Hz system integration without root.",
                            fontSize = 12.sp,
                            color = SlateGrey
                        )
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.border(
                width = 1.dp,
                color = when(result) {
                    is ApplyResult.Success -> Color(0x3B00E676)
                    is ApplyResult.Failed -> Color(0x3BFF1744)
                    is ApplyResult.FallbackRequired -> Color(0x3BFF5E00)
                },
                shape = RoundedCornerShape(24.dp)
            )
        )
    }
}

// -------------------------------------------------------------
// SCREEN 1: SETTINGS HOME SCREEN (Apple iOS & Samsung One UI layout)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHomeScreen(
    navController: NavController,
    viewModel: SpeedViewModel
) {
    val haptic = LocalHapticFeedback.current
    var query by remember { mutableStateOf("") }
    val activeProf by viewModel.activeProfile.collectAsState()
    val rate by viewModel.realtimeRefreshRate.collectAsState()
    val fps by viewModel.realtimeFps.collectAsState()
    val battery by viewModel.batteryInfo.collectAsState()
    val root by viewModel.rootStatus.collectAsState()
    val shizuku by viewModel.shizukuStatus.collectAsState()

    ApplyOverlayStatus(viewModel = viewModel)

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            "Speed Console",
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Default,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            "By Mehraann • Flagship System Utility",
                            style = MaterialTheme.typography.labelMedium,
                            color = FlagshipCyan,
                            letterSpacing = 1.sp,
                            modifier = Modifier.alpha(0.85f)
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = AmoledBlack,
                    scrolledContainerColor = DarkCardBg,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.optimizeMemory()
                    }) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = "Quick Boost", tint = FlagshipOrange)
                    }
                }
            )
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Real-Time HUD Dashboard Segment
            item {
                GlassCard(borderGlow = true) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "REAL-TIME MONITOR",
                                style = MaterialTheme.typography.labelSmall,
                                color = FlagshipCyan,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Current Display: ${rate.roundToInt()}Hz",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Active Preset: ${activeProf?.name ?: "Default Android"}",
                                color = SlateGrey,
                                fontSize = 13.sp
                            )
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF13131A))
                                .border(1.5.dp, DarkCardBorderGlow, CircleShape)
                                .clickable {
                                    navController.navigate(Routes.DETAILED_MONITOR)
                                }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$fps",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AccentSuccess,
                                    lineHeight = 22.sp
                                )
                                Text(
                                    "FPS",
                                    fontSize = 11.sp,
                                    color = SlateGrey,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = DarkCardBorder)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatusHUDGridItem("Temp", "${viewModel.deviceTemp.collectAsState().value}°C", Icons.Default.Thermostat, FlagshipOrange)
                        StatusHUDGridItem("Battery", "${battery.percentage}%", Icons.Default.BatteryChargingFull, AccentSuccess)
                        StatusHUDGridItem(
                            "Secure API", 
                            if (root.isRooted) "Rooted APatch" else if (shizuku.isRunning) "Shizuku Link" else "Awaiting PM",
                            Icons.Default.Security,
                            if (root.isRooted || shizuku.isRunning) AccentSuccess else FlagshipOrange
                        )
                    }
                }
            }

            // iOS & One UI Styled Search Bar
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search system tools, profiles, overrides...", color = SlateGrey) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SlateGrey) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear text", tint = SlateGrey)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkCardBg,
                        unfocusedContainerColor = DarkCardBg,
                        focusedBorderColor = FlagshipCyan,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Categorized iOS-Style Rows Group 1: CORE HARDWARE OVERRIDES
            item {
                CategoryHeading("Display & Render Parameters")
            }
            item {
                SettingsCategoryCard {
                    SettingsRowItem("Refresh Rate Control", "Force lock, Override, Adaptive scales", Icons.Default.Speed, FlagshipCyan, "Default rate overrides") {
                        navController.navigate(Routes.REFRESH_RATE)
                    }
                    HorizontalDivider(color = Color(0x13FFFFFF), modifier = Modifier.padding(start = 56.dp))
                    SettingsRowItem("Display Configuration", "Brightness bounds, HDR scans, parameters", Icons.Default.DisplaySettings, Color(0xFF2979FF), "60Hz to 120Hz configs") {
                        navController.navigate(Routes.DISPLAY)
                    }
                }
            }

            // Group 2: PERFORMANCES
            item {
                CategoryHeading("Performance & Optimization")
            }
            item {
                SettingsCategoryCard {
                    SettingsRowItem("Performance Presets", "Gaming mode, Battery Preserves, CPU Governors", Icons.Default.SettingsSuggest, FlagshipOrange, "120Hz Hyper preset") {
                        navController.navigate(Routes.PERFORMANCE)
                    }
                    HorizontalDivider(color = Color(0x13FFFFFF), modifier = Modifier.padding(start = 56.dp))
                    SettingsRowItem("Gaming Center Profiles", "Per-app locks, floating gauge panel rules", Icons.Default.SportsEsports, GamingViolet, "Dynamic profile maps") {
                        navController.navigate(Routes.GAMING)
                    }
                    HorizontalDivider(color = Color(0x13FFFFFF), modifier = Modifier.padding(start = 56.dp))
                    SettingsRowItem("System Dashboard", "Live Canvas graphs of memory, CPU, GPU, FPS", Icons.Default.TrendingUp, AccentSuccess, "Realtime updates") {
                        navController.navigate(Routes.DETAILED_MONITOR)
                    }
                }
            }

            // Group 3: SECURITY & CONTROLS
            item {
                CategoryHeading("Elevated Core Controls")
            }
            item {
                SettingsCategoryCard {
                    SettingsRowItem("Root Command Center", "Magisk, APatch, SurfaceFlinger overrides", Icons.Default.Code, Color(0xFFFF1744), "Advanced kernel dials") {
                        navController.navigate(Routes.ROOT_TOOLS)
                    }
                    HorizontalDivider(color = Color(0x13FFFFFF), modifier = Modifier.padding(start = 56.dp))
                    SettingsRowItem("Shizuku ADB Alternative", "Secure settings permissions wireless bindings", Icons.Default.SettingsEthernet, Color(0xFFF1C40F), "Non-root driver hooks") {
                        navController.navigate(Routes.SHIZUKU_TOOLS)
                    }
                    HorizontalDivider(color = Color(0x13FFFFFF), modifier = Modifier.padding(start = 56.dp))
                    SettingsRowItem("Custom Tuning Presets", "Create and store custom governor configs", Icons.Default.Tune, Color(0xFF00E676), "Room database logs") {
                        navController.navigate(Routes.CUSTOM_PROFILES)
                    }
                }
            }

            // Group 4: ABOUT SYSTEM
            item {
                CategoryHeading("Device Information")
            }
            item {
                SettingsCategoryCard {
                    SettingsRowItem("About Device", "Codename, Kernel, battery health, stats audit", Icons.Default.Info, Color(0xFF7F8C8D), "Samsung Spec Matrix") {
                        navController.navigate(Routes.ABOUT_PHONE)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Speed Executive Utility • v1.0.0 Stable\nCrafted with Precision by Mehraann",
                    textAlign = TextAlign.Center,
                    color = SlateGrey,
                    fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth().alpha(0.6f)
                )
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun StatusHUDGridItem(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(title, color = SlateGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CategoryHeading(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = SlateGrey,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(start = 6.dp, bottom = 4.dp, top = 6.dp)
    )
}

@Composable
fun SettingsCategoryCard(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(DarkCardBg)
            .border(1.dp, Color(0x1EFFFFFF), RoundedCornerShape(24.dp))
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun SettingsRowItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    tag: String = "",
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (tag.isNotEmpty() && Random.nextFloat() > 0.95f) { // Random minor visual flair
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(FlagshipOrange)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text("PRO", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            }
            Text(
                text = subtitle,
                color = SlateGrey,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = SlateGrey,
            modifier = Modifier.size(18.dp)
        )
    }
}

// -------------------------------------------------------------
// SCREEN 2: REFRESH RATE CONTROL SCREEN
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshRateForcingScreen(
    navController: NavController,
    viewModel: SpeedViewModel
) {
    val context = LocalContext.current
    val supportedRates = remember { SystemControl.getSupportedRefreshRates(context) }
    val currentRateFlow = viewModel.realtimeRefreshRate.collectAsState()
    val activeProfile by viewModel.activeProfile.collectAsState()
    val allProfiles by viewModel.allProfiles.collectAsState()
    val hasPerm = SystemControl.hasSecureSettingsPermission(context)
    val rootStatus by viewModel.rootStatus.collectAsState()
    val shizukuStatus by viewModel.shizukuStatus.collectAsState()

    ApplyOverlayStatus(viewModel = viewModel)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Display Refresh Rate Control", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack, titleContentColor = Color.White)
            )
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Permission Audit Diagnostic Banner
            item {
                GlassCard(borderGlow = !hasPerm) {
                    val statusText = if (hasPerm) "SECURE WRITE LINKED" else if (rootStatus.isRooted) "ROOT ACCESS READY" else "SECURE API LOCKED"
                    val statusColor = if (hasPerm || rootStatus.isRooted) AccentSuccess else FlagshipOrange
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = statusColor,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (hasPerm || rootStatus.isRooted) 
                            "Hardware control channels are online. Refresh rate locking commands will execute instantly system-wide."
                            else "Device locking requires WRITE_SECURE_SETTINGS API clearance. Review non-root Shizuku binding guides.",
                        fontSize = 13.sp,
                        color = SlateGrey
                    )
                }
            }

            // Supported Rates Detected Group
            item {
                CategoryHeading("Officially Detected Hardware Rates")
            }
            item {
                GlassCard {
                    Text(
                        "Your screen reports compatibility with these exact vertical refresh rates natively:",
                        color = SlateGrey,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (rate in supportedRates) {
                            val isCurrent = currentRateFlow.value.roundToInt() == rate
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isCurrent) FlagshipCyan else Color(0x0EFFFFFF))
                                    .border(1.dp, if (isCurrent) Color.White else DarkCardBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        // Attempt individual target toggle forcing
                                        val mockProf = RefreshProfile(
                                            id = 999,
                                            name = "Manual override $rate Hz",
                                            description = "Override forced to $rate Hz natively.",
                                            targetRefreshRate = rate
                                        )
                                        viewModel.applyProfile(mockProf)
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "$rate",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = if (isCurrent) Color.Black else Color.White
                                    )
                                    Text(
                                        "Hz",
                                        fontSize = 11.sp,
                                        color = if (isCurrent) Color.Black else SlateGrey
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Presets List SELECTORS
            item {
                CategoryHeading("Preset Forcing Engine")
            }
            items(allProfiles) { profile ->
                val isSelected = profile.id == (activeProfile?.id ?: -1)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) DarkCardBg else Color(0xFF040406))
                        .border(
                            width = 1.dp,
                            color = if (isSelected) FlagshipCyan else DarkCardBorder,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.applyProfile(profile) }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.applyProfile(profile) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = FlagshipCyan,
                                unselectedColor = SlateGrey
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    profile.name,
                                    color = if (isSelected) FlagshipCyan else Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF1B1B22))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("${profile.targetRefreshRate}Hz", fontSize = 10.sp, color = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(profile.description, color = SlateGrey, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Flagship System Switches Toggles
            item {
                CategoryHeading("Display Control Drivers")
            }
            item {
                GlassCard {
                    var lockPerm by remember { mutableStateOf(true) }
                    var disableOem by remember { mutableStateOf(false) }
                    var gameLock by remember { mutableStateOf(true) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Disable Adaptive Refresh Rate", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Stops display scaling down to 1Hz or 10Hz, locking continuous response.", color = SlateGrey, fontSize = 11.sp)
                        }
                        Switch(
                            checked = lockPerm,
                            onCheckedChange = { lockPerm = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = FlagshipCyan)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0x13FFFFFF))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Disable OEM Hardware Switching", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Blocks custom thermal or battery-saver engine clamps dynamically.", color = SlateGrey, fontSize = 11.sp)
                        }
                        Switch(
                            checked = disableOem,
                            onCheckedChange = { disableOem = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = FlagshipCyan)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0x13FFFFFF))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Continuous Gaming Refresh Lock", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Bypasses default 60Hz limits imposed on games, locking rate to maximum (120Hz/144Hz) system wide.", color = SlateGrey, fontSize = 11.sp)
                        }
                        Switch(
                            checked = gameLock,
                            onCheckedChange = { gameLock = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = FlagshipCyan)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SCREEN 3: MOUNT / DISPLAY SETTINGS
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplaySettingsScreen(
    navController: NavController,
    viewModel: SpeedViewModel
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Display Optimization Diagnostics", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack, titleContentColor = Color.White)
            )
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GlassCard(borderGlow = true) {
                    Text(
                        "FLAGSHIP PANEL SPEC",
                        style = MaterialTheme.typography.labelSmall,
                        color = FlagshipCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    DisplayMatrixRow("Touch Sampling Rate", "360Hz - Ultra Gaming Standard")
                    DisplayMatrixRow("HDR Compatible", "HDR10+ Dynamic OLED Compliant")
                    DisplayMatrixRow("Color Optimization", "DCI-P3 Expanded Saturated Space")
                    DisplayMatrixRow("Active Display Mode", "AMOLED Panel RGB Stripe Layout")
                    DisplayMatrixRow("Brightness Ranges", "Ultra Dynamic (Up to 1800 nits)")
                }
            }

            item {
                CategoryHeading("Display Scan Matrix")
            }
            item {
                GlassCard {
                    var valBrightness by remember { mutableFloatStateOf(65f) }
                    var smartTouch by remember { mutableStateOf(true) }
                    var extraDim by remember { mutableStateOf(false) }

                    Text("Touch Sampling Rate Calibration", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    Text("Forces dual-sensor layer capture to lock latency at lowest bounds.", color = SlateGrey, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Forced Touch Polling Standard (360Hz)", color = Color.White, fontSize = 13.sp)
                        Switch(
                            checked = smartTouch,
                            onCheckedChange = { smartTouch = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = FlagshipCyan)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0x13FFFFFF))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Flagship Extra Dim Module", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    Text("Applies a hardware color shader to render displays beneath safe system limits.", color = SlateGrey, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Super-Low Luminance Dimmer", color = Color.White, fontSize = 13.sp)
                        Switch(
                            checked = extraDim,
                            onCheckedChange = { extraDim = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = FlagshipCyan)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0x13FFFFFF))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Manual Calibration Brightness", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    Text("Scale: ${valBrightness.roundToInt()}% for extreme outdoor display limits.", color = SlateGrey, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Slider(
                        value = valBrightness,
                        onValueChange = { valBrightness = it },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = FlagshipCyan,
                            activeTrackColor = FlagshipCyan,
                            inactiveTrackColor = Color(0x1EFFFFFF)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun DisplayMatrixRow(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = SlateGrey, fontSize = 13.sp)
        Text(subtitle, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
    HorizontalDivider(color = Color(0x0EFFFFFF))
}

// -------------------------------------------------------------
// SCREEN 4: PERFORMANCE SETTINGS
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceSettingsScreen(
    navController: NavController,
    viewModel: SpeedViewModel
) {
    val activeProfile by viewModel.activeProfile.collectAsState()
    val allProfiles by viewModel.allProfiles.collectAsState()

    ApplyOverlayStatus(viewModel = viewModel)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Core Performance Drivers", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack, titleContentColor = Color.White)
            )
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GlassCard(borderGlow = true) {
                    Text(
                        "SYSTEM OPTIMIZER",
                        style = MaterialTheme.typography.labelSmall,
                        color = FlagshipOrange,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Optimize system governors and garbage-collection limits with a single click. Ideal for restoring 120Hz frames before long game drives.",
                        fontSize = 13.sp,
                        color = SlateGrey
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.optimizeMemory() },
                            colors = ButtonDefaults.buttonColors(containerColor = FlagshipOrange, contentColor = Color.Black),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Boost RAM", fontWeight = FontWeight.Bold)
                        }
                        
                        Button(
                            onClick = { viewModel.executeCacheClear() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E24), contentColor = Color.White),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0x1BFFFFFF))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear Cache")
                        }
                    }
                }
            }

            item {
                CategoryHeading("Preset Selection Tuning")
            }
            items(allProfiles) { profile ->
                val isSelected = profile.id == (activeProfile?.id ?: -1)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) DarkCardBg else Color(0xFF040406))
                        .border(
                            width = 1.dp,
                            color = if (isSelected) FlagshipOrange else DarkCardBorder,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.applyProfile(profile) }
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) FlagshipOrange.copy(alpha = 0.15f) else Color(0x0EFFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (profile.cpuGovernor) {
                                    "performance" -> Icons.Default.Bolt
                                    "powersave" -> Icons.Default.BatterySaver
                                    else -> Icons.Default.ToggleOn
                                },
                                contentDescription = null,
                                tint = if (isSelected) FlagshipOrange else SlateGrey
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                profile.name,
                                color = if (isSelected) FlagshipOrange else Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Governor: ${profile.cpuGovernor.uppercase()} • Target: ${profile.targetRefreshRate}Hz", color = SlateGrey, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SCREEN 5: GAMING CENTER SCREEN
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamingCenterScreen(
    navController: NavController,
    viewModel: SpeedViewModel
) {
    val searchApps by viewModel.filteredApps.collectAsState()
    val appProfiles by viewModel.allAppProfiles.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showConfigDialog by remember { mutableStateOf<DeviceInfoApp?>(null) }
    var inputRate by remember { mutableStateOf("120") }
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gaming Center Dashboard", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack, titleContentColor = Color.White)
            )
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GlassCard(borderGlow = true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GamingViolet.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SportsEsports, contentDescription = null, tint = GamingViolet, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Flagship Game Booster", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                            Text("Locks displays at maximum rate (120Hz/144Hz) when custom games load.", color = SlateGrey, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Custom app profiles built list
            item {
                CategoryHeading("Installed Gaming Exceptions")
            }
            if (appProfiles.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkCardBg)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SportsEsports, contentDescription = null, tint = SlateGrey, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No gaming constraints defined yet.", color = SlateGrey, fontSize = 13.sp)
                            Text("Add an app exception from the list below.", color = SlateGrey, fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            } else {
                items(appProfiles) { appProf ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkCardBg)
                            .border(1.dp, GamingViolet.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(appProf.appName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(appProf.packageName, color = SlateGrey, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(180.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GamingViolet)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("${appProf.refreshRate} Hz", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(onClick = { viewModel.removeAppProfile(appProf) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete app profile", tint = AccentFailure)
                            }
                        }
                    }
                }
            }

            // Search Filter and selection list
            item {
                CategoryHeading("Device App Directory Exception Mapper")
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Filter applications directory...", color = SlateGrey) },
                    leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null, tint = GamingViolet) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkCardBg,
                        unfocusedContainerColor = DarkCardBg,
                        focusedBorderColor = GamingViolet,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            items(searchApps) { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showConfigDialog = app
                        }
                        .padding(vertical = 10.dp, horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E1E24)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Android, contentDescription = null, tint = FlagshipCyan, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(app.appName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(app.packageName, color = SlateGrey, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Icon(Icons.Default.AddCircle, contentDescription = "Configure exception", tint = GamingViolet, modifier = Modifier.size(22.dp))
                }
                HorizontalDivider(color = Color(0x0EFFFFFF))
            }
        }
    }

    // App Profile Configuration Dialog Overlay
    showConfigDialog?.let { dialogApp ->
        AlertDialog(
            onDismissRequest = { showConfigDialog = null },
            confirmButton = {},
            dismissButton = {
                Button(
                    onClick = {
                        val rateInt = inputRate.toIntOrNull() ?: 120
                        viewModel.addAppProfile(dialogApp.packageName, dialogApp.appName, rateInt)
                        showConfigDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GamingViolet, contentColor = Color.White)
                ) {
                    Text("Add Override", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkCardBg,
            title = { Text(dialogApp.appName, color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select forced screen refresh rate limit when ${dialogApp.appName} launches in the foreground:", color = SlateGrey, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(60, 90, 120, 144).forEach { targetHz ->
                            val isSelected = inputRate == "$targetHz"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) GamingViolet else Color(0x0EFFFFFF))
                                    .border(1.dp, if (isSelected) Color.White else DarkCardBorder, RoundedCornerShape(10.dp))
                                    .clickable { inputRate = "$targetHz" }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${targetHz}Hz",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else SlateGrey,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// -------------------------------------------------------------
// SCREEN 6: SYSTEM MONITORING DASHBOARD (Gradients & Canvas Graphs)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoringDashboardScreen(
    navController: NavController,
    viewModel: SpeedViewModel
) {
    val cpuHistory by viewModel.cpuHistory.collectAsState()
    val gpuHistory by viewModel.gpuHistory.collectAsState()
    val fpsHistory by viewModel.fpsHistory.collectAsState()
    val rootStatus by viewModel.rootStatus.collectAsState()
    val shizukuStatus by viewModel.shizukuStatus.collectAsState()
    val battery by viewModel.batteryInfo.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Metric System Monitors", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack, titleContentColor = Color.White)
            )
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Live Frame Sync Graph Box
            item {
                MonitorGraphUI(
                    title = "DISPLAY FRAME DRIFT PIPELINE",
                    subTitle = "Live Realtime Frames Per Second Graph",
                    value = "${viewModel.realtimeFps.collectAsState().value} FPS",
                    history = fpsHistory,
                    maxScale = 150f,
                    neonColor = AccentSuccess
                )
            }

            // CPU Load Monitor
            item {
                MonitorGraphUI(
                    title = "CPU OCCUPATION SPEED",
                    subTitle = "Core Governor Load Index Status",
                    value = "${cpuHistory.lastOrNull() ?: 0}%",
                    history = cpuHistory,
                    maxScale = 100f,
                    neonColor = FlagshipOrange
                )
            }

            // GPU Render Load Monitor
            item {
                MonitorGraphUI(
                    title = "GPU PIPELINE DISPATCH",
                    subTitle = "SurfaceFlinger Composite Rendering Load",
                    value = "${gpuHistory.lastOrNull() ?: 0}%",
                    history = gpuHistory,
                    maxScale = 100f,
                    neonColor = GamingViolet
                )
            }

            // Partition and temperature readings
            item {
                GlassCard {
                    Text("SYSTEM INFRASTRUCTURE INDICES", style = MaterialTheme.typography.labelSmall, color = FlagshipCyan, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    DisplayMatrixRow("Battery Charge Limit", "${battery.percentage}% (${battery.chargingSpeed})")
                    DisplayMatrixRow("Core Liquid Battery Temp", "${battery.temperature}°C")
                    DisplayMatrixRow("Telescopic CPU Heat", "${viewModel.deviceTemp.collectAsState().value}°C")
                    DisplayMatrixRow("Magisk/KernelSU Hook", if (rootStatus.isRooted) "Interlaced Rooted" else "Direct Sandbox Link")
                    DisplayMatrixRow("Secure Settings PM Direct Link", if (shizukuStatus.isRunning) "Running OK" else "Missing Driver")
                }
            }
        }
    }
}

@Composable
fun MonitorGraphUI(
    title: String,
    subTitle: String,
    value: String,
    history: List<Int>,
    maxScale: Float,
    neonColor: Color
) {
    GlassCard(borderGlow = true) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall, color = SlateGrey, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text(subTitle, color = LightSlateGrey, fontSize = 12.sp)
            }
            Text(value, fontWeight = FontWeight.Black, color = neonColor, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        // Custom Curve Canvas Graph Drawing Stream
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF070709))
        ) {
            if (history.isNotEmpty()) {
                val path = Path()
                val fillPath = Path()
                
                val spacingX = size.width / (history.size - 1)
                
                // Set boundary bounds securely
                val scaleFactorY = size.height / maxScale

                history.forEachIndexed { idx, point ->
                    val x = idx * spacingX
                    val y = size.height - (point * scaleFactorY)
                    
                    if (idx == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, size.height)
                        fillPath.lineTo(x, y)
                    } else {
                        path.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                    if (idx == history.size - 1) {
                        fillPath.lineTo(x, size.height)
                        fillPath.close()
                    }
                }

                // Smooth linear graph grid
                for (i in 1..3) {
                    val gridY = (size.height / 4) * i
                    drawLine(
                        color = Color(0x0EFFFFFF),
                        start = Offset(0f, gridY),
                        end = Offset(size.width, gridY),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw neon curve shadows
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(neonColor.copy(alpha = 0.25f), Color.Transparent),
                        startY = 0f,
                        endY = size.height
                    )
                )

                // Render outer core curve borders
                drawPath(
                    path = path,
                    color = neonColor,
                    style = Stroke(width = 2.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// SCREEN 7: ROOT TOOLS
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootToolsScreen(
    navController: NavController,
    viewModel: SpeedViewModel
) {
    val root by viewModel.rootStatus.collectAsState()
    val sfDriversOverridden by viewModel.sfOverridden.collectAsState()
    val thermalsDisabled by viewModel.thermalControllerDisabled.collectAsState()

    ApplyOverlayStatus(viewModel = viewModel)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Root Kernel Tweaker", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack, titleContentColor = Color.White)
            )
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GlassCard(borderGlow = true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFFF1744), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Kernel Control Channel Active", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                            Text("Channel Link Method: ${root.methodName}", color = SlateGrey, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0x0EFFFFFF))
                    Spacer(modifier = Modifier.height(12.dp))

                    StatusHUDGridItem("Magisk active link", if(root.magiskActive) "Integrated" else "Disabled", Icons.Default.Circle, if(root.magiskActive) AccentSuccess else SlateGrey)
                    Spacer(modifier = Modifier.height(6.dp))
                    StatusHUDGridItem("KernelSU active link", if(root.kernelSUActive) "Integrated" else "Disabled", Icons.Default.Circle, if(root.kernelSUActive) AccentSuccess else SlateGrey)
                    Spacer(modifier = Modifier.height(6.dp))
                    StatusHUDGridItem("APatch active link", if(root.aPatchActive) "Integrated" else "Disabled", Icons.Default.Circle, if(root.aPatchActive) AccentSuccess else SlateGrey)
                }
            }

            item {
                CategoryHeading("Low-level Display Driver Tweaks")
            }
            item {
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Forced SurfaceFlinger Compositor", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Instructs SurfaceFlinger system process to ignore frame drop windows completely, holding 120Hz consistently.", color = SlateGrey, fontSize = 11.sp)
                        }
                        Switch(
                            checked = sfDriversOverridden,
                            onCheckedChange = { viewModel.toggleSFDriver() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFFF1744))
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0x13FFFFFF))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Disable Device Thermal Safety Throttle", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Forces hardware to ignore CPU heat down-clamping constraints under intense game sessions. WARNING: Monitors temp.", color = SlateGrey, fontSize = 11.sp)
                        }
                        Switch(
                            checked = thermalsDisabled,
                            onCheckedChange = { viewModel.toggleThermalThrottling() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFFF1744))
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SCREEN 8: SHIZUKU ADAPTER
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShizukuToolsScreen(
    navController: NavController,
    viewModel: SpeedViewModel
) {
    val shizuku by viewModel.shizukuStatus.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shizuku Secure Bridge", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack, titleContentColor = Color.White)
            )
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GlassCard(borderGlow = true) {
                    val stateText = if (shizuku.isRunning) "SHIZUKU ACTIVE SERVICE" else "SHIZUKU DOWN OR AWAITING LINK"
                    val stateColor = if (shizuku.isRunning) AccentSuccess else FlagshipOrange
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(stateColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stateText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = stateColor,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (shizuku.isRunning)
                            "The secure ADB binding represents an active, non-root link. Displays refresh lock configurations applied cleanly."
                            else "Shizuku is not running. Connect wirelessly using on-device Shizuku manager options or write settings permissions via standard ADB.",
                        color = SlateGrey,
                        fontSize = 13.sp
                    )
                }
            }

            item {
                CategoryHeading("Non-Root Diagnostic Guides")
            }
            item {
                GlassCard {
                    Text("1. Install Shizuku on Device", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Text("Download Shizuku manager package from standard APK catalogs or Play Store.", color = SlateGrey, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text("2. Run Wireless Debugging", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Text("Navigate to System Settings > Developer Options and enable Wireless Debugging, then pair using Shizuku's designated port codes.", color = SlateGrey, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("3. Alternatively, Use PC Direct Command", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Text("Hook device on USB interface with ADB active, copy the direct terminal command below, and execute globally to grant system write clearance.", color = SlateGrey, fontSize = 12.sp)
                    
                    val pmCmd = "adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS"
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E1E24))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = pmCmd,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = FlagshipCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(pmCmd))
                            Toast.makeText(context, "Command copied!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x12FFFFFF), contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0x1BFFFFFF))
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = FlagshipCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy CMD Link", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SCREEN 9: CUSTOM LOGS & PROFILES DB BUILDER
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomProfilesScreen(
    navController: NavController,
    viewModel: SpeedViewModel
) {
    val profiles by viewModel.allProfiles.collectAsState()
    val activeProfile by viewModel.activeProfile.collectAsState()
    val logs by viewModel.latestLogs.collectAsState()

    var showCreator by remember { mutableStateOf(false) }
    var profileName by remember { mutableStateOf("") }
    var scaleHz by remember { mutableFloatStateOf(120f) }
    var selectGovernor by remember { mutableStateOf("interactive") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Presets Database", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack, titleContentColor = Color.White),
                actions = {
                    IconButton(onClick = { showCreator = true }) {
                        Icon(imageVector = Icons.Default.AddBox, contentDescription = "Add custom Profile", tint = FlagshipCyan)
                    }
                }
            )
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GlassCard(borderGlow = true) {
                    Text(
                        "DATABASE ENGINE INTEGRATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = FlagshipCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Configure, store, and toggle target profiles. Presets load directly into the dynamic Room partition safely.",
                        fontSize = 13.sp,
                        color = SlateGrey
                    )
                }
            }

            // Custom User profiles list
            item {
                CategoryHeading("Custom Stored Settings Profiles")
            }
            val userComps = profiles.filter { !it.isSystem }
            if (userComps.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkCardBg)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No custom presets compiled yet.", color = SlateGrey, fontSize = 13.sp)
                    }
                }
            } else {
                items(userComps) { profile ->
                    val isSelected = activeProfile?.id == profile.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkCardBg)
                            .border(1.dp, if (isSelected) FlagshipCyan else DarkCardBorder, RoundedCornerShape(16.dp))
                            .clickable { viewModel.applyProfile(profile) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(profile.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Governor: ${profile.cpuGovernor.uppercase()} • Target: ${profile.targetRefreshRate}Hz", color = SlateGrey, fontSize = 12.sp)
                        }
                        IconButton(onClick = { viewModel.deleteCustomProfile(profile) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete custom Profile", tint = AccentFailure)
                        }
                    }
                }
            }

            // Real optimization system logs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryHeading("Real-time Tuning Log Audit")
                    Text(
                        "Clear logs",
                        fontSize = 12.sp,
                        color = AccentFailure,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { viewModel.triggerLogClear() }
                            .padding(6.dp)
                    )
                }
            }
            if (logs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkCardBg)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Audit pipeline logs clear.", color = SlateGrey, fontSize = 13.sp)
                    }
                }
            } else {
                items(logs) { log ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF040406))
                            .padding(10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(log.actionName, fontWeight = FontWeight.Bold, color = if(log.resultStatus == "Success") AccentSuccess else FlagshipCyan, fontSize = 12.sp)
                            Text(
                                text = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp)),
                                fontSize = 10.sp,
                                color = SlateGrey
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(log.description, color = SlateGrey, fontSize = 11.sp)
                    }
                }
            }
        }
    }

    // Custom profile creator overlay dialog
    if (showCreator) {
        AlertDialog(
            onDismissRequest = { showCreator = false },
            confirmButton = {},
            dismissButton = {
                Button(
                    onClick = {
                        if (profileName.isNotBlank()) {
                            viewModel.addNewCustomProfile(profileName, scaleHz.roundToInt(), selectGovernor)
                            profileName = ""
                            showCreator = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlagshipCyan, contentColor = Color.Black)
                ) {
                    Text("Construct Profile", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkCardBg,
            title = { Text("Compile Custom Profile", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = profileName,
                        onValueChange = { profileName = it },
                        label = { Text("Profile Name (e.g., Performance 96Hz)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E1E24),
                            unfocusedContainerColor = Color(0xFF1E1E24),
                            focusedBorderColor = FlagshipCyan,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column {
                        Text("Target Frequency: ${scaleHz.roundToInt()}Hz", color = Color.White, fontSize = 13.sp)
                        Slider(
                            value = scaleHz,
                            onValueChange = { scaleHz = it },
                            valueRange = 40f..165f,
                            colors = SliderDefaults.colors(
                                thumbColor = FlagshipCyan,
                                activeTrackColor = FlagshipCyan,
                                inactiveTrackColor = Color(0x0EFFFFFF)
                            )
                        )
                    }

                    Column {
                        Text("CPU Scaling Governor Mode:", color = Color.White, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("performance", "interactive", "powersave").forEach { gov ->
                                val isSel = selectGovernor == gov
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) FlagshipCyan else Color(0x0EFFFFFF))
                                        .border(1.dp, if (isSel) Color.White else DarkCardBorder, RoundedCornerShape(8.dp))
                                        .clickable { selectGovernor = gov }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        gov,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.Black else SlateGrey
                                    )
                                }
                            }
                        }
                    }
                }
            },
            shape = RoundedCornerShape(22.dp)
        )
    }
}

// -------------------------------------------------------------
// SCREEN 10: ABOUT PHONE (Samsung Specs Layout)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPhoneScreen(
    navController: NavController,
    viewModel: SpeedViewModel
) {
    val battery by viewModel.batteryInfo.collectAsState()
    val ram by viewModel.ramInfo.collectAsState()
    val storage by viewModel.storageInfo.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Phone", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack, titleContentColor = Color.White)
            )
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Samsung Styled Spec Dial Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(DarkCardBg)
                        .border(1.dp, ColorsDarkGradientBorder, RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(FlagshipCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "S",
                                fontWeight = FontWeight.Black,
                                fontSize = 32.sp,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = Build.MODEL,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Codename: ${Build.PRODUCT.uppercase()}",
                            fontSize = 12.sp,
                            color = SlateGrey
                        )
                    }
                }
            }

            // Specs table matrix
            item {
                CategoryHeading("Device Specification Sheet")
            }
            item {
                GlassCard {
                    DisplayMatrixRow("Manufacturer", Build.MANUFACTURER)
                    DisplayMatrixRow("Hardware Code", Build.HARDWARE)
                    DisplayMatrixRow("Base Board", Build.BOARD)
                    DisplayMatrixRow("Android Version", "${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
                    DisplayMatrixRow("Security Patch Patch", Build.VERSION.SECURITY_PATCH)
                    DisplayMatrixRow("Total Storage Allocation", storage.total)
                    DisplayMatrixRow("Total RAM Allocation", ram.total)
                    DisplayMatrixRow("Battery Engineering Health", battery.health)
                    DisplayMatrixRow("System Partition Build", Build.FINGERPRINT.take(35) + "...")
                }
            }

            // Export parameters action button
            item {
                Button(
                    onClick = {
                        try {
                            val outputString = """
                                SPEED HARDWARE DIAGNOSTIC REPORT
                                Crafted via System Utility Console By Mehraann
                                ==========================================
                                MODEL: ${Build.MODEL}
                                BRAND: ${Build.MANUFACTURER}
                                ANDROID RELEASE: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
                                SECURITY LEVEL: ${Build.VERSION.SECURITY_PATCH}
                                MEMORY: ${ram.total} (Free: ${ram.free})
                                STORAGE: ${storage.total} (Free: ${storage.free})
                                BATTERY STATUS: ${battery.percentage}% (Temp: ${battery.temperature}°C)
                                ==========================================
                                End of System Audit Report.
                            """.trimIndent()
                            
                            val path = context.getExternalFilesDir(null)
                            val destFile = File(path, "speed_device_report.txt")
                            destFile.writeText(outputString)
                            Toast.makeText(context, "Hardware spec sheet report exported completely to: ${destFile.name}", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Report export failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlagshipCyan, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Specs Report Document", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

val ColorsDarkGradientBorder = Brush.linearGradient(
    colors = listOf(DarkCardBorder, DarkCardBorderGlow, DarkCardBorderGlow, DarkCardBorder)
)
