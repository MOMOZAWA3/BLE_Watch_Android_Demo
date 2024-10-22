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
import com.starmax.bluetoothsdk.data.Contact
import com.starmax.sdkdemo.pages.lazyKoinViewModel
import com.starmax.sdkdemo.viewmodel.BleViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import com.starmax.sdkdemo.viewmodel.SetContactViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SetContactDialog() {
    val viewModel: SetContactViewModel = koinViewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    LaunchedEffect(Unit) {
        viewModel.getData()
    }

    SetContactDialogView(homeViewModel = homeViewModel,viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetContactDialogView(homeViewModel: HomeViewModel,viewModel: SetContactViewModel) {
    Dialog(
        onDismissRequest = { homeViewModel.toggleContactOpen() }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "设置常用联系人",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                Text(
                    text = "联系人1",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(15.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "姓名", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.firstName, onValueChange = {
                        viewModel.firstName = it
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "姓名",style = MaterialTheme.typography.labelSmall)
                        })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "电话", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.firstNumber, onValueChange = {
                        viewModel.firstNumber = it
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "电话",style = MaterialTheme.typography.labelSmall)
                        })
                }
                Text(
                    text = "联系人2",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(15.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "姓名", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.secondName, onValueChange = {
                        viewModel.secondName = it
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "姓名",style = MaterialTheme.typography.labelSmall)
                        })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "电话", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.secondNumber, onValueChange = {
                        viewModel.secondNumber = it
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "电话",style = MaterialTheme.typography.labelSmall)
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
                        homeViewModel.toggleContactOpen()
                    }) {
                        Text(text = "取消")
                    }
                    ElevatedButton(
                        onClick = {
                            viewModel.setData()
                            homeViewModel.toggleContactOpen()
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
fun PreviewSetContactDialog() {
    SetContactDialogView(viewModel = SetContactViewModel(), homeViewModel = HomeViewModel())
}