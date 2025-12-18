@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@file:Suppress("LocalVariableName", "FunctionName", "ConvertToStringTemplate", "SpellCheckingInspection", "UnusedReceiverParameter", "LiftReturnOrAssignment", "CascadeIf", "SENSELESS_COMPARISON", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNUSED_ANONYMOUS_PARAMETER", "REDUNDANT_ELSE_IN_WHEN", "ReplaceSizeCheckWithIsNotEmpty", "ReplaceRangeToWithRangeUntil", "ReplaceGetOrSet", "SimplifyBooleanWithConstants")

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.dragData
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
import com.kdroid.composetray.utils.SingleInstanceManager
import com.zoffcc.applications.undereat.Log
import com.zoffcc.applications.undereat.MainActivity.Companion.DEBUG_COMPOSE_UI_UPDATES
import com.zoffcc.applications.undereat.MainActivity.Companion.PREF__database_files_dir
import com.zoffcc.applications.undereat.MainScreen
import com.zoffcc.applications.undereat.PrefsSettings
import com.zoffcc.applications.undereat.corefuncs
import com.zoffcc.applications.undereat.createGlobalStore
import com.zoffcc.applications.undereat.import_db_from_file
import com.zoffcc.applications.undereat.import_file_extension
import com.zoffcc.applications.undereat.restore_mainlist_state
import com.zoffcc.applications.undereat.show
import com.zoffcc.applications.undereat_material.undereat_material.BuildConfig
import com.zoffcc.applications.undereat_material.undereat_material.generated.resources.Res
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
import java.net.URI
import java.nio.file.LinkOption
import java.util.*
import java.util.prefs.Preferences
import kotlin.io.path.exists
import kotlin.io.path.toPath

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
var startup_import_filename: String? = null

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
        var import_file_name_main: String by remember { mutableStateOf("") }
        var show_import_alert by remember { mutableStateOf(false) }
        if (show_import_alert)
        {
            AlertDialog(onDismissRequest = { },
                title = { Text("Import data from file:" + "\n" + import_file_name_main) },
                confirmButton = {
                    Button(onClick = {
                        val import_file_name2 = import_file_name_main
                        import_file_name_main = ""
                        import_db_from_file(import_file_name2)
                        restore_mainlist_state()
                        show_import_alert = false
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    Button(onClick = { show_import_alert = false }) {
                        Text("No")
                    }
                },
                text = { "Really import data ?" })
        }

        Scaffold(modifier = Modifier.randomDebugBorder(), scaffoldState = scaffoldState) {

            var isDragging by remember { mutableStateOf(false) }
            val dragAndDropTarget = remember() {
                object: DragAndDropTarget
                {
                    override fun onExited(event: DragAndDropEvent)
                    {
                        // println("======> onExited:" + event)
                        isDragging = false
                    }

                    override fun onEntered(event: DragAndDropEvent)
                    {
                        isDragging = true
                        // println("======> onEntered:" + event)
                    }

                    override fun onChanged(event: DragAndDropEvent)
                    {
                        // println("======> onChanged:" + event)
                    }

                    override fun onStarted(event: DragAndDropEvent) {
                        // println("======> onStarted:" + event + " " + event.dragData())
                    }
                    override fun onEnded(event: DragAndDropEvent) {
                        isDragging = false
                        // println("======> onEnded:" + event)
                    }
                    override fun onDrop(event: DragAndDropEvent): Boolean {
                        isDragging = false
                        // println("======> onDrop:" + event + " " + event.dragData())
                        if (event.dragData() is DragData.FilesList)
                        {
                            // println("======> onDrop:" + event)
                            val newFiles = (event.dragData() as DragData.FilesList).readFiles().mapNotNull { it1: String ->
                                URI(it1).toPath().takeIf { it.exists(LinkOption.NOFOLLOW_LINKS) }
                            }
                            newFiles.forEach {
                                if (it.toAbsolutePath().toString().isNotEmpty()) {
                                    Log.i(TAG," " + it.toAbsolutePath().parent.toString() + " "
                                            + it.toAbsolutePath().fileName.toString())
                                    import_file_name_main = it.toAbsolutePath().toString()
                                    show_import_alert = true
                                }
                            }
                        }
                        else if (event.dragData() is DragData.Image)
                        {
                            // println("======> onDrop:iiiii " + event + " " + (event.dragData() as DragData.Image).toString())
                        }
                        else if (event.dragData() is DragData.Text)
                        {
                            // println("======> onDrop:ttttt " + event + " " + (event.dragData() as DragData.Text).readText())
                        }
                        else
                        {
                            // println("======> onDrop:uuuuu " + event + " " + event.dragData())
                        }
                        isDragging = false
                        return true
                    }
                }
            }

            Box(Modifier.fillMaxSize()
                .background(color = if (isDragging) Color.LightGray else Color.Transparent)
                .dragAndDropTarget(
                    shouldStartDragAndDrop = { true },
                    target = dragAndDropTarget
                )) {
                if (isDragging)
                {
                    val scope = rememberCoroutineScope()
                    Column(modifier = Modifier.fillMaxSize()
                        .padding(all = 10.dp)
                        .dashedBorder(color = if (isDragging) DragAndDropColors.active else Color.Transparent,
                            strokeWidth = if (isDragging) 5.dp else 0.dp,
                            cornerRadiusDp = if (isDragging) 25.dp else 0.dp)) {
                        Spacer(modifier = Modifier.weight(0.6f))
                        DragAndDropDescription(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = DragAndDropColors.active
                        )
                        Spacer(modifier = Modifier.weight(0.6f))
                    }
                }
                else
                {
                    MainScreen()
                    if ((startup_import_filename != null) && (startup_import_filename!!.length > import_file_extension.length))
                    {
                        // HINT: start import here
                        import_file_name_main = startup_import_filename!!
                        startup_import_filename = null
                        show_import_alert = true
                    }
                }
            }
        }
    }
    corefuncs().init_me()
    restore_mainlist_state()
}

@Composable
fun DragAndDropDescription(modifier: Modifier, color: Color) {
    val modifier2 = modifier.padding(vertical = 2.dp)
    Text(
        "Drag & drop files here",
        fontSize = 20.sp,
        modifier = modifier2,
        color = color
    )
}

object DragAndDropColors {
    val default = Color.Gray
    val active = Color(29, 117, 223, 255)
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

fun main(args: Array<String>) = application(exitProcessOnExit = true) {

    try
    {
        println("args START ============")
        println("args all:" + args.size)
        args.iterator().forEach {
            println("args:" + it)
            if (it.endsWith(import_file_extension)) {
                // HINT: application was opened with an import file as arguement, so lets import it
                startup_import_filename = it
            }
        }
        println("args DONE  ============")
    }
    catch(e: Exception)
    {
        e.printStackTrace()
    }

    // -- check for single instance --
    // thanks to: https://github.com/kdroidFilter/ComposeNativeTray/blob/master/src/commonMain/kotlin/com/kdroid/composetray/utils/SingleInstanceManager.kt
    //
    val isSingleInstance = SingleInstanceManager.isSingleInstance(
        onRestoreRequest = {
            // indicate that our main windows needs to be shown (if minimized now)
            globalstore.updateMinimized(false)
        }
    )
    if (!isSingleInstance) {
        exitApplication()
        return@application
    }
    // -- check for single instance --

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
        awtAppClassNameField[xToolkit] = "UndereatMainKt" // this needs to be exactly the same String as "StartupWMClass" in the "*.desktop" file
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
    // ** // globalstore.updateUiDensity(1.73f)

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
                val globalstore__ by globalstore.stateFlow.collectAsState()
                if (!globalstore__.mainwindow_minimized)
                {
                    // un-minimize main window when someone tried to open another instance of this app
                    state.isMinimized = false
                    globalstore.updateMinimized(false)
                }
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

enum class SORTER(val value: Long) {
    NAME(0),
    ADDRESS(1),
    DISTANCE(2),
    RATING(3),
    ADDED_DATE(4),
    MODIFIED_DATE(5)
}
