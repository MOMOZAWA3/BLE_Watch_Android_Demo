package com.starmax.sdkdemo.dialogs

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.starmax.sdkdemo.pages.lazyKoinViewModel
import com.starmax.sdkdemo.viewmodel.BloodSugarCalibrationViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.math.min

@Composable
fun BloodSugarCalibrationDialog() {
    val viewModel: BloodSugarCalibrationViewModel = koinViewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    BloodSugarCalibrationDialogView(homeViewModel = homeViewModel,viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodSugarCalibrationDialogView(homeViewModel: HomeViewModel, viewModel: BloodSugarCalibrationViewModel) {
    LaunchedEffect(Unit){
        viewModel.getFromBle()
    }
    val context = LocalContext.current
    val activity = if(context is AppCompatActivity) context else null

    Dialog(
        onDismissRequest = { homeViewModel.toggleBloodSugarCalibrationDialog() }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "血糖校准",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                Text(
                    text = "日期:${viewModel.year}-${viewModel.month}-${viewModel.day},状态:${viewModel.label}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(value = viewModel.minValue.data2.toString(), onValueChange = {
                        val newNumber = min(it.toIntOrNull() ?: 0,250)
                        viewModel.minValue = viewModel.minValue.copy(data2 = newNumber)
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(55.dp),
                        singleLine = true,
                        label = {
                            Row(){
                                Text(text = "空腹", style = MaterialTheme.typography.labelMedium)
                                Text(text = "${String.format("%02d",viewModel.minValue.hour)}:${String.format("%02d",viewModel.minValue.minute)}", style = MaterialTheme.typography.labelMedium)
                            }
                        })
                    TextButton(
                        onClick = {
                            viewModel.sendToBle(true)
                        },
                        modifier = Modifier.offset(15.dp)
                    ) {
                        Text(text = "发送")
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(value = viewModel.maxValue.data2.toString(), onValueChange = {
                        viewModel.maxValue = viewModel.maxValue.copy(data2 = it.toIntOrNull() ?: 0)
                    },
                        textStyle = MaterialTheme.typography.labelSmall.copy(
                            lineHeight = 18.sp
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier
                            .weight(0.3f)
                            .height(55.dp),
                        singleLine = true,
                        label = {
                            Row(){
                                Text(text = "饭后", style = MaterialTheme.typography.labelMedium)
                                Text(text = "${String.format("%02d",viewModel.maxValue.hour)}:${String.format("%02d",viewModel.maxValue.minute)}", style = MaterialTheme.typography.labelMedium)
                            }
                        })
                    TextButton(
                        onClick = {
                            viewModel.sendToBle(false)
                        },
                        modifier = Modifier.offset(15.dp)
                    ) {
                        Text(text = "发送")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Divider()
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    OutlinedButton(
                        shape = RoundedCornerShape(10.dp),
                        onClick = {
                        homeViewModel.toggleBloodSugarCalibrationDialog()
                    }) {
                        Text(text = "取消", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        shape = RoundedCornerShape(10.dp),
                        onClick = {
                            viewModel.clearCalibration()
                        },
                    ) {
                        Text(text = "清除", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        shape = RoundedCornerShape(10.dp),
                        onClick = {
                            viewModel.getFromBle()
                        },
                    ) {
                        Text(text = "读取", style = MaterialTheme.typography.labelSmall)
                    }


                    ElevatedButton(
                        shape = RoundedCornerShape(10.dp),
                        elevation = ButtonDefaults.elevatedButtonElevation(
                            defaultElevation = 0.dp
                        ),
                        onClick = {
                            viewModel.startCalibration()
                        }, colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                    ) {
                        Text(text = "校准", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBloodSugarCalibrationDialog() {
    BloodSugarCalibrationDialogView(viewModel = BloodSugarCalibrationViewModel(), homeViewModel = HomeViewModel())
}