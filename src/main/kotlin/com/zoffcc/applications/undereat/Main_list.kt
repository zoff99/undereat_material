@file:Suppress("LocalVariableName",
    "SpellCheckingInspection", "ConvertToStringTemplate", "UselessCallOnNotNull", "DEPRECATION",
    "KotlinConstantConditions"
)

package com.zoffcc.applications.undereat

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zoffcc.applications.sorm.Category
import com.zoffcc.applications.undereat.corefuncs.DEMO_SHOWCASE_DEBUG_ONLY
import com.zoffcc.applications.undereat.corefuncs.SpecialCategory.SPECIAL_CATEGORY_ALL
import com.zoffcc.applications.undereat.corefuncs.SpecialCategory.SPECIAL_CATEGORY_NOSTORE
import com.zoffcc.applications.undereat.corefuncs.orma
import com.zoffcc.applications.undereat_material.undereat_material.BuildConfig
import globalstore
import randomDebugBorder
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun main_list(restaurants: StateRestaurantList) {

    var ics_item_name by remember { mutableStateOf("") }
    var ics_item_address by remember { mutableStateOf("") }
    var ics_y by remember { mutableIntStateOf(1900) }
    var ics_m by remember { mutableIntStateOf(0) }
    var ics_d by remember { mutableIntStateOf(0) }
    var ics_24hour by remember { mutableIntStateOf(0) }
    var ics_minute by remember { mutableIntStateOf(0) }

    var git_hash = ""
    try {
        git_hash = BuildConfig.GIT_COMMIT_HASH
    } catch (_: Exception) {
    }

    val all_cat = Category()
    all_cat.id = SPECIAL_CATEGORY_ALL.value.toLong()
    all_cat.name = "* All *"
    //
    val noshops_cat = Category()
    noshops_cat.id = SPECIAL_CATEGORY_NOSTORE.value.toLong()
    noshops_cat.name = "* No Stores *"
    //
    val cat_list = ArrayList<Category>()
    cat_list.add(all_cat)
    cat_list.add(noshops_cat)
    cat_list.addAll(orma.selectFromCategory().toList())
    //
    val filter_category_id = globalstore.getFilterCategoryId()
    var current_filter_cat_id_pos = 0
    cat_list.forEachIndexed { index, category ->
        if (category.id == filter_category_id) {
            current_filter_cat_id_pos = index
        }
    }

    val cat_isDropDownExpanded = remember { mutableStateOf(false) }
    val cat_itemPosition = remember { mutableIntStateOf(current_filter_cat_id_pos) }
    //
    val sort_list = ArrayList<Sorter>()
    val name_sorter = Sorter(id = SORTER.NAME.value, name = "Name")
    val adress_sorter = Sorter(id = SORTER.ADDRESS.value, name = "Adresse")
    val distance_sorter = Sorter(id = SORTER.DISTANCE.value, name = "Distanz")
    val rating_sorter = Sorter(id = SORTER.RATING.value, name = "Bewertung")
    val added_date_sorter = Sorter(id = SORTER.ADDED_DATE.value, name = "Erstellt")
    val modified_date_sorter = Sorter(id = SORTER.ADDED_DATE.value, name = "Bearbeitet")
    sort_list.add(name_sorter)
    sort_list.add(adress_sorter)
    sort_list.add(distance_sorter)
    sort_list.add(rating_sorter)
    sort_list.add(added_date_sorter)
    sort_list.add(modified_date_sorter)
    //
    val sorter_id = globalstore.getSorterId()
    var current_sort_id_pos = SORTER.NAME.value.toInt()
    sort_list.forEachIndexed { index, sorter ->
        if (sorter.id == sorter_id) {
            current_sort_id_pos = index
        }
    }
    val sort_isDropDownExpanded = remember { mutableStateOf(false) }
    val sort_itemPosition = remember { mutableIntStateOf(current_sort_id_pos) }
    //
    val filter_string_current = globalstore.getFilterString()
    var input_filter by remember {
        val textFieldValue =
            TextFieldValue(text = if (filter_string_current.isNullOrEmpty()) "" else filter_string_current)
        mutableStateOf(textFieldValue)
    }
    // --> this works only as long as the component ist shwoing // val listState = rememberLazyListState()
    // remember lazylist state even if components get disposed
    val listState = rememberForeverLazyListState(key = "mainlist")

    Column {

        // Header Row ---------------------
        Row {
            Button(
                modifier = Modifier
                    .height(50.dp)
                    .padding(4.dp),
                shape = RoundedCornerShape(10.dp),
                elevation = ButtonDefaults.buttonElevation(4.dp),
                onClick = {
                    globalstore.updateMainscreenState(MAINSCREEN.ADD)
                },
                content = {
                    Text(
                        text = "add",
                        style = TextStyle(
                            fontSize = 12.sp,
                        )
                    )
                }
            )
            Row(modifier = Modifier
                .fillMaxWidth()
                .weight(10F)
                .align(Alignment.CenterVertically)
                .padding(2.dp)
                .clickable {
                    globalstore.updateMainscreenState(MAINSCREEN.SETTINGS)
                })
            {
                var num_text = "${restaurants.restaurantlist.size} Restaurants"
                if (DEMO_SHOWCASE_DEBUG_ONLY)
                {
                    num_text = "${restaurants.restaurantlist.size} Restaurants" + "\n" + git_hash
                }
                Text(
                    text = num_text,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(10F)
                        .align(Alignment.CenterVertically)
                        .padding(2.dp),
                    style = TextStyle(
                        fontSize = 17.sp,
                        textAlign = TextAlign.Start,
                    )
                )
            }
            /*
            Button(
                modifier = Modifier
                    .height(50.dp)
                    .padding(4.dp),
                shape = RoundedCornerShape(10.dp),
                elevation = ButtonDefaults.buttonElevation(4.dp),
                onClick = {
                },
                content = {
                    Icon(
                        modifier = Modifier
                            .randomDebugBorder()
                            .padding(0.dp),
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call Restaurant"
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        modifier = Modifier.padding(0.dp),
                        text = "Taxi",
                        style = TextStyle(
                            fontSize = 13.sp,
                        )
                    )
                }
            )
             */
        }
        // Header Row ---------------------


        // select and filter Row ---------------------
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            content = {

                // dropdown: sort --------------------------
                Box {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .randomDebugBorder()
                            .height(60.dp)
                            .padding(start = 0.dp)
                            .clickable {
                                sort_isDropDownExpanded.value = true
                            }
                    ) {
                        if ((sort_itemPosition.intValue < 0) || (sort_itemPosition.intValue >= sort_list.size))
                        {
                            Text(
                                text = "????",
                                fontSize = 11.sp
                            )
                        }
                        else {
                            Text(
                                text = sort_list[sort_itemPosition.intValue].name,
                                fontSize = 11.sp
                            )
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "select Sort Order")
                    }
                    DropdownMenu(
                        expanded = sort_isDropDownExpanded.value,
                        onDismissRequest = {
                            sort_isDropDownExpanded.value = false
                        }) {
                        sort_list.forEachIndexed { index, sorter_ ->
                            DropdownMenuItem(
                                modifier = Modifier
                                    .height(45.dp)
                                    .padding(1.dp),
                                text = {
                                    Text(text = sorter_.name, fontSize = 19.sp)
                                },
                                onClick = {
                                    sort_isDropDownExpanded.value = false
                                    sort_itemPosition.intValue = index
                                    // Log.i(TAG, "SSO1:" + sort_isDropDownExpanded.value + " " + sort_itemPosition.value)
                                    // Log.i(TAG, "SSO2:" + sort_list[sort_itemPosition.value].id + " " + sort_list[sort_itemPosition.value].name)
                                    globalstore.setSorterId(sort_list[sort_itemPosition.intValue].id)
                                    save_sorter()
                                    load_restaurants()
                                })
                        }
                    }
                }
                // dropdown: sort --------------------------



                // dropdown: filter --------------------------
                Box {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .randomDebugBorder()
                            .defaultMinSize(minWidth = 120.dp)
                            .height(60.dp)
                            .padding(start = 0.dp)
                            .clickable {
                                cat_isDropDownExpanded.value = true
                            }
                    ) {
                        Text(text = cat_list[cat_itemPosition.intValue].name, fontSize = 11.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "select Category")
                    }
                    DropdownMenu(
                        expanded = cat_isDropDownExpanded.value,
                        onDismissRequest = {
                            cat_isDropDownExpanded.value = false
                        }) {
                        cat_list.forEachIndexed { index, category_ ->
                            DropdownMenuItem(
                                modifier = Modifier
                                    .height(45.dp)
                                    .padding(1.dp),
                                text = {
                                    Text(text = category_.name, fontSize = 19.sp)
                                },
                                onClick = {
                                    cat_isDropDownExpanded.value = false
                                    cat_itemPosition.intValue = index
                                    //Log.i(TAG, "CTT1:" + cat_isDropDownExpanded.value + " " + cat_itemPosition.value)
                                    //Log.i(TAG, "CTT2:" + cat_list[cat_itemPosition.value].id + " " + cat_list[cat_itemPosition.value].name)
                                    globalstore.setFilterCategoryId(cat_list[cat_itemPosition.intValue].id)
                                    save_filters()
                                    load_restaurants()
                                })
                        }
                    }
                }
                // dropdown: filter --------------------------


                Spacer(modifier = Modifier
                    .defaultMinSize(minWidth = 0.dp)
                    .weight(1000F)
                    .height(10.dp))

                val state_compactMainlist by globalstore.stateFlow.collectAsState()
                // switch: compact list --------------------------
                JCSwitch(
                    switchWidth = 38.dp,
                    switchHeight = 38.dp,
                    isChecked = state_compactMainlist.compactMainList,
                    enabledColor = SwitchDefaults.colors().checkedTrackColor,
                    thumbContent = if (state_compactMainlist.compactMainList) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                                tint = SwitchDefaults.colors().checkedTrackColor.copy(green = 0.8f, alpha = 0.76f)
                            )
                        }
                    } else {
                        null
                    },
                    onCheckChanged = {
                        globalstore.setCompactMainList(it)
                        save_compact_flag()
                    }
                )
                // switch: compact list --------------------------

                Spacer(modifier = Modifier
                    .width(8.dp)
                    .height(1.dp))

                // switch: have_ac flag --------------------------
                val flag_ac_color = Color.Blue.copy(green = 0.8f, alpha = 0.4f)
                val flag_ac_color_brighter = Color.Blue.copy(green = 0.8f, alpha = 0.76f)
                JCSwitch(
                    switchWidth = 38.dp,
                    switchHeight = 38.dp,
                    isChecked = state_compactMainlist.haveacFilter,
                    enabledColor = flag_ac_color_brighter,
                    disabledColor = flag_ac_color,
                    thumbContent = if (state_compactMainlist.haveacFilter) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                                tint = flag_ac_color_brighter
                            )
                        }
                    } else {
                        null
                    },
                    onCheckChanged = {
                        globalstore.setHaveacFilter(it)
                        save_haveac_flag()
                        load_restaurants()
                    }
                )
                // switch: have_ac flag --------------------------

                Spacer(modifier = Modifier
                    .width(8.dp)
                    .height(1.dp))

                // switch: for_summer flag --------------------------
                val flag_color = Color.Yellow.copy(green = 0.8f, alpha = 0.4f)
                val flag_color_brighter = Color.Yellow.copy(green = 0.8f, alpha = 0.76f)
                JCSwitch(
                    switchWidth = 38.dp,
                    switchHeight = 38.dp,
                    isChecked = state_compactMainlist.forsummerFilter,
                    enabledColor = flag_color_brighter,
                    disabledColor = flag_color,
                    thumbContent = if (state_compactMainlist.forsummerFilter) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                                tint = flag_color_brighter
                            )
                        }
                    } else {
                        null
                    },
                    onCheckChanged = {
                        globalstore.setForsummerFilter(it)
                        save_forsummer_flag()
                        load_restaurants()
                    }
                )
                // switch: for_summer flag --------------------------

                Spacer(modifier = Modifier
                    .width(8.dp)
                    .height(1.dp))
            }
        )
        // select and filter Row ---------------------

        Row(modifier = Modifier.padding(start = 3.dp, end = 13.dp, bottom = 8.dp)) {
            // ----------- search filter input -----------
            val interactionSource = remember { MutableInteractionSource() }
            val textColor = MaterialTheme.colorScheme.onBackground
            val mergedTextStyle = LocalTextStyle.current.merge(TextStyle(color = textColor))
            val rounded_broder_dp = 6.dp
            BasicTextField(
                value = input_filter,
                textStyle = mergedTextStyle,
                singleLine = true,
                onValueChange = { targetValue ->
                    input_filter = targetValue
                    globalstore.setFilterString(input_filter.text)
                    save_filter_string()
                    load_restaurants()
                },
                modifier = Modifier
                    .randomDebugBorder()
                    .clip(RoundedCornerShape(rounded_broder_dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(rounded_broder_dp)
                    )
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(4.dp),
                interactionSource = interactionSource,
                cursorBrush = SolidColor(Color.Unspecified),
                decorationBox = @Composable { innerTextField ->
                    Column(
                        modifier = Modifier
                            // .verticalScroll(rememberScrollState())
                            .padding(all = 1.dp),
                    ) {
                        innerTextField()
                    }
                }
            )
            // ----------- search filter input -----------
        }

        var showDatePicker by remember { mutableStateOf(false) }
        var showTimePicker by remember { mutableStateOf(false) }

        if (showDatePicker) {
            DatePicker2(onDateSelected = {
                val calendar = Calendar.getInstance()
                calendar.setTime(it)
                ics_d = calendar.get(Calendar.DAY_OF_MONTH) // starts with 1
                ics_m = calendar.get(Calendar.MONTH) + 1 // between 0 and 11, with the value 0 representing January
                ics_y = calendar.get(Calendar.YEAR) // the year represented by this date, minus 1900
                Log.i(TAG, "ics1: " + ics_item_name + " "
                        + ics_item_address + " "
                        + ics_y + " "
                        + ics_m + " "
                        + ics_d + " "
                        + ics_24hour + " "
                        + ics_minute + " "
                )
                showDatePicker = false
                showTimePicker = true
            }, onDismissRequest = {
                ics_item_name = ""
                ics_item_address = ""
                ics_d = 0
                ics_m = 0
                ics_y = 1900
                ics_minute = 0
                ics_24hour = 0
                showDatePicker = false
            })
        }

        if (showTimePicker) {
            val currentTime = Calendar.getInstance()
            val timePickerState = rememberTimePickerState(
                initialHour = currentTime.time.hours,
                initialMinute = currentTime.time.minutes,
                is24Hour = true,
            )

            Dialog(onDismissRequest = {}, properties = DialogProperties()) {
                Row(modifier = Modifier
                    .wrapContentSize()
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(size = 16.dp)
                    )) {
                    Spacer(modifier = Modifier.width(25.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(25.dp))
                        TimePicker(
                            state = timePickerState,
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(bottom = 16.dp, end = 16.dp)
                        ) {
                            Button(onClick = {
                                ics_item_name = ""
                                ics_item_address = ""
                                ics_d = 0
                                ics_m = 0
                                ics_y = 1900
                                ics_minute = 0
                                ics_24hour = 0
                                showTimePicker = false
                            }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(30.dp))
                            Button(onClick = {
                                ics_minute = timePickerState.minute // 0 - 59
                                ics_24hour = timePickerState.hour // 0 - 23
                                Log.i(TAG, "ics2: " + ics_item_name + " "
                                        + ics_item_address + " "
                                        + ics_y + " "
                                        + ics_m + " "
                                        + ics_d + " "
                                        + ics_24hour + " "
                                        + ics_minute + " "
                                )

                                try {
                                } catch(e: Exception) {
                                    e.printStackTrace()
                                }

                                showTimePicker = false
                            }) {
                                Text("Ok")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(25.dp))
                }
            }
        }

        // Data list ----------------------
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxHeight().fillMaxWidth().padding(start = 2.dp, end = 10.dp).randomDebugBorder(),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                items(items = restaurants.restaurantlist, key = { item -> item.id }, contentType = { item -> item::class }) { data ->
                    RestaurantCard(-999, data, null)
                }
            }
            VerticalScrollbar(adapter = rememberScrollbarAdapter(listState),
                modifier = Modifier.fillMaxHeight().
                align(CenterEnd).
                width(10.dp))
        }
        // Data list ----------------------
    }
}

/**
 * Static field, contains all scroll values
 */
private val SaveMap = mutableMapOf<String, KeyParams>()

private data class KeyParams(
    val params: String = "",
    val index: Int,
    val scrollOffset: Int
)

/**
 * Save scroll state on all time.
 * @param key value for comparing screen
 * @param params arguments for find different between equals screen
 * @param initialFirstVisibleItemIndex see [LazyListState.firstVisibleItemIndex]
 * @param initialFirstVisibleItemScrollOffset see [LazyListState.firstVisibleItemScrollOffset]
 */
@Composable
fun rememberForeverLazyListState(
    key: String,
    params: String = "",
    initialFirstVisibleItemIndex: Int = 0,
    initialFirstVisibleItemScrollOffset: Int = 0
): LazyListState {
    val scrollState = rememberSaveable(saver = LazyListState.Saver) {
        var savedValue = SaveMap[key]
        if (savedValue?.params != params) savedValue = null
        val savedIndex = savedValue?.index ?: initialFirstVisibleItemIndex
        val savedOffset = savedValue?.scrollOffset ?: initialFirstVisibleItemScrollOffset
        LazyListState(
            savedIndex,
            savedOffset
        )
    }
    DisposableEffect(Unit) {
        onDispose {
            val lastIndex = scrollState.firstVisibleItemIndex
            val lastOffset = scrollState.firstVisibleItemScrollOffset
            SaveMap[key] = KeyParams(params, lastIndex, lastOffset)
        }
    }
    return scrollState
}

@Composable
fun theme_is_lightmode(): Boolean {
    val isLight = MaterialTheme.colorScheme.onPrimary.luminance() > 0.5
    Log.i(TAG, "isLight = " + isLight)
    return isLight
}

