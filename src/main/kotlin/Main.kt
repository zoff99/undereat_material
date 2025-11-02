@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@file:Suppress("LocalVariableName", "FunctionName", "ConvertToStringTemplate", "SpellCheckingInspection", "UnusedReceiverParameter", "LiftReturnOrAssignment", "CascadeIf", "SENSELESS_COMPARISON", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNUSED_ANONYMOUS_PARAMETER", "REDUNDANT_ELSE_IN_WHEN", "ReplaceSizeCheckWithIsNotEmpty", "ReplaceRangeToWithRangeUntil", "ReplaceGetOrSet", "SimplifyBooleanWithConstants")

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.Typography
import androidx.compose.material.lightColors
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowExceptionHandler
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ca.gosyer.appdirs.AppDirs
import com.zoffcc.applications.undereat.Log
import com.zoffcc.applications.undereat.MainActivity.Companion.DEBUG_COMPOSE_UI_UPDATES
import com.zoffcc.applications.undereat.MainActivity.Companion.PREF__database_files_dir
import com.zoffcc.applications.undereat.MainScreen
import com.zoffcc.applications.undereat.PrefsSettings
import com.zoffcc.applications.undereat.corefuncs
import com.zoffcc.applications.undereat.createGlobalStore
import com.zoffcc.applications.undereat.restore_mainlist_state
import com.zoffcc.applications.undereat_material.undereat_material.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n
import java.awt.Toolkit
import java.io.File
import java.util.*
import java.util.prefs.Preferences

private const val TAG = "undereat.Main.kt"
var closing_application = false
val global_prefs: Preferences = Preferences.userNodeForPackage(com.zoffcc.applications.undereat.PrefsSettings::class.java)
val UISCALE_ITEM_HEIGHT = 30.dp
val SETTINGS_HEADER_SIZE = 56.dp
const val SNACKBAR_TOAST_MS_DURATION: Long = 1200
val APPDIRS = AppDirs("undereat_material", "zoxcore")
val RESOURCESDIR = File(System.getProperty("compose.application.resources.dir"))
var scaffoldState: ScaffoldState = ScaffoldState(drawerState = DrawerState(initialValue = DrawerValue.Closed), snackbarHostState = SnackbarHostState())
@OptIn(DelicateCoroutinesApi::class)
var ScaffoldCoroutineScope: CoroutineScope = GlobalScope
var NotoEmojiFont: FontFamily? = null
var DefaultFont: FontFamily? = null

val globalstore = CoroutineScope(SupervisorJob()).createGlobalStore()

@OptIn(DelicateCoroutinesApi::class, ExperimentalFoundationApi::class)
@Composable
@Preview
fun App()
{
    println("User data dir: " + APPDIRS.getUserDataDir())
    println("User data dir (roaming): " + APPDIRS.getUserDataDir(roaming = true))
    PREF__database_files_dir = APPDIRS.getUserDataDir(roaming = true)
    try
    {
        val dir_file = File(PREF__database_files_dir)
        dir_file.mkdirs()
    }
    catch(e: Exception)
    {
        Log.i(TAG, "error creating savefile directory and parents: " + PREF__database_files_dir)
    }
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
    // Log.i(TAG, "resources dir canonical: " + RESOURCESDIR.canonicalPath + File.separator)

    Log.i(TAG, "CCCC:" + PrefsSettings::class.java)

    var ui_scale by remember { mutableStateOf(1.0f) }
    scaffoldState = rememberScaffoldState()
    ScaffoldCoroutineScope = rememberCoroutineScope()
    Theme {
        Scaffold(modifier = Modifier.randomDebugBorder(), scaffoldState = scaffoldState) {
            MainScreen()
        }
    }
    corefuncs().init_me()
    restore_mainlist_state()
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

    // ------- set UI look and feel to "system" for java AWT ----------
    // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    // ------- set UI look and feel to "system" for java AWT ----------

    MainAppStart()
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MainAppStart()
{
    globalstore.setDefaultDensity(LocalDensity.current.density)

    globalstore.loadUiDensity()
    val appIcon = painterResource("icon-linux.png")

    // HINT: !! until we have settings, set ui density here manually !!
    globalstore.updateUiDensity(1.73f)

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
                    title = i18n("ui.close_undereat"),
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

fun get_trifa_build_str(): String
{
    var build_str = ""

    try
    {
        build_str = build_str + BuildConfig.GIT_COMMIT_HASH.take(4)
    } catch (e: java.lang.Exception)
    {
        build_str = build_str + "??????????".take(4)
    }

    try
    {
        build_str = build_str + "-" + System.getProperty("os.arch")
    } catch (e: java.lang.Exception)
    {
        build_str = build_str + "??????????".take(3)
    }

    return build_str
}

@Composable
fun Theme(content: @Composable () -> Unit)
{
    var Typography: androidx.compose.material.Typography? = null
    try
    {
        Typography = Typography(
            defaultFontFamily = DefaultFont!!
        )
    }
    catch(_: Exception)
    {
        Typography = MaterialTheme.typography
    }

    // colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
    // TextFieldDefaults.textFieldColors(backgroundColor = Color(ChatColorsConfig.LIGHT__TEXTFIELD_BGCOLOR))
    MaterialTheme(
        typography = Typography!!,
        colors = lightColors(
            surface = Color(ChatColorsConfig.LIGHT__FGCOLOR),
            background = Color(ChatColorsConfig.LIGHT__BGCOLOR),
        ),
    ) {
        ProvideTextStyle(LocalTextStyle.current.copy(letterSpacing = 0.sp)) {
            content()
        }
    }
}

@Composable
fun UIScaleItem(
    label: String,
    description: String,
    setting: @Composable (RowScope.() -> Unit),
) = Row(Modifier.randomDebugBorder().fillMaxWidth().height(UISCALE_ITEM_HEIGHT)
    .padding(horizontal = 16.dp).
    semantics(mergeDescendants = true) { // it would be nicer to derive the contentDescriptions from the descendants automatically
        // which is currently not supported in Compose for Desktop
        // see https://github.com/JetBrains/compose-jb/issues/2111
        contentDescription = description
    }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
    Text(label)
    setting()
}
