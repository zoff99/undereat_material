@file:Suppress("FunctionName", "LocalVariableName", "SpellCheckingInspection",
    "UselessCallOnNotNull",
    "ConvertToStringTemplate", "UnusedReceiverParameter", "CascadeIf", "LiftReturnOrAssignment",
    "UNUSED_EXPRESSION", "unused", "RedundantExplicitType", "VariableNeverRead", "AssignedValueIsNeverRead")

package com.zoffcc.applications.undereat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoffcc.applications.sorm.OrmaDatabase
import com.zoffcc.applications.sorm.OrmaDatabase.run_query_for_single_result
import com.zoffcc.applications.undereat_material.undereat_material.BuildConfig
import globalstore
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.path
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.isReadable

const val export_sql_filename = "export.uedb"
const val export_ics_filename = "export.ics"
private const val sql_dump_prefix = "import_5907edf"

// private const val sql_export_dir = "export"
private const val sql_export_db_name = "dbexp"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun settings_form(context: Object?) {
    var show_import_alert by remember { mutableStateOf(false) }
    var input_taxi_number by remember {
        val textFieldValue =
            TextFieldValue(text = if (TAXI_PHONE_NUMBER.isNullOrEmpty()) "" else TAXI_PHONE_NUMBER!!)
        mutableStateOf(textFieldValue)
    }
    var import_file_name by remember { mutableStateOf("") }

    if (show_import_alert)
    {
        AlertDialog(onDismissRequest = { },
            title = { Text("Import data") },
            confirmButton = {
                Button(onClick = {
                    try {
                        // now import the db from sqlite file -------------
                        // val dbs_path: String = context.getDir(sql_export_dir, MODE_PRIVATE).absolutePath
                        val dbs_path: String = import_file_name
                        import_file_name = ""
                        // val dbs_path: String = context.getExternalFilesDir(null)!!.absolutePath
                        val sql_export_filename: String = dbs_path
                        Log.i(TAG, "import filename: " + sql_export_filename)
                        val sql_01 = "ATTACH DATABASE '$sql_export_filename' AS $sql_dump_prefix KEY '';"
                        val sql_02 = "SELECT * from $sql_dump_prefix.Restaurant;"
                        // HINT: !!! keep these columns updated with current schema definition!!!
                        // @formatter:off
                        val sql_02a = "DELETE FROM Category;"
                        val sql_02b = "INSERT INTO Category (id, name) " +
                                "select id, name " +
                                "from $sql_dump_prefix.Category;"
                        //
                        val sql_03a = "DELETE FROM Restaurant;"
                        val sql_03b = "INSERT INTO Restaurant (id, name, category_id, address, area_code, lat, lon, rating, comment, active, for_summer, need_reservation, phonenumber, have_ac, added_timestamp, modified_timestamp, only_evening)" +
                                " " +
                                "select id, name, category_id, address, area_code, lat, lon, rating, comment, active, for_summer, need_reservation, phonenumber, have_ac, added_timestamp, modified_timestamp, only_evening" +
                                " " +
                                "from $sql_dump_prefix.Restaurant;"
                        val sql_04 = "DETACH DATABASE $sql_dump_prefix;"

                        Log.i(TAG, "share_local_file:001")

                        // @formatter:on
                        run_query_for_single_result(sql_01)
                        run_query_for_single_result(sql_02)
                        run_query_for_single_result(sql_02a)
                        run_query_for_single_result(sql_02b)
                        run_query_for_single_result(sql_03a)
                        run_query_for_single_result(sql_03b)
                        run_query_for_single_result(sql_04)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
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

    val scrollState = rememberScrollState()
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(4.dp)
        .verticalScroll(scrollState))
    {
        Spacer(modifier = Modifier.height(50.dp))
        Button(
            modifier = Modifier
                .height(50.dp)
                .padding(horizontal = 15.dp),
            shape = RoundedCornerShape(10.dp),
            elevation = ButtonDefaults.buttonElevation(4.dp),
            onClick = {
                // now dump the DB to file in SQL format -------------
                // val dbs_path: String = context.getDir(sql_export_dir, MODE_PRIVATE).absolutePath
                val dbs_path: String = System.getProperty("java.io.tmpdir")
                // val dbs_path: String = context.getExternalFilesDir(null)!!.absolutePath
                val sql_export_filename: String = dbs_path + File.separator + export_sql_filename
                try {
                    File(sql_export_filename).delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                Log.i(TAG, "export filename: " + sql_export_filename)
                val sql_01 = "ATTACH DATABASE '$sql_export_filename' AS $sql_export_db_name;"
                // val sql_02 = "SELECT sqlcipher_export('$sql_export_db_name');"
                val sql_02 = "CREATE TABLE '$sql_export_db_name'.Restaurant AS SELECT * FROM Restaurant;"
                val sql_02_1 = "CREATE TABLE '$sql_export_db_name'.Category AS SELECT * FROM Category;"
                val sql_02_2 = "CREATE TABLE '$sql_export_db_name'.lov AS SELECT * FROM lov;"
                // remove values from "lov" table, since those are kind of private settings
                val sql_02b = "DELETE FROM '$sql_export_db_name'.lov;"
                val sql_03 = "DETACH DATABASE $sql_export_db_name;"
                run_query_for_single_result(sql_01)
                run_query_for_single_result(sql_02)
                run_query_for_single_result(sql_02_1)
                run_query_for_single_result(sql_02_2)
                run_query_for_single_result(sql_02b)
                run_query_for_single_result(sql_03)

                /*
                val file_uri = FileProvider.getUriForFile(
                    context, "com.zoffcc.applications.undereat.std_fileprovider",
                    File(sql_export_filename))
                Log.i(TAG, "share_local_file:file_uri : " + file_uri)

                val intent = Intent(Intent.ACTION_SEND, file_uri)
                intent.putExtra(Intent.EXTRA_STREAM, file_uri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                val myMime = MimeTypeMap.getSingleton()
                var mimeType = myMime.getMimeTypeFromExtension(
                    MimeTypeMap.getFileExtensionFromUrl(sql_export_filename)
                )
                if (mimeType == null) {
                    mimeType = "application/octet-stream"
                }

                Log.i(TAG, "share_local_file:mime type: " + mimeType)
                intent.setDataAndType(file_uri, mimeType)
                try {
                    context.startActivity(Intent.createChooser(intent, "Share"))
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }
                 */
            },
            content = {
                Text(
                    text = "Export Database",
                    style = TextStyle(
                        fontSize = 15.sp,
                    )
                )
            }
        )
        Spacer(modifier = Modifier.height(50.dp))
        val launcher = rememberFilePickerLauncher { file ->
            Log.i(TAG, "selected file for import: " + file)
            if (file != null)
            {
                if (Path(file.absolutePath()).isReadable())
                {
                    import_file_name = file.absolutePath()
                    show_import_alert = true
                }
                else
                {
                    Toast.makeText(null, "Import File is not readable", Toast.LENGTH_SHORT).show()
                }
            }
            else
            {
                Toast.makeText(null, "Import File is null", Toast.LENGTH_SHORT).show()
            }
        }
        Button(
            modifier = Modifier
                .height(50.dp)
                .padding(horizontal = 15.dp),
            shape = RoundedCornerShape(10.dp),
            elevation = ButtonDefaults.buttonElevation(4.dp),
            onClick = {
                // show_import_alert = true
                launcher.launch()
            },
            content = {
                Text(
                    text = "Import Database",
                    style = TextStyle(
                        fontSize = 15.sp,
                    )
                )
            }
        )
        Spacer(modifier = Modifier.height(50.dp))
        Text(modifier = Modifier
            .fillMaxWidth()
            .padding(end = 15.dp, start = 15.dp, bottom = 5.dp),
            text = "Taxi Phone Number",
            fontSize = 18.sp
        )
        TextField(modifier = Modifier
            .fillMaxWidth()
            .padding(end = 15.dp, start = 10.dp, bottom = 5.dp),
            value = input_taxi_number, placeholder = { Text(text = "Taxi Phone Number", fontSize = 14.sp) },
            onValueChange = {
                input_taxi_number = it
                set_taxi_number(input_taxi_number.text)
            })

        Row {
            Column(modifier = Modifier.width(16.dp)) {

            }
            Column {
                Spacer(modifier = Modifier.height(50.dp))
                var git_hash = ""
                try {
                    git_hash = BuildConfig.GIT_COMMIT_HASH
                } catch (_: Exception) {
                }
                Text("git hash: " + git_hash, fontSize = 14.sp)

                var version_code = ""
                try {
                    version_code = "" + BuildConfig.APP_VERSION
                } catch (_: Exception) {
                }
                Text("version: " + version_code, fontSize = 14.sp)


                var debug__sqlite_user_version: String? = "unknown"
                try {
                    debug__sqlite_user_version =
                        run_query_for_single_result("PRAGMA user_version")
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }

                var debug__sqlite_version: String? = "unknown"
                try {
                    debug__sqlite_version =
                        run_query_for_single_result("SELECT sqlite_version()")
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }

                var debug__cipher_version: String? = "unknown"
                try {
                    debug__cipher_version =
                        run_query_for_single_result("PRAGMA cipher_version")
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }

                var debug__cipher_provider: String? = "unknown"
                try {
                    debug__cipher_provider =
                        run_query_for_single_result("PRAGMA cipher_provider")
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }

                var debug__cipher_provider_version: String? = "unknown"
                try {
                    debug__cipher_provider_version =
                        run_query_for_single_result("PRAGMA cipher_provider_version")
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                //
                var debug_output = ""
                debug_output = debug_output + "--- sorma2 ---" + "\n"
                debug_output = debug_output + "sorma_version=" + OrmaDatabase.getVersion() + "\n"
                debug_output = debug_output + "cipher_version=" + debug__cipher_version + "\n"
                debug_output = debug_output + "sqlite_version=" + debug__sqlite_version + "\n"
                debug_output = debug_output + "cipher_provider=" + debug__cipher_provider + "\n"
                debug_output = debug_output + "cipher_provider_version=" + debug__cipher_provider_version + "\n"
                Text("" + debug_output, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(50.dp))
        Button(
            modifier = Modifier
                .height(50.dp)
                .padding(horizontal = 15.dp),
            shape = RoundedCornerShape(10.dp),
            elevation = ButtonDefaults.buttonElevation(4.dp),
            onClick = {
                globalstore.updateMainscreenState(MAINSCREEN.MAINLIST)
            },
            content = {
                Text(
                    text = "Back",
                    style = TextStyle(
                        fontSize = 15.sp,
                    )
                )
            }
        )
    }
}

