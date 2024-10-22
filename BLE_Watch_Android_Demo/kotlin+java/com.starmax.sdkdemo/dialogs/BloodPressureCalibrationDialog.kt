package com.starmax.sdkdemo.dialogs

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.starmax.sdkdemo.viewmodel.BloodPressureCalibrationViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.math.min

@Composable
fun BloodPressureCalibrationDialog() {
    val viewModel: BloodPressureCalibrationViewModel = koinViewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    BloodPressureCalibrationDialogView(homeViewModel = homeViewModel,viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodPressureCalibrationDialogView(homeViewModel: HomeViewModel, viewModel: BloodPressureCalibrationViewModel) {
    LaunchedEffect(Unit){
        viewModel.getFromBle()
    }
    val context = LocalContext.current
    val activity = if(context is AppCompatActivity) context else null



    Dialog(
        onDismissRequest = { homeViewModel.toggleBloodPressureCalibrationDialog() }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "血压校准",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                Text(
                    text = "日期:${viewModel.year}-${viewModel.month}-${viewModel.day},状态:${viewModel.label}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(15.dp)
                )
                listOf(
                    viewModel.value0,
                    viewModel.value1,
                    viewModel.value2,
                    viewModel.value3,
                    viewModel.value4
                ).forEachIndexed {
                                 index, calibrationValue ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = when(index){
                            0 -> "第一次"
                            1 -> "第二次"
                            2 -> "第三次"
                            3 -> "第四次"
                            4 -> "第五次"
                            else -> ""
                        }, style = MaterialTheme.typography.labelMedium)
                        Text(text = "${String.format("%02d",calibrationValue.hour)}:${String.format("%02d",calibrationValue.minute)}", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.width(10.dp))
                        OutlinedTextField(value = calibrationValue.data1.toString(),shape = RoundedCornerShape(10.dp), onValueChange = {
                            val newNumber = min(it.toIntOrNull() ?: 0,250)
                            when(index){
                                0 -> viewModel.value0 = viewModel.value0.copy(data1 = newNumber)
                                1 -> viewModel.value1 = viewModel.value1.copy(data1 = newNumber)
                                2 -> viewModel.value2 = viewModel.value2.copy(data1 = newNumber)
                                3 -> viewModel.value3 = viewModel.value3.copy(data1 = newNumber)
                                4 -> viewModel.value4 = viewModel.value4.copy(data1 = newNumber)
                                else -> {}
                            }
                        },
                            maxLines = 1,
                            textStyle = MaterialTheme.typography.labelSmall,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier
                                .weight(0.3f)
                                .height(55.dp),
                            singleLine = true,
                            label = {
                                Text(text = "收缩压",style = MaterialTheme.typography.labelSmall)
                            })
                        Spacer(modifier = Modifier.width(10.dp))
                        OutlinedTextField(value = calibrationValue.data2.toString(),shape = RoundedCornerShape(10.dp), onValueChange = {
                            val newNumber = min(it.toIntOrNull() ?: 0,250)
                            when(index){
                                0 -> viewModel.value0 = viewModel.value0.copy(data2 = newNumber)
                                1 -> viewModel.value1 = viewModel.value1.copy(data2 = newNumber)
                                2 -> viewModel.value2 = viewModel.value2.copy(data2 = newNumber)
                                3 -> viewModel.value3 = viewModel.value3.copy(data2 = newNumber)
                                4 -> viewModel.value4 = viewModel.value4.copy(data2 = newNumber)
                                else -> {}
                            }
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
                                Text(text = "舒张压",style = MaterialTheme.typography.labelSmall)
                            })
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Divider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    OutlinedButton(
                        shape = RoundedCornerShape(10.dp),
                        onClick = {
                            viewModel.clearCalibration()
                        }
                    ) {
                        Text(text = "清除", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        shape = RoundedCornerShape(10.dp),
                        onClick = {
                            viewModel.getFromBle()
                        }) {
                        Text(text = "读取", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        shape = RoundedCornerShape(10.dp),
                        onClick = {
                            viewModel.sendToBle()
                        }) {
                        Text(text = "发送", style = MaterialTheme.typography.labelSmall)
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
fun PreviewBloodPressureCalibrationDialog() {
    BloodPressureCalibrationDialogView(viewModel = BloodPressureCalibrationViewModel(), homeViewModel = HomeViewModel())
}