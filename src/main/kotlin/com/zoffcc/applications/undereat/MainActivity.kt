@file:Suppress("UNUSED_PARAMETER", "LocalVariableName", "PropertyName", "ClassName", "FunctionName", "unused", "UNUSED_VARIABLE", "SpellCheckingInspection", "UnnecessaryVariable", "ConvertToStringTemplate", "UNUSED_VALUE", "ReplaceCallWithBinaryOperator", "CascadeIf", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "ControlFlowWithEmptyBody", "MemberVisibilityCanBePrivate", "ConstPropertyName", "ConstPropertyName", "ObjectPropertyName", "ReplaceJavaStaticMethodWithKotlinAnalog", "KotlinConstantConditions", "FoldInitializerAndIfToElvis", "SENSELESS_COMPARISON")
package com.zoffcc.applications.undereat

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.zoffcc.applications.undereat.HelperOSFile.open_webpage
import com.zoffcc.applications.undereat.corefuncs.del_g_opts
import com.zoffcc.applications.undereat.corefuncs.get_g_opts
import com.zoffcc.applications.undereat.corefuncs.orma
import com.zoffcc.applications.undereat.corefuncs.set_g_opts
import com.zoffcc.applications.undereat_material.undereat_material.BuildConfig
import globalstore
import java.util.Locale

const val DEBUG_COMPOSE_UI_UPDATES = false // set "false" for release builds
const val UNDEREAT_USERAGENT = "UnderEatApp https://github.com/zoff99/UnderEat"
const val HTTP_MAPS_URL = "https://www.google.com/maps/search/?api=1&query="
const val HTTP_NOMINATIM_SEARCH_URL = "https://nominatim.openstreetmap.org/search?limit=1&addressdetails=1&format=json&q="
var TAXI_PHONE_NUMBER: String? = null
var global_categories: MutableMap<Long, String> = mutableMapOf()
const val MAX_DISTANCE = 30_000 // max distance in meters when location will not be used anymore on mainlist
val RESERVATION_LINE = Color(0xff842111)

var gps = null

@Suppress("UNUSED_PARAMETER", "LocalVariableName", "PropertyName", "ClassName", "FunctionName", "SpellCheckingInspection")
class MainActivity
{
    companion object
    {
        private const val TAG = "undereat.MainActivity"

        // --------- global config ---------
        // --------- global config ---------
        const val DEBUG_COMPOSE_UI_UPDATES = false // set "false" for release builds

        // --------- global config ---------
        // --------- global config ---------
        @JvmStatic var PREF__DB_wal_mode = true

        @JvmField
        var PREF__database_files_dir = "."

        fun main_init()
        {
            try
            {
                println("Version:" + BuildConfig.APP_VERSION)
            } catch (_: Exception)
            {
            }

            try
            {
                println("java.vm.name:" + System.getProperty("java.vm.name"))
                println("java.home:" + System.getProperty("java.home"))
                println("java.vendor:" + System.getProperty("java.vendor"))
                println("java.version:" + System.getProperty("java.version"))
                println("java.specification.vendor:" + System.getProperty("java.specification.vendor"))
                println("java.vendor.version:" + System.getProperty("java.vendor.version"))
            } catch (e: Exception)
            {
                e.printStackTrace()
            }
            try
            {
                val locale = Locale.getDefault()
                Log.i(TAG, locale.displayCountry)
                Log.i(TAG, locale.displayLanguage)
                Log.i(TAG, locale.displayName)
                Log.i(TAG, locale.isO3Country)
                Log.i(TAG, locale.isO3Language)
                Log.i(TAG, locale.language)
                Log.i(TAG, locale.country)
            } catch (_: Exception)
            {
            }
            try
            {
                Thread.currentThread().name = "t_main"
            } catch (_: Exception)
            {
            }
            Log.i(TAG, "java.library.path:" + System.getProperty("java.library.path"))
            Log.i(TAG, "MainActivity:" + this)
        }
    }
}

internal fun restore_mainlist_state() {
    load_taxi_number()
    load_categories()
    load_compact_flag()
    load_filters()
    load_sorter()
    load_filter_string()
    load_forsummer_flag()
    load_haveac_flag()
    load_restaurants()
}

val restaurantliststore = createRestaurantListStore()

@Composable
fun MainScreen() {
    val restaurants by restaurantliststore.stateFlow.collectAsState()
    val state_mainscreen by globalstore.stateFlow.collectAsState()

    // Log.i(TAG, "size_list=" + restaurants.restaurantlist.size)

    if (state_mainscreen.mainscreen_state == MAINSCREEN.MAINLIST) {
        globalstore.setEditRestaurantId(-1)
        main_list(restaurants)
    }
    else if (state_mainscreen.mainscreen_state == MAINSCREEN.COMPASS)
    {
        // CompassScreen()
        // HINT: no compass view on desktop
        globalstore.setEditRestaurantId(-1)
        main_list(restaurants)
    }
    else if (state_mainscreen.mainscreen_state == MAINSCREEN.ADD)
    {
        globalstore.setEditRestaurantId(-1)
        add_form(null)
    }
    else if (state_mainscreen.mainscreen_state == MAINSCREEN.SETTINGS)
    {
        // globalstore.setEditRestaurantId(-1)
        // settings_form()
        // HINT: !!no settings view for now!!
        globalstore.setEditRestaurantId(-1)
        main_list(restaurants)
    }
    else if (state_mainscreen.mainscreen_state == MAINSCREEN.EDIT)
    {
        edit_form(null)
    }
}

fun set_taxi_number(taxi_num: String?) {
    if (taxi_num.isNullOrEmpty())
    {
        del_g_opts("TAXI_PHONE_NUMBER")
        TAXI_PHONE_NUMBER = null
    }
    else
    {
        set_g_opts("TAXI_PHONE_NUMBER", taxi_num)
        TAXI_PHONE_NUMBER = taxi_num
    }
}

private fun load_categories() {
    val cat_list = orma.selectFromCategory().toList()
    global_categories.clear()
    cat_list.forEach {
        try {
            global_categories[it.id] = it.name
        } catch (e: Exception) {
            e.printStackTrace()
            global_categories[it.id] = "Unknown"
        }
    }
}

private fun load_taxi_number() {
    TAXI_PHONE_NUMBER = get_g_opts("TAXI_PHONE_NUMBER")
}

fun save_compact_flag() {
    val flag = globalstore.getCompactMainList()
    set_g_opts("CompactMainList", flag.toString())
}

fun save_forsummer_flag() {
    val flag = globalstore.getForsummerFilter()
    set_g_opts("ForsummerFilter", flag.toString())
}

fun save_haveac_flag() {
    val flag = globalstore.getHaveacFilter()
    set_g_opts("HaveacFilter", flag.toString())
}

private fun load_compact_flag() {
    val flag = get_g_opts("CompactMainList")
    if (flag.isNullOrEmpty())
    {
        globalstore.setCompactMainList(false)
    }
    else
    {
        try
        {
            globalstore.setCompactMainList(flag.toBoolean())
        }
        catch(e: Exception)
        {
            e.printStackTrace()
        }

    }
}

private fun load_forsummer_flag() {
    val flag = get_g_opts("ForsummerFilter")
    if (flag.isNullOrEmpty())
    {
        globalstore.setForsummerFilter(false)
    }
    else
    {
        try
        {
            globalstore.setForsummerFilter(flag.toBoolean())
        }
        catch(e: Exception)
        {
            e.printStackTrace()
        }

    }
}

private fun load_haveac_flag() {
    val flag = get_g_opts("HaveacFilter")
    if (flag.isNullOrEmpty())
    {
        globalstore.setHaveacFilter(false)
    }
    else
    {
        try
        {
            globalstore.setHaveacFilter(flag.toBoolean())
        }
        catch(e: Exception)
        {
            e.printStackTrace()
        }

    }
}

fun save_filters() {
    val fid = globalstore.getFilterCategoryId()
    set_g_opts("FilterCategoryId", fid.toString())
}

private fun load_filters() {
    val fid = get_g_opts("FilterCategoryId")
    if (fid.isNullOrEmpty())
    {
        globalstore.setFilterCategoryId(-1)
    }
    else
    {
        try
        {
            globalstore.setFilterCategoryId(fid.toLong())
        }
        catch(e: Exception)
        {
            e.printStackTrace()
        }

    }
}

fun save_sorter() {
    val id = globalstore.getSorterId()
    set_g_opts("SorterId", id.toString())
}

private fun load_sorter() {
    val id = get_g_opts("SorterId")
    if (id.isNullOrEmpty())
    {
        globalstore.setSorterId(SORTER.NAME.value)
    }
    else
    {
        try
        {
            globalstore.setSorterId(id.toLong())
        }
        catch(e: Exception)
        {
            e.printStackTrace()
        }

    }
}

fun save_filter_string() {
    val id = globalstore.getFilterString()
    set_g_opts("FilterString", id)
}

private fun load_filter_string() {
    val id = get_g_opts("FilterString")
    globalstore.setFilterString(id)
}

@Composable
fun group_show_open_link_dialog(show_link_click: Boolean, link_str: String, setLinkVars: (Boolean, String) -> Unit)
{
    var show_link_click1 = show_link_click
    var link_str1 = link_str
    if (show_link_click1)
    {
        AlertDialog(onDismissRequest = { link_str1 = ""; show_link_click1 = false; setLinkVars(show_link_click1, link_str1) },
            title = { Text("Open this URL ?") },
            confirmButton = {
                Button(onClick = { open_webpage(link_str1); link_str1 = ""; show_link_click1 = false; setLinkVars(show_link_click1, link_str1) }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { link_str1 = ""; show_link_click1 = false;setLinkVars(show_link_click1, link_str1) }) {
                    Text("No")
                }
            },
            text = { Text("This could be potentially dangerous!" + "\n\n" + link_str1) })
    }
}
