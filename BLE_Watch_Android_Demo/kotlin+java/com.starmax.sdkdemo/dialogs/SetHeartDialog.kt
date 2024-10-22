package com.starmax.sdkdemo.dialogs

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.android.material.timepicker.MaterialTimePicker
import com.starmax.sdkdemo.pages.lazyKoinViewModel
import com.starmax.sdkdemo.viewmodel.HeartRateViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetHeartDialog() {
    val activity = LocalContext.current as AppCompatActivity
    val viewModel: HeartRateViewModel = koinViewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    LaunchedEffect(Unit) {
        viewModel.getData()
    }

    Dialog(
        onDismissRequest = { homeViewModel.toggleHeartRateOpen() }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "设置心率检测间隔和范围",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "开始时间", style = MaterialTheme.typography.labelMedium)
                    TextButton(onClick = {
                        activity.let {
                            val picker = MaterialTimePicker.Builder().build()
                            picker.addOnPositiveButtonClickListener {
                                viewModel.setStartTime(picker)
                            }
                            picker.show(it.supportFragmentManager, picker.toString())
                        }
                    }) {
                        Text(text = "${viewModel.startHour}:${viewModel.startMinute}")
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "结束时间", style = MaterialTheme.typography.labelMedium)
                    TextButton(onClick = {
                        activity.let {
                            val picker = MaterialTimePicker.Builder().build()
                            picker.addOnPositiveButtonClickListener {
                                viewModel.setEndTime(picker)
                            }
                            picker.show(it.supportFragmentManager, picker.toString())
                        }
                    }) {
                        Text(text = "${viewModel.endHour}:${viewModel.endMinute}")
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "检测周期", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.period.toString(), onValueChange = {
                        viewModel.period = it.toIntOrNull() ?: 180
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "检测周期(以分钟为单位)", style = MaterialTheme.typography.labelSmall)
                        })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "静态心率高报警阈值", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.alarmThreshold.toString(), onValueChange = {
                        viewModel.alarmThreshold = it.toIntOrNull() ?: 75
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "静态心率高报警阈值", style = MaterialTheme.typography.labelSmall)
                        })
                }
                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = {
                        homeViewModel.toggleHeartRateOpen()
                    }) {
                        Text(text = "取消")
                    }
                    ElevatedButton(
                        onClick = {
                            viewModel.setData()
                            homeViewModel.toggleHeartRateOpen()
                        }, colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.offset(15.dp)
                    ) {
                        Text(text = "确定")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSetHeartDialog() {
    SetHeartDialog()
}