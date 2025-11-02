/*
 * Briar Desktop
 * Copyright (C) 2021-2022 The Briar Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
@file:Suppress("LocalVariableName")

package org.briarproject.briar.desktop

import SETTINGS_HEADER_SIZE
import SnackBarToast
import UIScaleItem
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoffcc.applications.undereat.Log
import com.zoffcc.applications.undereat.MainActivity
import com.zoffcc.applications.undereat.TAG
import global_prefs
import globalstore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.briarproject.briar.desktop.ui.VerticallyScrollableArea
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n
import java.io.File
import kotlin.random.Random
import kotlin.random.nextUInt

@Composable
fun SettingDetails()
{
    SettingDetail(i18n("ui.settings_headline")) {
        val global_store by globalstore.stateFlow.collectAsState()
        general_settings()
        Spacer(modifier = Modifier.height(60.dp))
        Spacer(modifier = Modifier.height(60.dp))
        if (global_store.ormaRunning)
        {
        }
        Spacer(modifier = Modifier.height(60.dp))
        //
        // --------------------------------------
        // HINT: change locale at runtime:
        //
        // Locale.setDefault(Locale.GERMAN)
        // ResourceBundle.clearCache()
        // --------------------------------------
    }
}


@Composable
private fun general_settings()
{
    // ---- set global density to scale the whole UI ----
    var ui_density by remember { mutableStateOf(globalstore.getUiDensity()) }
    DetailItem(label = i18n("ui.ui_density"),
        description = i18n("ui.ui_density")) {
        UIScaleItem(
            label = "" + "%06.2f".format(ui_density),
            description = i18n("ui.drag_slider_to_change")) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.FormatSize, null, Modifier.scale(0.7f))
                Slider(value = ui_density, onValueChange = {
                    ui_density = it
                }, onValueChangeFinished = {
                    globalstore.updateUiDensity(ui_density)
                    Log.i(TAG, "updateUiDensity:ui_density:1: $ui_density")
                }, valueRange = 0.25f..10f, steps = 64)
                Icon(Icons.Default.FormatSize, null)
            }
        }
    }
}

@Composable
fun SettingDetail(header: String, content: @Composable (ColumnScope.() -> Unit)) =
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().height(SETTINGS_HEADER_SIZE).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween)
        {
            Text(header, style = MaterialTheme.typography.h4, color = MaterialTheme.colors.onSurface)
        }
        VerticallyScrollableArea { scrollState ->
            LazyColumn(state = scrollState) {
                item {
                    content()
                }
            }
        }

}

@Composable
fun DetailItem(
    label: String,
    description: String,
    setting: @Composable (RowScope.() -> Unit),
) = Box(modifier = Modifier.padding(start = 15.dp, end = 22.dp, top = 5.dp, bottom = 2.dp)) {
    Box(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
            )
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Row(Modifier.fillMaxWidth()
            .height(SETTINGS_HEADER_SIZE)
            .padding(horizontal = 16.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = description
            }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label)
            setting()
        }
    }
}
