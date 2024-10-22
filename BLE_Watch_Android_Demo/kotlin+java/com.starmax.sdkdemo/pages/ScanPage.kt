package com.starmax.sdkdemo.pages

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.KeyboardArrowLeft
import androidx.compose.material.icons.twotone.Bluetooth
import androidx.compose.material.icons.twotone.Search
import androidx.compose.material.icons.twotone.SearchOff
import androidx.compose.material.icons.twotone.SignalCellularAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.starmax.sdkdemo.ui.theme.AppTheme
import com.starmax.sdkdemo.viewmodel.BleViewModel
import com.starmax.sdkdemo.viewmodel.ScanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanPage(navController: NavController, viewModel: ScanViewModel = viewModel()){
    DisposableEffect(Unit){
        onDispose {
            viewModel.stopScan()
        }
    }

    val bleViewModel : BleViewModel = viewModel(LocalContext.current as ComponentActivity)

    ScanPageView(navController,viewModel,bleViewModel)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanPageView(navController: NavController, viewModel: ScanViewModel = viewModel(),bleViewModel: BleViewModel) {

    AppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(text = "蓝牙搜索")
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Sharp.KeyboardArrowLeft, contentDescription = "返回")
                        }
                    },
                )
            }
        ) { innerPadding ->

            LazyColumn(
                contentPadding = innerPadding
            ) {
                item{
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(
                            horizontal = 15.dp
                        )
                    ) {
                        OutlinedTextField(value = viewModel.searchName, onValueChange = {
                            viewModel.searchName = it
                        },
                            label = {
                                Text(text = "蓝牙名称", style = MaterialTheme.typography.labelSmall)
                            },
                            textStyle = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(text = "GTS5",style = MaterialTheme.typography.labelSmall)
                            },trailingIcon = {
                                Icon(
                                    if(viewModel.isScanning) Icons.TwoTone.SearchOff else Icons.TwoTone.Search,
                                    modifier = Modifier.clickable {
                                        viewModel.startScan()
                                    },
                                    contentDescription = "search"
                                )
                            })

                    }

                }
                item{
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(
                            horizontal = 15.dp
                        )
                    ) {
                        OutlinedTextField(value = viewModel.searchMac, onValueChange = {
                            viewModel.searchMac = it
                        },
                            label = {
                                Text(text = "Mac地址", style = MaterialTheme.typography.labelSmall)
                            },
                            textStyle = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(text = "",style = MaterialTheme.typography.labelSmall)
                            },trailingIcon = {
                                Icon(
                                    if(viewModel.isScanning) Icons.TwoTone.SearchOff else Icons.TwoTone.Search,
                                    modifier = Modifier.clickable {
                                        viewModel.startScan()
                                    },
                                    contentDescription = "search"
                                )
                            })

                    }

                }
                items(viewModel.devices.size) { index ->
                    ListItem(
                        modifier = Modifier.clickable {
                            bleViewModel.connect(viewModel.devices[index])
                            navController.popBackStack()
                        },
                        headlineText = { Text(text = viewModel.getDeviceName(index)) },
                        overlineText = { Text(text = viewModel.devices[index].mac) },
                        supportingText = { (if(viewModel.broadcast.contains(viewModel.devices[index].mac)) viewModel.broadcast[viewModel.devices[index].mac] else "")?.let { Text(text = it) } },
                        leadingContent = { Icon(Icons.TwoTone.Bluetooth, contentDescription = "") },
                        trailingContent = {
                            Icon(
                                Icons.TwoTone.SignalCellularAlt,
                                contentDescription = ""
                            )
                        }
                    )
                    Divider()
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun PreviewScanPage() {
    val navController = rememberNavController()
    ScanPageView(navController,ScanViewModel(),BleViewModel())
}