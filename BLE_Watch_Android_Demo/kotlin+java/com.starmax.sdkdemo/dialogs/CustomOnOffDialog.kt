package com.starmax.sdkdemo.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.CheckBox
import androidx.compose.material.icons.twotone.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.starmax.sdkdemo.pages.lazyKoinViewModel
import com.starmax.sdkdemo.viewmodel.BleViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import com.starmax.sdkdemo.viewmodel.CustomOnOffViewModel

@Composable
fun CustomOnOffDialog() {
    val bleViewModel : BleViewModel by lazyKoinViewModel()
    val viewModel: CustomOnOffViewModel = viewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    CustomOnOffDialogView(bleViewModel = bleViewModel, homeViewModel = homeViewModel, viewModel = viewModel)
}
@Composable
fun CustomOnOffDialogView(homeViewModel: HomeViewModel,bleViewModel: BleViewModel,viewModel: CustomOnOffViewModel) {

    Dialog(
        onDismissRequest = { homeViewModel.toggleCustomDialog() }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "自定义开关",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconToggleButton(checked = viewModel.type == 0, onCheckedChange = { it ->
                        viewModel.type = 0
                    }) {
                        Icon(if(viewModel.type == 0) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank, "蓝牙")
                    }
                    Text(text = "蓝牙", style = MaterialTheme.typography.labelSmall)
                    IconToggleButton(checked = viewModel.type == 1, onCheckedChange = { it ->
                        viewModel.type = 1
                    }) {
                        Icon(if(viewModel.type == 1) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank, "SOS")
                    }
                    Text(text = "SOS", style = MaterialTheme.typography.labelSmall)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconToggleButton(checked = viewModel.onOff, onCheckedChange = { it ->
                        viewModel.onOff = !viewModel.onOff
                    }) {
                        Icon(if(viewModel.onOff) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank, "开关")
                    }
                    Text(text = "开关", style = MaterialTheme.typography.labelSmall)
                }
                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = {
                        homeViewModel.toggleCustomDialog()
                    }) {
                        Text(text = "取消")
                    }
                    ElevatedButton(
                        onClick = {
                            viewModel.setData()
                            homeViewModel.toggleCustomDialog()
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
fun PreviewCustomOnOffDialogView() {
    CustomOnOffDialogView(viewModel = CustomOnOffViewModel(), bleViewModel = BleViewModel(), homeViewModel = HomeViewModel())
}