package com.starmax.sdkdemo.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.CheckBox
import androidx.compose.material.icons.twotone.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.starmax.sdkdemo.pages.lazyKoinViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import com.starmax.sdkdemo.viewmodel.SetStateViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SetStateDialog() {
    val viewModel: SetStateViewModel = koinViewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    LaunchedEffect(Unit) {
        viewModel.getState()
    }

    SetStateDialogView(homeViewModel = homeViewModel, viewModel = viewModel)
}

@Composable
fun SetStateDialogView(homeViewModel: HomeViewModel, viewModel: SetStateViewModel) {
    AlertDialog(
        modifier = Modifier.fillMaxHeight(1f),
        confirmButton = {
            ElevatedButton(
                onClick = {
                    viewModel.setState()
                    homeViewModel.toggleSetState()
                }, colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.offset(15.dp)
            ) {
                Text(text = "确定")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = {
                homeViewModel.toggleSetState()
            }) {
                Text(text = "取消")
            }
        },
        title = {
            Text(
                text = "设置状态",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(15.dp)
            )
        },
        onDismissRequest = { homeViewModel.toggleSetState() },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item{
                    Row(
                        verticalAlignment = Alignment.CenterVertically,

                    ) {
                        Text(text = "时间制", style = MaterialTheme.typography.labelMedium)
                        IconToggleButton(checked = viewModel.timeFormat == 1, onCheckedChange = { it ->
                            viewModel.timeFormat = 1
                        }) {
                            Icon(
                                if (viewModel.timeFormat == 1) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                                "12小时制"
                            )
                        }
                        Text(text = "12小时制", style = MaterialTheme.typography.labelSmall)

                        IconToggleButton(checked = viewModel.timeFormat == 0, onCheckedChange = { it ->
                            viewModel.timeFormat = 0
                        }) {
                            Icon(
                                if (viewModel.timeFormat == 0) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                                "24小时制"
                            )
                        }
                        Text(text = "24小时制", style = MaterialTheme.typography.labelSmall)
                    }
                }
                item{
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "单位制", style = MaterialTheme.typography.labelMedium)
                        IconToggleButton(checked = viewModel.unitFormat == 0, onCheckedChange = { it ->
                            viewModel.unitFormat = 0
                        }) {
                            Icon(
                                if (viewModel.unitFormat == 0) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                                "公制"
                            )
                        }
                        Text(text = "公制", style = MaterialTheme.typography.labelSmall)

                        IconToggleButton(checked = viewModel.unitFormat == 1, onCheckedChange = { it ->
                            viewModel.unitFormat = 1
                        }) {
                            Icon(
                                if (viewModel.unitFormat == 1) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                                "英制"
                            )
                        }
                        Text(text = "英制", style = MaterialTheme.typography.labelSmall)
                    }
                }
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "温度制", style = MaterialTheme.typography.labelMedium)
                        IconToggleButton(checked = viewModel.tempFormat == 0, onCheckedChange = { it ->
                            viewModel.tempFormat = 0
                        }) {
                            Icon(
                                if (viewModel.tempFormat == 0) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                                "摄氏"
                            )
                        }
                        Text(text = "摄氏", style = MaterialTheme.typography.labelSmall)

                        IconToggleButton(checked = viewModel.tempFormat == 1, onCheckedChange = { it ->
                            viewModel.tempFormat = 1
                        }) {
                            Icon(
                                if (viewModel.tempFormat == 1) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                                "华氏"
                            )
                        }
                        Text(text = "华氏", style = MaterialTheme.typography.labelSmall)
                    }
                }
                item {
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "语言", style = MaterialTheme.typography.labelMedium)
                        LazyVerticalGrid(
                            modifier = Modifier.height(350.dp),
                            columns = GridCells.Fixed(3)) {
                            val languageList = listOf(
                                "简体中文",
                                "繁体中文",
                                "英文",
                                "俄语",
                                "法语",
                                "西班牙语",
                                "德语",
                                "日语",
                                "意大利语",
                                "韩语",
                                "荷兰语",
                                "泰语",
                                "越南语",
                                "马来语",
                                "印尼语",
                                "葡萄牙语",
                                "罗马尼亚语",
                                "波兰语",
                                "土耳其语",
                                "蒙古语",
                                "印地语"
                            )

                            items(languageList.size, key = {
                                languageList[it]
                            }) { i ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconToggleButton(checked = viewModel.language == i, onCheckedChange = { it ->
                                        viewModel.language = i
                                    }) {
                                        Icon(
                                            if (viewModel.language == i) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                                            languageList[i]
                                        )
                                    }

                                    Text(text = languageList[i], style = MaterialTheme.typography.labelSmall)
                                }

                            }
                        }
                    }
                }
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "背光时长", style = MaterialTheme.typography.labelMedium)
                        Slider(value = viewModel.backlighting.toFloat(), onValueChange = {
                            viewModel.backlighting = it.toInt()
                        }, steps = 5, valueRange = 5f..25f, modifier = Modifier.offset(x = 15.dp))
                    }
                }
                item{
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "屏幕亮度", style = MaterialTheme.typography.labelMedium)
                        Slider(value = viewModel.screen.toFloat(), onValueChange = {
                            viewModel.screen = it.toInt()
                        }, steps = 5, valueRange = 0f..60f, modifier = Modifier.offset(x = 15.dp))
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "抬手亮开关", style = MaterialTheme.typography.labelMedium)
                        Switch(checked = viewModel.wristUp, onCheckedChange = {
                            viewModel.wristUp = it
                        })
                    }
                }
            }
        })
}

@Preview(showBackground = true)
@Composable
fun PreviewSetStateDialog() {
    SetStateDialogView(viewModel = SetStateViewModel(), homeViewModel = HomeViewModel())
}