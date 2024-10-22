package com.starmax.sdkdemo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.starmax.sdkdemo.NavPage
import org.koin.core.component.KoinComponent

class HomeViewModel(

) : ViewModel() , KoinComponent{
    var openNetDialog by mutableStateOf(false)
        private set

    var openStateDialog by mutableStateOf(false)
        private set

    var openVolumeDialog by mutableStateOf(false)
        private set

    var openNfcDialog by mutableStateOf(false)
        private set

    var openCameraDialog by mutableStateOf(false)
        private set

    var openCustomOnOffDialog by mutableStateOf(false)
        private set

    var openCallDialog by mutableStateOf(false)
        private set

    var openUserInfoDialog by mutableStateOf(false)
        private set

    var openGoalsDialog by mutableStateOf(false)
        private set

    var openHealthOpenDialog by mutableStateOf(false)
        private set

    var openHeartRateDialog by mutableStateOf(false)
        private set

    var openRealTimeDataDialog by mutableStateOf(false)
        private set

    var openRealTimeMeasureDialog by mutableStateOf(false)
        private set

    var openContactDialog by mutableStateOf(false)
        private set

    var openSosDialog by mutableStateOf(false)
        private set

    var openNotDisturbDialog by mutableStateOf(false)
        private set

    var openAppDialog by mutableStateOf(false)
        private set

    var openWorldClockDialog by mutableStateOf(false)
        private set

    var openPasswordDialog by mutableStateOf(false)
        private set

    var openFemaleHealthDialog by mutableStateOf(false)
        private set

    var openBloodSugarCalibrationDialog by mutableStateOf(false)
        private set

    var openBloodPressureCalibrationDialog by mutableStateOf(false)
        private set

    fun toggleSetNet(){
        openNetDialog = !openNetDialog
    }

    fun toggleSetState(){
        openStateDialog = !openStateDialog
    }

    fun toggleVolume(){
        openVolumeDialog = !openVolumeDialog
    }

    fun toggleNfcDialog(){
        openNfcDialog = !openNfcDialog
    }

    fun toggleCamera(){
        openCameraDialog = !openCameraDialog
    }

    fun toggleCustomDialog(){
        openCustomOnOffDialog = !openCustomOnOffDialog
    }

    fun toggleCall(){
        openCallDialog = !openCallDialog
    }

    fun toggleUserInfo(){
        openUserInfoDialog = !openUserInfoDialog
    }

    fun toggleGoals(){
        openGoalsDialog = !openGoalsDialog
    }

    fun toggleHealthOpen(){
        openHealthOpenDialog = !openHealthOpenDialog
    }

    fun toggleHeartRateOpen(){
        openHeartRateDialog = !openHeartRateDialog
    }

    fun toggleRealTimeDataOpen(){
        openRealTimeDataDialog = !openRealTimeDataDialog
    }

    fun toggleRealTimeMeasureOpen(){
        openRealTimeMeasureDialog = !openRealTimeMeasureDialog
    }

    fun toggleContactOpen(){
        openContactDialog = !openContactDialog
    }

    fun toggleSosOpen(){
        openSosDialog = !openSosDialog
    }

    fun toggleNotDisturbOpen(){
        openNotDisturbDialog = !openNotDisturbDialog
    }

    fun toggleAppOpen(){
        openAppDialog = !openAppDialog
    }

    fun toggleWorldClockOpen(){
        openWorldClockDialog = !openWorldClockDialog
    }

    fun togglePasswordOpen(){
        openPasswordDialog = !openPasswordDialog
    }

    fun toggleFemaleHealthOpen(){
        openFemaleHealthDialog = !openFemaleHealthDialog
    }

    fun toggleBloodSugarCalibrationDialog(){
        openBloodSugarCalibrationDialog = !openBloodSugarCalibrationDialog
    }

    fun toggleBloodPressureCalibrationDialog(){
        openBloodPressureCalibrationDialog = !openBloodPressureCalibrationDialog
    }

    fun toScan(navController: NavController) {
        navController.navigate(NavPage.ScanPage.name)
    }
}