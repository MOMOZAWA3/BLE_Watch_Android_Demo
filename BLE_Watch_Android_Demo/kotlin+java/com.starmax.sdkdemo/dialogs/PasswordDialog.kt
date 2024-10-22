package com.starmax.sdkdemo.dialogs

import androidx.compose.foundation.layout.*
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
import com.starmax.sdkdemo.viewmodel.PasswordViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun PasswordDialog() {
    val viewModel: PasswordViewModel = koinViewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    PasswordDialogView(homeViewModel = homeViewModel,viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordDialogView(homeViewModel: HomeViewModel,viewModel: PasswordViewModel) {
    LaunchedEffect(Unit){
        viewModel.getFromBle()
    }

    Dialog(
        onDismissRequest = { homeViewModel.togglePasswordOpen() }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "设置密码",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconToggleButton(
                        checked = viewModel.isOpen,
                        onCheckedChange = { it ->
                            viewModel.isOpen = it
                        }) {
                        Icon(
                            if (viewModel.isOpen) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "开关"
                        )
                    }
                    Text(text = "密码", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.password, onValueChange = {
                        viewModel.password = it
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "密码",style = MaterialTheme.typography.labelSmall)
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
                        homeViewModel.togglePasswordOpen()
                    }) {
                        Text(text = "取消")
                    }
                    ElevatedButton(
                        onClick = {
                            viewModel.sendToBle()
                            homeViewModel.togglePasswordOpen()
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
fun PreviewPasswordDialog() {
    PasswordDialogView(viewModel = PasswordViewModel(), homeViewModel = HomeViewModel())
}