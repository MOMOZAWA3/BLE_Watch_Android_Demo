package com.starmax.sdkdemo.dialogs

import android.media.AudioManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import com.starmax.sdkdemo.pages.lazyKoinViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import com.starmax.sdkdemo.viewmodel.VolumeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun VolumeDialog() {
    val viewModel: VolumeViewModel = koinViewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    LaunchedEffect(Unit) {
        viewModel.getVolume()
    }

    VolumeDialogView(homeViewModel = homeViewModel, viewModel = viewModel)
}

@Composable
fun VolumeDialogView(homeViewModel: HomeViewModel, viewModel: VolumeViewModel) {
    AlertDialog(
        modifier = Modifier.fillMaxHeight(1f),
        confirmButton = {
            ElevatedButton(
                onClick = {
                    viewModel.setVolume()
                    homeViewModel.toggleVolume()
                }, colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.offset(15.dp)
            ) {
                Text(text = "确定")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = {
                homeViewModel.toggleVolume()
            }) {
                Text(text = "取消")
            }
        },
        title = {
            Text(
                text = "设置声音",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(15.dp)
            )
        },
        onDismissRequest = { homeViewModel.toggleVolume() },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "流类型", style = MaterialTheme.typography.labelMedium)
                        LazyVerticalGrid(
                            modifier = Modifier.height(350.dp),
                            columns = GridCells.Fixed(3)) {
                            val streamTypeList = listOf(
                                AudioManager.STREAM_ACCESSIBILITY,
                                AudioManager.STREAM_DTMF,
                                AudioManager.STREAM_MUSIC,
                                AudioManager.STREAM_NOTIFICATION,
                                AudioManager.STREAM_RING,
                                AudioManager.STREAM_SYSTEM,
                                AudioManager.STREAM_VOICE_CALL
                            )

                            items(streamTypeList.size, key = {
                                streamTypeList[it]
                            }) { i ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconToggleButton(checked = viewModel.streamType == streamTypeList[i], onCheckedChange = { it ->
                                        viewModel.streamType = streamTypeList[i]
                                        viewModel.getVolume()
                                    }) {
                                        Icon(
                                            if (viewModel.streamType == streamTypeList[i]) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                                            streamTypeList[i].toString()
                                        )
                                    }

                                    Text(text = streamTypeList[i].toString(), style = MaterialTheme.typography.labelSmall)
                                }

                            }
                        }
                    }
                }
                item{
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "音量", style = MaterialTheme.typography.labelMedium)
                        Slider(value = viewModel.volume.toFloat(), onValueChange = {
                            viewModel.volume = it.toInt()
                            viewModel.setVolume()
                        }, steps = 5, valueRange = 0f..viewModel.maxVolume.toFloat(), modifier = Modifier.offset(x = 15.dp))
                    }
                }
            }
        })
}

@Preview(showBackground = true)
@Composable
fun PreviewVolumeDialog() {
    VolumeDialogView(viewModel = VolumeViewModel(), homeViewModel = HomeViewModel())
}