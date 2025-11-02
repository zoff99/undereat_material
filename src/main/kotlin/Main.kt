@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@file:Suppress("LocalVariableName", "FunctionName", "ConvertToStringTemplate", "SpellCheckingInspection", "UnusedReceiverParameter", "LiftReturnOrAssignment", "CascadeIf", "SENSELESS_COMPARISON", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNUSED_ANONYMOUS_PARAMETER", "REDUNDANT_ELSE_IN_WHEN", "ReplaceSizeCheckWithIsNotEmpty", "ReplaceRangeToWithRangeUntil", "ReplaceGetOrSet", "SimplifyBooleanWithConstants")

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowExceptionHandler
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ca.gosyer.appdirs.AppDirs
import com.zoffcc.applications.undereat.Log
import com.zoffcc.applications.undereat.PrefsSettings
import com.zoffcc.applications.undereat.TAG
import com.zoffcc.applications.undereat_material.undereat_material.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n
import java.awt.Toolkit
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import java.util.prefs.Preferences

private const val TAG = "undereat.Main.kt"
var tox_running_state_wrapper = "start"
var start_button_text_wrapper = "stopped"
var online_button_text_wrapper = "offline"
var online_button_color_wrapper = Color.White.toArgb()
var closing_application = false
val global_prefs: Preferences = Preferences.userNodeForPackage(com.zoffcc.applications.undereat.PrefsSettings::class.java)
val UISCALE_ITEM_HEIGHT = 30.dp
val CONTACTITEM_HEIGHT = 50.dp
val GROUPITEM_HEIGHT = 50.dp
val GROUP_PEER_HEIGHT = 33.dp
val SETTINGS_HEADER_SIZE = 56.dp
val CONTACT_COLUMN_WIDTH = 230.dp
const val CONTACT_COLUMN_CONTACTNAME_LEN_THRESHOLD = 13
const val PUSHURL_SHOW_LEN_THRESHOLD = 60
val GROUPS_COLUMN_WIDTH = 190.dp
val GROUPS_COLLAPSED_COLUMN_WIDTH = 50.dp
const val GROUPS_COLUMN_GROUPNAME_LEN_THRESHOLD = 13
val GROUP_PEER_COLUMN_WIDTH = 165.dp
val GROUP_COLLAPSED_PEER_COLUMN_WIDTH = 45.dp
const val GROUP_PEER_COLUMN_PEERNAME_LEN_THRESHOLD = 12
val MESSAGE_INPUT_LINE_HEIGHT = 58.dp
val MAIN_TOP_TAB_HEIGHT = 160.dp
const val IMAGE_PREVIEW_SIZE = 70f
const val AVATAR_SIZE = 40f
const val MAX_AVATAR_SIZE = 70f
val SPACE_AFTER_LAST_MESSAGE = 2.dp
val SPACE_BEFORE_FIRST_MESSAGE = 10.dp
const val LAST_MSG_SCROLL_TO_SCROLL_OFFSET = 10000
const val VIDEO_PLACEHOLDER_ALPHA = 0.2f
val AV_SELECTOR_ICON_SIZE = 10.dp
val VIDEO_IN_BOX_WIDTH_SMALL = 80.dp
val VIDEO_IN_BOX_HEIGHT_SMALL = 80.dp
const val VIDEO_IN_BOX_WIDTH_FRACTION_SMALL = 0.3f
const val VIDEO_IN_BOX_WIDTH_FRACTION_BIG = 0.9f
val VIDEO_STATS_TEXT_HEIGHT = 20.dp
val VIDEO_IN_BOX_WIDTH_BIG = 800.dp
val VIDEO_IN_BOX_HEIGHT_BIG = 3000.dp
val VIDEO_OUT_BOX_WIDTH_SMALL = 130.dp
val VIDEO_OUT_BOX_HEIGHT_SMALL = 100.dp
val VIDEO_OUT_BOX_WIDTH_BIG = 500.dp
val VIDEO_OUT_BOX_HEIGHT_BIG = 500.dp
const val DOUBLE_BUFFER_VIDEOIN = true
const val DOUBLE_BUFFER_VIDEOOUT = true
val SAVEDATA_PATH_WIDTH = 200.dp
val SAVEDATA_PATH_HEIGHT = 50.dp
val MYTOXID_WIDTH = 200.dp
val MYTOXID_HEIGHT = 50.dp
val MAIN_STATUS_BAR_HEIGHT = 18.dp
val MESSAGE_BOX_BOTTOM_PADDING = 4.dp
const val MSG_TEXT_FONT_SIZE_MIXED = 14.0f
const val MSG_TEXT_FONT_SIZE_EMOJI_ONLY = 55.0f
const val MAX_EMOJI_POP_SEARCH_LEN = 20
const val MAX_EMOJI_POP_RESULT = 15
const val MAX_ONE_ON_ONE_MESSAGES_TO_SHOW = 20000
const val MAX_GROUP_MESSAGES_TO_SHOW = 20000
const val SNACKBAR_TOAST_MS_DURATION: Long = 1200
const val BG_COLOR_RELAY_CONTACT_ITEM = 0x448ABEB9
const val BG_COLOR_OWN_RELAY_CONTACT_ITEM = 0x44FFFFB9
const val URL_TEXTVIEW_URL_COLOR = 0xFF223DDC
const val NGC_PRIVATE_MSG_INDICATOR_COLOR = 0xFFFFA255
val VIDEO_BOX_BG_COLOR = Color(0x00E7E7E7) // this is now fully transparent. but just in case the color vaule of the grey BG is saved here
val MESSAGE_PUSH_CHECKMARK_COLOR = Color(0xFF2684A7)
val DELIVERY_CHECKMARK_COLOR = Color(0xFF2684A7)
val DELIVERY_CONFIRM_CHECKMARK_COLOR = Color(0xFF2684A7)
val MESSAGE_CHECKMARKS_ICON_SIZE = 12.dp
val MESSAGE_CHECKMARKS_CONTAINER_SIZE = 12.dp
const val NGC_PEER_LUMINANCE_THRESHOLD_FOR_SHADOW = 0.733 // 0.85f // 0.935f // 0.733f // 0.935f
const val NGC_PEER_SHADOW_COLOR = 0xFF444444
val ImageloaderDispatcher = Executors.newFixedThreadPool(5).asCoroutineDispatcher()
val APPDIRS = AppDirs("trifa_undereat", "zoxcore")
val RESOURCESDIR = File(System.getProperty("compose.application.resources.dir"))
const val GENERIC_TOR_USERAGENT = "Mozilla/5.0 (Windows NT 6.1; rv:60.0) Gecko/20100101 Firefox/60.0"
var scaffoldState: ScaffoldState = ScaffoldState(drawerState = DrawerState(initialValue = DrawerValue.Closed), snackbarHostState = SnackbarHostState())
@OptIn(DelicateCoroutinesApi::class)
var ScaffoldCoroutineScope: CoroutineScope = GlobalScope
var NotoEmojiFont: FontFamily? = null
var DefaultFont: FontFamily? = null

@OptIn(DelicateCoroutinesApi::class, ExperimentalFoundationApi::class)
@Composable
@Preview
fun App()
{
    println("User data dir: " + APPDIRS.getUserDataDir())
    println("User data dir (roaming): " + APPDIRS.getUserDataDir(roaming = true))
    savepathstore.updatePath(APPDIRS.getUserDataDir(roaming = true))
    println("User config dir: " + APPDIRS.getUserConfigDir())
    println("User config dir (roaming): " + APPDIRS.getUserConfigDir(roaming = true))
    println("User cache dir: " + APPDIRS.getUserCacheDir())
    println("User log dir: " + APPDIRS.getUserLogDir())
    println("Site data dir: " + APPDIRS.getSiteDataDir())
    println("Site data dir (multi path): " + APPDIRS.getSiteDataDir(multiPath = true))
    println("Site config dir: " + APPDIRS.getSiteConfigDir())
    println("Site config dir (multi path): " + APPDIRS.getSiteConfigDir(multiPath = true))
    println("Shared dir: " + APPDIRS.getSharedDir())
    try
    {
        println("Prefs dir (estimation for linux): " + "~/.java/.userPrefs/" + global_prefs.absolutePath())
    }
    catch(_: Exception)
    {
    }

    Log.i(TAG, "resources dir: " + RESOURCESDIR)
    Log.i(TAG, "resources dir canonical: " + RESOURCESDIR.canonicalPath + File.separator)

    Log.i(TAG, "CCCC:" + PrefsSettings::class.java)

    var ui_scale by remember { mutableStateOf(1.0f) }
    scaffoldState = rememberScaffoldState()
    ScaffoldCoroutineScope = rememberCoroutineScope()
    Theme {
        Scaffold(modifier = Modifier.randomDebugBorder(), scaffoldState = scaffoldState) {
            Column() {
            }

        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun SnackBarToast(message: String, duration_ms: Long = SNACKBAR_TOAST_MS_DURATION)
{
    GlobalScope.launch {
        val job = ScaffoldCoroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
        delay(duration_ms)
        job.cancel()
    }
}

fun main() = application(exitProcessOnExit = true) {
    try
    { // HINT: show proper name in MacOS Menubar
        // https://alvinalexander.com/java/java-application-name-mac-menu-bar-menubar-class-name/
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Undereat - Material")
        System.setProperty("apple.awt.application.name", "Undereat - Material")
        System.setProperty("apple.laf.useScreenMenuBar", "true")
    } catch (e: java.lang.Exception)
    {
        e.printStackTrace()
    }

    try
    { // set "StartupWMClass" for Java Swing applications
        //
        // https://stackoverflow.com/a/29218320
        //
        val xToolkit: Toolkit = Toolkit.getDefaultToolkit()
        var awtAppClassNameField: java.lang.reflect.Field? = null
        awtAppClassNameField = xToolkit.javaClass.getDeclaredField("awtAppClassName")
        awtAppClassNameField.isAccessible = true
        awtAppClassNameField[xToolkit] = "normal_undereat_material" // this needs to be exactly the same String as "StartupWMClass" in the "*.desktop" file
    } catch (e: Exception)
    { // e.printStackTrace()
    }

    try {
        set_resouces_dir(RESOURCESDIR.canonicalPath)
    } catch(_: Exception) {}

    // ------- set UI look and feel to "system" for java AWT ----------
    // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    // ------- set UI look and feel to "system" for java AWT ----------

    init_system_tray(RESOURCESDIR.canonicalPath + File.separator + "icon-linux.png")

    MainAppStart()
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MainAppStart()
{
    globalstore.setDefaultDensity(LocalDensity.current.density)

    globalstore.loadUiDensity()
    val appIcon = painterResource("icon-linux.png")

    // ----------- main app screen -----------
    // ----------- main app screen -----------
    // ----------- main app screen -----------
    var isOpen by remember { mutableStateOf(true) }
    var isAskingToClose by remember { mutableStateOf(false) }
    var state = rememberWindowState()
    var x_ = Dp(0.0f)
    var y_ = Dp(0.0f)
    var w_ = Dp(0.0f)
    var h_ = Dp(0.0f)
    var error = 0
    try
    {
        x_ = global_prefs.get("main.window.position.x", "").toFloat().dp
        y_ = global_prefs.get("main.window.position.y", "").toFloat().dp
        w_ = global_prefs.get("main.window.size.width", "").toFloat().dp
        h_ = global_prefs.get("main.window.size.height", "").toFloat().dp
        println("init:onWindowReload " + x_ + " " + y_ + " " + w_ + " " + h_)
    } catch (_: Exception)
    {
        error = 1
    }

    if (error == 0)
    {
        val wpos = WindowPosition(x = x_, y = y_)
        val wsize = DpSize(w_, h_)
        state = rememberWindowState(position = wpos, size = wsize)
    }

    if (isOpen)
    {
        var win_title_addon = "Unknown Version"
        try
        {
            win_title_addon = BuildConfig.APP_VERSION + " (Build: " + get_trifa_build_str() + ")"
        } catch (_: java.lang.Exception)
        {
        }
        Window(onCloseRequest = { isAskingToClose = true },
            title = "Undereat - " + win_title_addon,
            icon = appIcon, state = state,
            focusable = true,
            onKeyEvent = {
                if (!it.isMetaPressed && !it.isAltPressed && !it.isCtrlPressed && !it.isShiftPressed && it.key == Key.F11 && it.type == KeyEventType.KeyDown)
                {
                    if (state.placement == WindowPlacement.Fullscreen)
                    {
                        state.placement = WindowPlacement.Floating
                    }
                    else
                    {
                        state.placement = WindowPlacement.Fullscreen
                    }
                    true
                }
                else if (!it.isMetaPressed && !it.isAltPressed && !it.isCtrlPressed && !it.isShiftPressed && it.key == Key.Escape && it.type == KeyEventType.KeyDown)
                {
                    if (state.placement == WindowPlacement.Fullscreen)
                    {
                        state.placement = WindowPlacement.Floating
                        true
                    }
                    else
                    {
                        false
                    }
                } else
                {
                    false
                }
            }
        ) {
            @OptIn(ExperimentalComposeUiApi::class)
            window.exceptionHandler = WindowExceptionHandler { e -> println("Exception in Compose: $e") }
            if (isAskingToClose)
            {
                Dialog(
                    onCloseRequest = { isAskingToClose = false },
                    title = i18n("ui.close_trifa"),
                ) {
                    Button(onClick = {
                        Log.i(TAG, "closing application")
                        isOpen = false
                        closing_application = true
                    }) {
                        Text(i18n("ui.yes"))
                    }
                }
            }
            val windowInfo = LocalWindowInfo.current
            LaunchedEffect(windowInfo) {
                snapshotFlow { windowInfo.isWindowFocused }.collect { onWindowFocused ->
                    onWindowFocused(onWindowFocused)
                }
            }
            LaunchedEffect(state) {
                snapshotFlow { state.isMinimized }.onEach(::onWindowMinimised).launchIn(this)
                snapshotFlow { state.size }.onEach(::onWindowResize).launchIn(this)
                snapshotFlow { state.position }.filter { it.isSpecified }.onEach(::onWindowRelocate).launchIn(this)
            }
            // var ui_density by remember { mutableStateOf(globalstore.getUiDensity()) }
            // val manual_recompose = remember { mutableStateOf(globalstore.state.ui_density) }
            CompositionLocalProvider(
                LocalDensity provides Density(globalstore.state.ui_density)
            )
            {
                App()
            }
        }
    }
    // ----------- main app screen -----------
    // ----------- main app screen -----------
    // ----------- main app screen -----------
}

private fun onWindowFocused(focused: Boolean)
{
    // println("onWindowFocused $focused")
    globalstore.updateFocused(focused)
}

private fun onWindowMinimised(minimised: Boolean)
{
    // println("onWindowMinimised $minimised")
    globalstore.updateMinimized(minimised)
}

private fun onWindowResize(size: DpSize)
{
    // println("size: onWindowResize $size " + size.width.value.toString() + " " + size.height.value.toString())
    global_prefs.put("main.window.size.width", size.width.value.toString())
    global_prefs.put("main.window.size.height", size.height.value.toString())
}

private fun onWindowRelocate(position: WindowPosition)
{
    // println("pos : onWindowRelocate $position " + position.x.value.toString() + " " + position.y.value.toString())
    global_prefs.put("main.window.position.x", position.x.value.toString())
    global_prefs.put("main.window.position.y", position.y.value.toString())
}

fun Modifier.randomDebugBorder(): Modifier =
    if (DEBUG_COMPOSE_UI_UPDATES)
    {
        Modifier.padding(3.dp).border(width = 4.dp,
            color = Color(
                Random().nextInt(0, 255),
                Random().nextInt(0, 255),
                Random().nextInt(0, 255)),
            shape = RectangleShape)
    }
    else
    {
        Modifier
    }

fun Modifier.dashedBorder(strokeWidth: Dp, color: Color, cornerRadiusDp: Dp) = composed(
    factory = {
        val density = LocalDensity.current
        val strokeWidthPx = density.run { strokeWidth.toPx() }
        val cornerRadiusPx = density.run { cornerRadiusDp.toPx() }

        then(
            Modifier.drawWithCache {
                onDrawBehind {
                    val stroke = Stroke(
                        width = strokeWidthPx,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                    drawRoundRect(
                        color = color,
                        style = stroke,
                        cornerRadius = CornerRadius(cornerRadiusPx)
                    )
                }
            }
        )
    }
)

