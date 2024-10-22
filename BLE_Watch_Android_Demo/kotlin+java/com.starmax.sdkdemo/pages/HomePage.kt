package com.starmax.sdkdemo.pages

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.starmax.net.NetApi
import com.starmax.net.NetChannel
import com.starmax.sdkdemo.dialogs.*
import com.starmax.sdkdemo.ui.theme.AppTheme
import com.starmax.sdkdemo.viewmodel.BleState
import com.starmax.sdkdemo.viewmodel.BleViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import com.starmax.sdkdemo.viewmodel.OtaViewModel
import com.starmax.sdkdemo.viewmodel.SetNetModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUnitApi::class)
@Composable
fun HomePage(navController: NavController) {
    val netViewModel: SetNetModel = koinViewModel()
    val bleViewModel: BleViewModel by lazyKoinViewModel()
    val otaViewModel: OtaViewModel by lazyKoinViewModel()
    val viewModel: HomeViewModel by lazyKoinViewModel()
    val scope = rememberCoroutineScope()
    val snackbarHostState = SnackbarHostState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    otaViewModel.otaMessage.observeForever { message ->
        scope.launch {
            if (message != "") {
                bleViewModel.bleMessage.value = message
            }
        }
    }

    AppTheme {
        if (viewModel.openNetDialog) {
            SetNetDialog()
        }
        if (viewModel.openVolumeDialog) {
            VolumeDialog()
        }
        if (viewModel.openNfcDialog) {
            NfcCardDialog()
        }
        if (viewModel.openStateDialog) {
            SetStateDialog()
        }
        if (viewModel.openCameraDialog) {
            CameraControlDialog()
        }
        if(viewModel.openCustomOnOffDialog){
            CustomOnOffDialog()
        }
        if (viewModel.openCallDialog) {
            CallControlDialog()
        }
        if (viewModel.openNotDisturbDialog) {
            SetNotDisturbDialog()
        }
        if (viewModel.openUserInfoDialog) {
            SetUserInfoDialog()
        }
        if (viewModel.openGoalsDialog) {
            SetGoalsDialog()
        }
        if (viewModel.openHealthOpenDialog) {
            SetHealthOpenDialog()
        }
        if (viewModel.openHeartRateDialog) {
            SetHeartDialog()
        }
        if (viewModel.openRealTimeDataDialog) {
            SetRealTimeDataOpenDialog()
        }
        if (viewModel.openRealTimeMeasureDialog) {
            SetRealTimeDataMeasureDialog()
        }
        if (viewModel.openContactDialog) {
            SetContactDialog()
        }
        if (viewModel.openSosDialog) {
            SetSosDialog()
        }
        if (viewModel.openAppDialog) {
            AppDialog()
        }
        if (viewModel.openWorldClockDialog) {
            WorldClockDialog()
        }
        if (viewModel.openPasswordDialog) {
            PasswordDialog()
        }
        if(viewModel.openFemaleHealthDialog){
            FemaleHealthDialog()
        }
        if(viewModel.openBloodSugarCalibrationDialog){
            BloodSugarCalibrationDialog()
        }
        if(viewModel.openBloodPressureCalibrationDialog){
            BloodPressureCalibrationDialog()
        }

        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState
                )
            },
            topBar = {
                SmallTopAppBar(
                    title = {
                        Text(text = "蓝牙SDKdemo")
                    }, actions = {
                        TextButton(onClick = {
                            viewModel.toScan(navController)
                        }) {
                            Text(
                                text = "绑定设备",
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.TwoTone.SmartButton, contentDescription = "指令列表")
                        }

                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                )
            },
            content = { innerPadding ->
                LazyColumn(
                    contentPadding = innerPadding,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        val bleDevice = bleViewModel.bleDevice

                        if (bleDevice?.get() == null) {
                            ListItem(
                                headlineText = { Text(text = "未连接") },
                                leadingContent = {
                                    Icon(
                                        Icons.TwoTone.Bluetooth,
                                        contentDescription = ""
                                    )
                                },
                                trailingContent = {
                                    Icon(
                                        Icons.TwoTone.SignalCellularOff,
                                        contentDescription = ""
                                    )
                                }
                            )
                        } else {
                            ListItem(
                                headlineText = { Text(text = bleViewModel.getDeviceName()) },
                                overlineText = { Text(text = bleDevice.get()!!.mac) },
                                leadingContent = {
                                    Icon(
                                        Icons.TwoTone.Bluetooth,
                                        contentDescription = ""
                                    )
                                },
                                trailingContent = { Text(text = bleViewModel.bleStateLabel) }
                            )
                        }
                    }
                    item {
                        Divider()
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .padding(15.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                modifier = Modifier
                                    .weight(1f),
                                text = (if (netViewModel.server == NetApi.Server) "正式" else "测试")
                                        + "服务器,"
                                        + (if (netViewModel.channel == NetChannel.Release) "Release" else "Beta")
                                        + "渠道",
                                fontSize = TextUnit(12F, TextUnitType.Sp)
                            )
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .padding(15.dp)
                                .fillMaxWidth()
                        ) {
                            bleViewModel.bleMessage.value.let {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = it,
                                    fontSize = TextUnit(12F, TextUnitType.Sp)
                                )
                            }
                        }
                    }
                    item {
                        Divider()
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .padding(15.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { bleViewModel.copy() }) {
                                Text("复制结果")
                            }
                        }

                    }
                    item {
                        Row(
                            modifier = Modifier
                                .padding(15.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(15.dp)
                                    .weight(1f),
                                text = bleViewModel.bleResponseLabel.value + "\n"+bleViewModel.bleResponse.value+ "\n原始数据：\n" + bleViewModel.originData.value
                            )
                        }
                    }
                }
            }
        )
        HomeDrawer(drawerState)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomePage() {
    val navController = rememberNavController()
    HomePage(navController)
}