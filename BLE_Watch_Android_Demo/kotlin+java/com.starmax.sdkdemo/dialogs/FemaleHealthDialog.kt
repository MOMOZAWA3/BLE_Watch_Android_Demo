package com.starmax.sdkdemo.dialogs

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.CheckBox
import androidx.compose.material.icons.twotone.CheckBoxOutlineBlank
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.android.material.datepicker.MaterialDatePicker
import com.starmax.sdkdemo.pages.lazyKoinViewModel
import com.starmax.sdkdemo.viewmodel.FemaleHealthViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun FemaleHealthDialog() {
    val viewModel: FemaleHealthViewModel = koinViewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    FemaleHealthDialogView(homeViewModel = homeViewModel,viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FemaleHealthDialogView(homeViewModel: HomeViewModel, viewModel: FemaleHealthViewModel) {
    LaunchedEffect(Unit){
        viewModel.getFromBle()
    }
    val context = LocalContext.current
    val activity = if(context is AppCompatActivity) context else null

    Dialog(
        onDismissRequest = { homeViewModel.toggleFemaleHealthOpen() }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "女性健康",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "月经天数", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.numberOfDays.toString(), onValueChange = {
                        viewModel.numberOfDays = it.toIntOrNull() ?: 0
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "月经天数",style = MaterialTheme.typography.labelSmall)
                        })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "月经周期", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.cycleDays.toString(), onValueChange = {
                        viewModel.cycleDays = it.toIntOrNull() ?: 0
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "月经周期",style = MaterialTheme.typography.labelSmall)
                        })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        activity?.let {
                            val picker = MaterialDatePicker.Builder.datePicker().build()
                            picker.addOnPositiveButtonClickListener {
                                viewModel.setDate(it)
                            }
                            picker.show(it.supportFragmentManager, picker.toString())
                        }
                    }
                ) {
                    Text(text = "上次经期日期:", style = MaterialTheme.typography.labelSmall)
                    Text(text = "${viewModel.year}-${viewModel.month}-${viewModel.day}")
                }
                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = {
                        homeViewModel.toggleFemaleHealthOpen()
                    }) {
                        Text(text = "取消")
                    }
                    ElevatedButton(
                        onClick = {
                            viewModel.sendToBle()
                            homeViewModel.toggleFemaleHealthOpen()
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
fun PreviewFemaleHealthDialog() {
    FemaleHealthDialogView(viewModel = FemaleHealthViewModel(), homeViewModel = HomeViewModel())
}