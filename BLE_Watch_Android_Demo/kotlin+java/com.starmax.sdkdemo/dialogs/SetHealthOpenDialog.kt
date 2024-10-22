package com.starmax.sdkdemo.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.starmax.sdkdemo.pages.lazyKoinViewModel
import com.starmax.sdkdemo.viewmodel.HealthOpenViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SetHealthOpenDialog() {
    val viewModel: HealthOpenViewModel = koinViewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    LaunchedEffect(Unit) {
        viewModel.getData()
    }

    SetHealthOpenDialogView(homeViewModel = homeViewModel, viewModel = viewModel)
}

@Composable
fun SetHealthOpenDialogView(homeViewModel: HomeViewModel,viewModel: HealthOpenViewModel) {

    Dialog(
        onDismissRequest = { homeViewModel.toggleHealthOpen() }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "健康数据检测开关",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "心率总开关", style = MaterialTheme.typography.labelMedium)
                    Switch(checked = viewModel.heartRate, onCheckedChange = {
                        viewModel.heartRate = it
                    })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "血压总开关", style = MaterialTheme.typography.labelMedium)
                    Switch(checked = viewModel.bloodPressure, onCheckedChange = {
                        viewModel.bloodPressure = it
                    })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "血氧总开关", style = MaterialTheme.typography.labelMedium)
                    Switch(checked = viewModel.bloodOxygen, onCheckedChange = {
                        viewModel.bloodOxygen = it
                    })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "压力总开关", style = MaterialTheme.typography.labelMedium)
                    Switch(checked = viewModel.pressure, onCheckedChange = {
                        viewModel.pressure = it
                    })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "温度总开关", style = MaterialTheme.typography.labelMedium)
                    Switch(checked = viewModel.temp, onCheckedChange = {
                        viewModel.temp = it
                    })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "血糖总开关", style = MaterialTheme.typography.labelMedium)
                    Switch(checked = viewModel.bloodSugar, onCheckedChange = {
                        viewModel.bloodSugar = it
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
                        homeViewModel.toggleHealthOpen()
                    }) {
                        Text(text = "取消")
                    }
                    ElevatedButton(
                        onClick = {
                            viewModel.setData()
                            homeViewModel.toggleHealthOpen()
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
fun PreviewSetHealthOpenDialog() {
    SetHealthOpenDialogView(homeViewModel = HomeViewModel(), viewModel = HealthOpenViewModel())
}