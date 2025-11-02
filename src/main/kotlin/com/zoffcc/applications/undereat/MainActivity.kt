@file:Suppress("UNUSED_PARAMETER", "LocalVariableName", "PropertyName", "ClassName", "FunctionName", "unused", "UNUSED_VARIABLE", "SpellCheckingInspection", "UnnecessaryVariable", "ConvertToStringTemplate", "UNUSED_VALUE", "ReplaceCallWithBinaryOperator", "CascadeIf", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "ControlFlowWithEmptyBody", "MemberVisibilityCanBePrivate", "ConstPropertyName", "ConstPropertyName", "ObjectPropertyName", "ReplaceJavaStaticMethodWithKotlinAnalog", "KotlinConstantConditions", "FoldInitializerAndIfToElvis", "SENSELESS_COMPARISON")
package com.zoffcc.applications.undereat

import SnackBarToast
import com.zoffcc.applications.trifa2.timestampMs
import com.zoffcc.applications.undereat_material.undereat_material.BuildConfig
import global_prefs
import globalstore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

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
