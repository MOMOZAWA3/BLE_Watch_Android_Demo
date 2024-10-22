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
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import com.starmax.sdkdemo.viewmodel.SetNotDisturbViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SetNotDisturbDialog() {
    val activity = LocalContext.current as AppCompatActivity
    val viewModel: SetNotDisturbViewModel = koinViewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    LaunchedEffect(Unit) {
        viewModel.getData()
    }

    Dialog(
        onDismissRequest = { homeViewModel.toggleNotDisturbOpen() }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "设置勿扰模式",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "开关", style = MaterialTheme.typography.labelMedium)
                    Switch(checked = viewModel.onOff, onCheckedChange = {
                        viewModel.onOff = it
                    })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "是否全天勿扰", style = MaterialTheme.typography.labelMedium)
                    Switch(checked = viewModel.allDayOnOff, onCheckedChange = {
                        viewModel.allDayOnOff = it
                    })
                }
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

                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = {
                        homeViewModel.toggleNotDisturbOpen()
                    }) {
                        Text(text = "取消")
                    }
                    ElevatedButton(
                        onClick = {
                            viewModel.setData()
                            homeViewModel.toggleNotDisturbOpen()
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
fun PreviewSetNotDisturbDialog() {
    SetNotDisturbDialog()
}