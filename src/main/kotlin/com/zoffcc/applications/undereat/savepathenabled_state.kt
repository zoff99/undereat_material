@file:Suppress("SpellCheckingInspection", "PropertyName", "ClassName", "FunctionName", "ConvertToStringTemplate")

package com.zoffcc.applications.undereat

import global_prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

const val TAG = "trifa.GlobalStore"

data class globalstore_state(
    val mainwindow_minimized: Boolean = false,
    val mainwindow_focused: Boolean = true,
    val ui_scale: Float = 1.0f,
    val ui_density: Float = 1.0f,
    val default_density: Float = 1.0f,
    val ormaRunning: Boolean = false,
    val app_startup: Boolean = true,
    // -------------------
    val mainscreen_state: MAINSCREEN = MAINSCREEN.MAINLIST,
    val restaurantId: Long = -1,
    val filterCategoryId: Long = -1,
    val compactMainList: Boolean = false,
    val forsummerFilter: Boolean = false,
    val haveacFilter: Boolean = false,
    val sorterId: Long = 0,
    val filterString: String? = null
)

interface GlobalStore {
    fun updateMinimized(value: Boolean)
    fun updateFocused(value: Boolean)
    fun updateUiScale(value: Float)
    fun updateUiDensity(value: Float)
    fun setDefaultDensity(value: Float)
    fun isMinimized(): Boolean
    fun isFocused(): Boolean
    fun loadUiScale()
    fun getUiScale(): Float
    fun loadUiDensity()
    fun getUiDensity(): Float
    fun setApp_startup(value: Boolean)
    fun getApp_startup(): Boolean
    fun setOrmaRunning(value: Boolean)
    fun getOrmaRunning(): Boolean
    // ------------------
    fun setEditRestaurantId(value: Long)
    fun setFilterCategoryId(value: Long)
    fun setSorterId(value: Long)
    fun setFilterString(value: String?)
    fun setCompactMainList(value: Boolean)
    fun setForsummerFilter(value: Boolean)
    fun setHaveacFilter(value: Boolean)
    fun updateMainscreenState(value: MAINSCREEN)
    fun getMainscreenState(): MAINSCREEN
    fun getRestaurantId(): Long
    fun getFilterCategoryId(): Long
    fun getSorterId(): Long
    fun getFilterString(): String?
    fun getCompactMainList(): Boolean
    fun getForsummerFilter(): Boolean
    fun getHaveacFilter(): Boolean
    val stateFlow: StateFlow<globalstore_state>
    val state get() = stateFlow.value
}

@OptIn(DelicateCoroutinesApi::class)
fun CoroutineScope.createGlobalStore(): GlobalStore {
    val mutableStateFlow = MutableStateFlow(globalstore_state())
    return object : GlobalStore
    {
        override val stateFlow: StateFlow<globalstore_state> = mutableStateFlow

        override fun updateMinimized(value: Boolean)
        {
            mutableStateFlow.value = state.copy(mainwindow_minimized = value)
        }

        override fun updateFocused(value: Boolean)
        {
            mutableStateFlow.value = state.copy(mainwindow_focused = value)
        }

        override fun updateUiScale(value: Float)
        {
            GlobalScope.launch {
                try
                {
                    global_prefs.putFloat("main.ui_scale_factor", value)
                }
                catch(_: Exception)
                {
                }
            }
            mutableStateFlow.value = state.copy(ui_scale = value)
        }

        override fun setDefaultDensity(value: Float)
        {
            mutableStateFlow.value = state.copy(default_density = value)
        }

        override fun updateUiDensity(value: Float)
        {
            GlobalScope.launch {
                try
                {
                    global_prefs.putFloat("main.ui_density_factor", value)
                }
                catch(_: Exception)
                {
                }
            }
            mutableStateFlow.value = state.copy(ui_density = value)
        }

        override fun isMinimized(): Boolean
        {
            return state.mainwindow_minimized
        }

        override fun isFocused(): Boolean
        {
            return state.mainwindow_focused
        }

        override fun setApp_startup(value: Boolean)
        {
            mutableStateFlow.value = state.copy(app_startup = value)
            Log.i(TAG, "setApp_startup: " + value)
        }

        override fun getApp_startup(): Boolean
        {
            return state.app_startup
        }

        override fun getOrmaRunning(): Boolean
        {
            return state.ormaRunning
        }

        override fun setOrmaRunning(value: Boolean)
        {
            mutableStateFlow.value = state.copy(ormaRunning = value)
        }

        override fun loadUiScale()
        {
            var value = 1.0f
            try
            {
                val tmp = global_prefs.get("main.ui_scale_factor", null)
                if (tmp != null)
                {
                    value = tmp.toFloat()
                    Log.i(TAG, "loadUiScale:density: $value")
                }
            } catch (_: Exception)
            {
            }
            mutableStateFlow.value = state.copy(ui_scale = value)
        }

        override fun loadUiDensity()
        {
            var value = 1.0f
            try
            {
                value = state.default_density
                Log.i(TAG, "current default density = " + value)
            }
            catch(_: Exception)
            {
            }
            try
            {
                val tmp = global_prefs.get("main.ui_density_factor", null)
                if (tmp != null)
                {
                    value = tmp.toFloat()
                    Log.i(TAG, "loadUiDensity:density: $value")
                }
            } catch (_: Exception)
            {
            }
            Log.i(TAG, "loading density = " + value)
            mutableStateFlow.value = state.copy(ui_density = value)
        }

        override fun getUiScale(): Float
        {
            return state.ui_scale
        }

        override fun getUiDensity(): Float
        {
            return state.ui_density
        }

        override fun updateMainscreenState(value: MAINSCREEN) {
            mutableStateFlow.value = state.copy(mainscreen_state = value)
        }

        override fun setEditRestaurantId(value: Long) {
            mutableStateFlow.value = state.copy(restaurantId = value)
        }

        override fun setFilterCategoryId(value: Long) {
            mutableStateFlow.value = state.copy(filterCategoryId = value)
        }

        override fun setSorterId(value: Long) {
            Log.i(TAG, "setSorterId:value=" + value)
            if (value == SORTER.DISTANCE.value) {
                Log.i(TAG, "setSorterId:2")
                try {
                } catch(e: java.lang.Exception) {
                    e.printStackTrace()
                }
            } else {
                try {
                } catch(e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
            mutableStateFlow.value = state.copy(sorterId = value)
        }

        override fun setFilterString(value: String?) {
            mutableStateFlow.value = state.copy(filterString = value)
        }

        override fun setCompactMainList(value: Boolean) {
            mutableStateFlow.value = state.copy(compactMainList = value)
        }

        override fun setForsummerFilter(value: Boolean) {
            mutableStateFlow.value = state.copy(forsummerFilter = value)
        }

        override fun setHaveacFilter(value: Boolean) {
            mutableStateFlow.value = state.copy(haveacFilter = value)
        }

        override fun getMainscreenState(): MAINSCREEN
        {
            return state.mainscreen_state
        }

        override fun getRestaurantId(): Long
        {
            return state.restaurantId
        }

        override fun getFilterCategoryId(): Long
        {
            return state.filterCategoryId
        }

        override fun getSorterId(): Long {
            return state.sorterId
        }

        override fun getFilterString(): String? {
            return state.filterString
        }

        override fun getCompactMainList(): Boolean {
            return state.compactMainList
        }

        override fun getForsummerFilter(): Boolean {
            return state.forsummerFilter
        }

        override fun getHaveacFilter(): Boolean {
            return state.haveacFilter
        }
    }
}

