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
import com.starmax.net.NetApi
import com.starmax.net.NetChannel
import com.starmax.sdkdemo.pages.lazyKoinViewModel
import com.starmax.sdkdemo.viewmodel.BleViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import com.starmax.sdkdemo.viewmodel.SetNetModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SetNetDialog() {
    val viewModel: SetNetModel = koinViewModel()
    val bleViewModel: BleViewModel by lazyKoinViewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    LaunchedEffect(Unit) {
        viewModel.getData()
    }

    Dialog(
        onDismissRequest = { homeViewModel.toggleSetNet() }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "设置服务器",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "服务器", style = MaterialTheme.typography.labelMedium)
                    IconToggleButton(checked = viewModel.server == NetApi.TestServer, onCheckedChange = { it ->
                        viewModel.setServerData(NetApi.TestServer)
                    }) {
                        Icon(
                            if (viewModel.server == NetApi.TestServer) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "测试"
                        )
                    }
                    Text(text = "测试", style = MaterialTheme.typography.labelSmall)

                    IconToggleButton(checked = viewModel.server == NetApi.Server, onCheckedChange = { it ->
                        viewModel.setServerData(NetApi.Server)
                    }) {
                        Icon(
                            if (viewModel.server == NetApi.Server) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "正式"
                        )
                    }
                    Text(text = "正式", style = MaterialTheme.typography.labelSmall)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "渠道", style = MaterialTheme.typography.labelMedium)
                    IconToggleButton(checked = viewModel.channel == NetChannel.Beta, onCheckedChange = { it ->
                        viewModel.setChannelData(NetChannel.Beta)
                    }) {
                        Icon(
                            if (viewModel.channel == NetChannel.Beta) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "Beta"
                        )
                    }
                    Text(text = "Beta", style = MaterialTheme.typography.labelSmall)

                    IconToggleButton(checked = viewModel.channel == NetChannel.Release, onCheckedChange = { it ->
                        viewModel.setChannelData(NetChannel.Release)
                    }) {
                        Icon(
                            if (viewModel.channel == NetChannel.Release) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "Release"
                        )
                    }
                    Text(text = "Release", style = MaterialTheme.typography.labelSmall)
                }
                Divider()
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "通知方式", style = MaterialTheme.typography.labelMedium)
                    IconToggleButton(checked = bleViewModel.tryOpenNotify.value, onCheckedChange = { it ->
                        bleViewModel.setNotify(true)
                    }) {
                        Icon(
                            if (bleViewModel.tryOpenNotify.value) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "Notify"
                        )
                    }
                    Text(text = "Notify", style = MaterialTheme.typography.labelSmall)

                    IconToggleButton(checked = !bleViewModel.tryOpenNotify.value, onCheckedChange = { it ->
                        bleViewModel.setNotify(false)
                    }) {
                        Icon(
                            if (!bleViewModel.tryOpenNotify.value) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "Indicate"
                        )
                    }
                    Text(text = "Indicate", style = MaterialTheme.typography.labelSmall)
                }
                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    ElevatedButton(
                        onClick = {
                            homeViewModel.toggleSetNet()
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
fun PreviewSetNetDialog() {
    SetNetDialog()
}