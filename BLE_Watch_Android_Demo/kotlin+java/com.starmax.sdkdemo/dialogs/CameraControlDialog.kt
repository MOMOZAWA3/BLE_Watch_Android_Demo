package com.starmax.sdkdemo.dialogs

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.CheckBox
import androidx.compose.material.icons.twotone.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.starmax.bluetoothsdk.data.CameraControlType
import com.starmax.sdkdemo.pages.lazyKoinViewModel
import com.starmax.sdkdemo.viewmodel.BleViewModel
import com.starmax.sdkdemo.viewmodel.CallControlViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import com.starmax.sdkdemo.viewmodel.CameraControlViewModel

@Composable
fun CameraControlDialog() {
    val bleViewModel : BleViewModel by lazyKoinViewModel()
    val viewModel: CameraControlViewModel = viewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    CameraControlDialogView(bleViewModel = bleViewModel, homeViewModel = homeViewModel, viewModel = viewModel)
}
@Composable
fun CameraControlDialogView(homeViewModel: HomeViewModel,bleViewModel: BleViewModel,viewModel: CameraControlViewModel) {

    Dialog(
        onDismissRequest = { homeViewModel.toggleCamera() }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "拍照控制",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconToggleButton(checked = viewModel.cameraControlType == CameraControlType.CameraIn, onCheckedChange = { it ->
                        viewModel.cameraControlType = CameraControlType.CameraIn
                    }) {
                        Icon(if(viewModel.cameraControlType == CameraControlType.CameraIn) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank, "进入拍照界面")
                    }
                    Text(text = "进入拍照界面", style = MaterialTheme.typography.labelSmall)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconToggleButton(checked = viewModel.cameraControlType == CameraControlType.CameraExit, onCheckedChange = { it ->
                        viewModel.cameraControlType = CameraControlType.CameraExit
                    }) {
                        Icon(if(viewModel.cameraControlType == CameraControlType.CameraExit) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank, "退出拍照界面")
                    }
                    Text(text = "退出拍照界面", style = MaterialTheme.typography.labelSmall)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconToggleButton(checked = viewModel.cameraControlType == CameraControlType.TakePhoto, onCheckedChange = { it ->
                        viewModel.cameraControlType = CameraControlType.TakePhoto
                    }) {
                        Icon(if(viewModel.cameraControlType == CameraControlType.TakePhoto) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank, "app点击拍照")
                    }
                    Text(text = "app点击拍照", style = MaterialTheme.typography.labelSmall)
                }
                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = {
                        homeViewModel.toggleCamera()
                    }) {
                        Text(text = "取消")
                    }
                    ElevatedButton(
                        onClick = {
                            viewModel.send()
                            homeViewModel.toggleCamera()
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
fun PreviewCameraControlDialog() {
    CameraControlDialogView(viewModel = CameraControlViewModel(), bleViewModel = BleViewModel(), homeViewModel = HomeViewModel())
}