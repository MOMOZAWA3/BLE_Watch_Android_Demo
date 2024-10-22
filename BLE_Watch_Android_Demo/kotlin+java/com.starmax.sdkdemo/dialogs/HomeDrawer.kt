package com.starmax.sdkdemo.dialogs

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.starmax.sdkdemo.pages.lazyKoinViewModel
import com.starmax.sdkdemo.utils.FileUtils
import com.starmax.sdkdemo.viewmodel.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDrawer(drawerState: DrawerState) {
    val context = LocalContext.current

    val bleViewModel: BleViewModel by lazyKoinViewModel()
    val otaViewModel: OtaViewModel by lazyKoinViewModel()
    val viewModel: HomeViewModel by lazyKoinViewModel()
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch {
            drawerState.close()
        }
    }

    HomeDrawerView(context = context, drawerState = drawerState, viewModel = viewModel, bleViewModel = bleViewModel, otaViewModel = otaViewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDrawerView(context: Context,drawerState: DrawerState,viewModel: HomeViewModel,bleViewModel: BleViewModel,otaViewModel: OtaViewModel) {
    val scope = rememberCoroutineScope()
    val activity = if(context is AppCompatActivity) context else null
    val selectBinLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            bleViewModel.binUri = uri
            bleViewModel.sendCustomDial(context)
        }

    val selectOtaLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            if(uri != null){
                otaViewModel.getFromPath(bleViewModel.bleDevice!!.get()!!, uri)
            }
        }

    val selectUiLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            if(uri != null){
                bleViewModel.sendUiLocal(context,uri)
            }

        }

    val selectImageLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            bleViewModel.imageUri = uri
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            selectBinLauncher.launch(intent)
        }

    val selectDialLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            bleViewModel.binUri = uri
            bleViewModel.sendDialLocal(context)
        }

    val selectLogoLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            bleViewModel.binUri = uri
            bleViewModel.sendLogoLocal(context)
        }

    val selectGts7FirmwareLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
        val uri = it.data?.data
        bleViewModel.binUri = uri
        bleViewModel.sendGts7FirmwareLocal(context)
    }

    val selectGts7CrcLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
        val uri = it.data?.data
        bleViewModel.binUri = uri
        bleViewModel.sendGts7CrcLocal(context)
    }

    DismissibleNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DismissibleDrawerSheet(
                modifier = Modifier
                    .width(200.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                IconButton(onClick = {
                    scope.launch {
                        drawerState.close()
                    }
                }) {
                    Icon(Icons.Filled.Close, contentDescription = "关闭")
                }
                Divider()
                NavigationDrawerItem(label = { Text(text = "设置服务器") }, onClick = {
                    viewModel.toggleSetNet()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "保持前台运行") }, onClick = {
                    bleViewModel.bindService()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "断开前台运行") }, onClick = {
                    bleViewModel.unbindService()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "音量") }, onClick = {
                    viewModel.toggleVolume()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "配对指令") }, onClick = {
                    bleViewModel.pair()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "获取NFC卡片信息") }, onClick = {
                    bleViewModel.getNfcCardInfo()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "设置NFC卡片") }, onClick = {
                    viewModel.toggleNfcDialog()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "自定义开关") }, onClick = {
                    viewModel.toggleCustomDialog()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "获取BT状态") }, onClick = {
                    bleViewModel.getBtStatus()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "配对设备") }, onClick = {
                    bleViewModel.bindDevice()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "设置状态") }, onClick = {
                    viewModel.toggleSetState()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "查找设备") }, onClick = {
                    bleViewModel.findDevice(true)
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "停止查找") }, onClick = {
                    bleViewModel.findDevice(false)
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "拍照控制") }, onClick = {
                    viewModel.toggleCamera()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "电话控制") }, onClick = {
                    viewModel.toggleCall()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "获取电量") }, onClick = {
                    bleViewModel.getPower()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "读取信号强度") }, onClick = {
                    bleViewModel.getRssi()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "连接OTA蓝牙") }, onClick = {
                    if (bleViewModel.bleDevice?.get() != null) {
                        otaViewModel.connect(bleViewModel.bleDevice?.get())
                    }
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "获取版本信息") }, onClick = {
                    bleViewModel.getVersion()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "同步时间") }, onClick = {
                    bleViewModel.setTimeOffset()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "获取时区") }, onClick = {
                    bleViewModel.getTimeOffset()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "设置时区") }, onClick = {
                    bleViewModel.setTimeOffset()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "获取夏令时") }, onClick = {
                    bleViewModel.getSummerWorldClock()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "设置夏令时") }, onClick = {
                    bleViewModel.setSummerWorldClock()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "设置用户信息") }, onClick = {
                    viewModel.toggleUserInfo()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "设置一天目标") }, onClick = {
                    viewModel.toggleGoals()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "获取当前健康数据") }, onClick = {
                    bleViewModel.getHealthDetail()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "检测开关") }, onClick = {
                    viewModel.toggleHealthOpen()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "实时数据开关") }, onClick = {
                    viewModel.toggleRealTimeDataOpen()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "打开心率开关") }, onClick = {
                    viewModel.toggleHeartRateOpen()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "设置常用联系人") }, onClick = {
                    viewModel.toggleContactOpen()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "设置紧急联系人") }, onClick = {
                    viewModel.toggleSosOpen()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "设置勿扰") }, onClick = {
                    viewModel.toggleNotDisturbOpen()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "获取闹钟") }, onClick = {
                    bleViewModel.getClock()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "设置闹钟") }, onClick = {
                    bleViewModel.setClock()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "获取久坐提醒") }, onClick = {
                    bleViewModel.getLongSit()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "设置久坐提醒") }, onClick = {
                    bleViewModel.setLongSit()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "获取喝水提醒") }, onClick = {
                    bleViewModel.getDrinkWater()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "设置喝水提醒") }, onClick = {
                    bleViewModel.setDrinkWater()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "发送/停止消息") }, onClick = {
                    bleViewModel.sendMessage()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "发送天气(4天)") }, onClick = {
                    bleViewModel.setWeather()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "读取天气(7天)") }, onClick = {
                    bleViewModel.getWeatherSeven()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "发送天气(7天)") }, onClick = {
                    bleViewModel.setWeatherSeven()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "发送音乐") }, onClick = {
                    bleViewModel.sendMusic()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "获取事件提醒") }, onClick = {
                    bleViewModel.getReminder()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "设置事件提醒") }, onClick = {
                    bleViewModel.setReminder()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "获取运动模式") }, onClick = {
                    bleViewModel.getSportMode()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "设置运动模式") }, onClick = {
                    bleViewModel.setSportMode()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "应用商店") }, onClick = {
                    viewModel.toggleAppOpen()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "世界时钟") }, onClick = {
                    viewModel.toggleWorldClockOpen()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "设置密码") }, onClick = {
                    viewModel.togglePasswordOpen()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "女性健康") }, onClick = {
                    viewModel.toggleFemaleHealthOpen()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "开启压力测量") }, onClick = {
                    bleViewModel.sendHealthMeasure(true)
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "关闭压力测量") }, onClick = {
                    bleViewModel.sendHealthMeasure(false)
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "开启心率测量") }, onClick = {
                    bleViewModel.sendHeartRateHealthMeasure(true)
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "关闭心率测量") }, onClick = {
                    bleViewModel.sendHeartRateHealthMeasure(false)
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "读取电量文件") }, onClick = {
                    bleViewModel.packageId = 0
                    bleViewModel.getDebugInfo(1)
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "读取gensor文件") }, onClick = {
                    bleViewModel.packageId = 0
                    bleViewModel.getDebugInfo(2)
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "赛维算法") }, onClick = {
                    bleViewModel.packageId = 0
                    bleViewModel.getDebugInfo(3)
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "血糖校准") }, onClick = {
                    viewModel.toggleBloodSugarCalibrationDialog()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "血压校准") }, onClick = {
                    viewModel.toggleBloodPressureCalibrationDialog()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "同步运动记录") }, onClick = {
                    bleViewModel.getSportHistory()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "同步记步睡眠记录") }, onClick = {
                    activity?.let {
                        val picker = MaterialDatePicker.Builder.datePicker().build()
                        picker.addOnPositiveButtonClickListener {
                            bleViewModel.getStepHistory(it)
                            scope.launch {
                                drawerState.close()
                            }
                        }
                        picker.show(it.supportFragmentManager, picker.toString())
                    }

                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "同步心率记录") }, onClick = {
//                    activity?.let {
//                        val picker = MaterialDatePicker.Builder.datePicker().build()
//                        picker.addOnPositiveButtonClickListener {
//                            bleViewModel.getHeartRateHistory(it)
//                            scope.launch {
//                                drawerState.close()
//                            }
//                        }
//                        picker.show(it.supportFragmentManager, picker.toString())
//                    }

                    bleViewModel.getHeartRateHistory(System.currentTimeMillis())
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "同步血压记录") }, onClick = {
                    activity?.let {
                        val picker = MaterialDatePicker.Builder.datePicker().build()
                        picker.addOnPositiveButtonClickListener {
                            bleViewModel.getBloodPressureHistory(it)
                            scope.launch {
                                drawerState.close()
                            }
                        }
                        picker.show(it.supportFragmentManager, picker.toString())
                    }
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "同步血氧记录") }, onClick = {

                    activity?.let {
                        val picker = MaterialDatePicker.Builder.datePicker().build()
                        picker.addOnPositiveButtonClickListener {
                            bleViewModel.getBloodOxygenHistory(it)
                            scope.launch {
                                drawerState.close()
                            }
                        }
                        picker.show(it.supportFragmentManager, picker.toString())
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "同步压力记录") }, onClick = {
                    activity?.let {
                        val picker = MaterialDatePicker.Builder.datePicker().build()
                        picker.addOnPositiveButtonClickListener {
                            bleViewModel.getPressureHistory(it)
                            scope.launch {
                                drawerState.close()
                            }
                        }
                        picker.show(it.supportFragmentManager, picker.toString())
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "同步梅脱记录") }, onClick = {
                    activity?.let {
                        val picker = MaterialDatePicker.Builder.datePicker().build()
                        picker.addOnPositiveButtonClickListener {
                            bleViewModel.getMetHistory(it)
                            scope.launch {
                                drawerState.close()
                            }
                        }
                        picker.show(it.supportFragmentManager, picker.toString())
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "同步温度记录") }, onClick = {
                    activity?.let {
                        val picker = MaterialDatePicker.Builder.datePicker().build()
                        picker.addOnPositiveButtonClickListener {
                            bleViewModel.getTempHistory(it)
                            scope.launch {
                                drawerState.close()
                            }
                        }
                        picker.show(it.supportFragmentManager, picker.toString())
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "同步MAI") }, onClick = {
                    activity?.let {
                        val picker = MaterialDatePicker.Builder.datePicker().build()
                        picker.addOnPositiveButtonClickListener {
                            bleViewModel.getMaiHistory(it)
                            scope.launch {
                                drawerState.close()
                            }
                        }
                        picker.show(it.supportFragmentManager, picker.toString())
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "同步血糖") }, onClick = {
                    activity?.let {
                        val picker = MaterialDatePicker.Builder.datePicker().build()
                        picker.addOnPositiveButtonClickListener {
                            bleViewModel.getBloodSugarHistory(it)
                            scope.launch {
                                drawerState.close()
                            }
                        }
                        picker.show(it.supportFragmentManager, picker.toString())
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "同步睡眠记录") }, onClick = {
                    activity?.let {
                        val picker = MaterialDatePicker.Builder.datePicker().build()
                        picker.addOnPositiveButtonClickListener {
                            bleViewModel.getSleepHistory(it)
                            scope.launch {
                                drawerState.close()
                            }
                        }
                        picker.show(it.supportFragmentManager, picker.toString())
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "同步睡眠原始数据") }, onClick = {
                    activity?.let {
                        val picker = MaterialDatePicker.Builder.datePicker().build()
                        picker.addOnPositiveButtonClickListener {
                            bleViewModel.getOriginSleepHistory(it)
                            scope.launch {
                                drawerState.close()
                            }
                        }
                        picker.show(it.supportFragmentManager, picker.toString())
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "同步步数有效日期") }, onClick = {
                    bleViewModel.getValidHistoryDates()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "同步睡眠有效日期") }, onClick = {
                    bleViewModel.getSleepValidHistoryDates()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "同步梅脱有效日期") }, onClick = {
                    bleViewModel.getMetValidHistoryDates()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "同步Mai有效日期") }, onClick = {
                    bleViewModel.getMaiValidHistoryDates()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "同步血糖有效日期") }, onClick = {
                    bleViewModel.getBloodSugarValidHistoryDates()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "同步血氧有效日期") }, onClick = {
                    bleViewModel.getBloodOxygenValidHistoryDates()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "发送ui") }, onClick = {
                    if (bleViewModel.bleDevice?.get() != null) {
                        bleViewModel.sendUi()
                    }
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "发送ui(差分升级)") }, onClick = {
                    if (bleViewModel.bleDevice?.get() != null) {
                        bleViewModel.sendUiDiff()
                    }
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "发送ui(本地)") }, onClick = {
                    if (bleViewModel.bleDevice?.get() != null) {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                        intent.type = "*/*"
                        intent.addCategory(Intent.CATEGORY_OPENABLE)
                        selectUiLauncher.launch(intent)
                    }

                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "瑞昱ota升级") }, onClick = {
                    if (bleViewModel.bleDevice?.get() != null) {
                        otaViewModel.otaType = OtaType.Real
                        otaViewModel.download(
                            bleViewModel.bleDevice!!.get()!!,
                            bleViewModel.bleModel,
                            bleViewModel.bleVersion
                        )
                    }
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "博通ota升级") }, onClick = {
                    if (bleViewModel.bleDevice?.get() != null) {
                        otaViewModel.otaType = OtaType.BK
                        otaViewModel.download(
                            bleViewModel.bleDevice!!.get()!!,
                            bleViewModel.bleModel,
                            bleViewModel.bleVersion
                        )
                    }
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "杰理升级") }, onClick = {
                    if (bleViewModel.bleDevice?.get() != null) {
                        otaViewModel.otaType = OtaType.JieLi
                        otaViewModel.download(
                            bleViewModel.bleDevice!!.get()!!,
                            bleViewModel.bleModel,
                            bleViewModel.bleVersion
                        )
                    }
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "杰理日志获取") }, onClick = {
                    if (bleViewModel.bleDevice?.get() != null) {
                        otaViewModel.onStartLog()
                    }
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "瑞昱ota升级(本地)") }, onClick = {
                    if (bleViewModel.bleDevice?.get() != null) {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                        intent.type = "*/*"
                        intent.addCategory(Intent.CATEGORY_OPENABLE)
                        otaViewModel.otaType = OtaType.Real
                        selectOtaLauncher.launch(intent)
                    }
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "博通ota升级(本地)") }, onClick = {
                    if (bleViewModel.bleDevice?.get() != null) {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                        intent.type = "*/*"
                        intent.addCategory(Intent.CATEGORY_OPENABLE)
                        otaViewModel.otaType = OtaType.BK
                        selectOtaLauncher.launch(intent)
                    }
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "杰理ota升级(本地)") }, onClick = {
                    if (bleViewModel.bleDevice?.get() != null) {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                        intent.type = "*/*"
                        intent.addCategory(Intent.CATEGORY_OPENABLE)
                        otaViewModel.otaType = OtaType.JieLi
                        selectOtaLauncher.launch(intent)
                    }
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "X03差分升级(本地)") }, onClick = {
                    if (bleViewModel.bleDevice?.get() != null) {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                        intent.type = "*/*"
                        intent.addCategory(Intent.CATEGORY_OPENABLE)
                        selectGts7FirmwareLauncher.launch(intent)
                    }
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "差分升级CRC校验(本地)") }, onClick = {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.type = "*/*"
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    selectGts7CrcLauncher.launch(intent)
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "发送自定义表盘") }, onClick = {
                    if (bleViewModel.bleDevice?.get() != null) {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                        intent.type = "image/*"
                        intent.addCategory(Intent.CATEGORY_OPENABLE)
                        selectImageLauncher.launch(intent)
                    }
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "发送本地表盘") }, onClick = {
                    if (bleViewModel.bleDevice?.get() != null) {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                        intent.type = "*/*"
                        intent.addCategory(Intent.CATEGORY_OPENABLE)
                        selectDialLauncher.launch(intent)
                    }
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "发送本地logo") }, onClick = {
                    if (bleViewModel.bleDevice?.get() != null) {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                        intent.type = "*/*"
                        intent.addCategory(Intent.CATEGORY_OPENABLE)
                        selectLogoLauncher.launch(intent)
                    }
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "清除logo") }, onClick = {
                    if (bleViewModel.bleDevice?.get() != null) {
                        bleViewModel.clearLogo()
                    }
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "获取表盘信息") }, onClick = {
                    if (bleViewModel.bleDevice?.get() != null) {
                        bleViewModel.getDialInfo()
                    }
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)

                NavigationDrawerItem(label = { Text(text = "切换表盘") }, onClick = {
                    bleViewModel.switchDial()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "恢复出厂设置") }, onClick = {
                    if (bleViewModel.bleDevice?.get() != null) {
                        bleViewModel.reset()
                    }
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "关机") }, onClick = {
                    if (bleViewModel.bleDevice?.get() != null) {
                        bleViewModel.close()
                    }
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "船运模式") }, onClick = {
                    if (bleViewModel.bleDevice?.get() != null) {
                        bleViewModel.shippingMode()
                    }
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
            }
        }) {}
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PreviewHomeDrawer() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val context = LocalContext.current
    HomeDrawerView(context = context, drawerState = drawerState, viewModel = HomeViewModel(), bleViewModel = BleViewModel(), otaViewModel = OtaViewModel())
}