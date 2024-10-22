package com.starmax.sdkdemo.dialogs

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.CheckBox
import androidx.compose.material.icons.twotone.CheckBoxOutlineBlank
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.starmax.sdkdemo.pages.lazyKoinViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import com.starmax.sdkdemo.viewmodel.SetAppViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppDialog() {
    val viewModel: SetAppViewModel = koinViewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    AppDialogView(homeViewModel = homeViewModel,viewModel = viewModel)
}

@Composable
fun AppDialogView(homeViewModel: HomeViewModel, viewModel: SetAppViewModel) {
    LaunchedEffect(Unit){
        viewModel.getFromBle()
    }

    Dialog(
        onDismissRequest = { homeViewModel.toggleAppOpen() }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "应用商店",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                LazyVerticalGrid(columns = GridCells.Fixed(3)){
                    items(viewModel.defaultApps.keys.toList().size, key = {
                        viewModel.defaultApps.keys.toList()[it]
                    }) {
                        i ->
                        val key = viewModel.defaultApps.keys.toList()[i]

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            IconToggleButton(
                                checked = viewModel.selectedApps.contains(key),
                                onCheckedChange = { it ->
                                    val result = viewModel.selectedApps.toMutableList()
                                    if(it){
                                        result.add(key)
                                    }else{
                                        result.remove(key)
                                    }

                                    viewModel.selectedApps = result
                                }) {
                                Icon(
                                    if (viewModel.selectedApps.contains(key)) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                                    viewModel.defaultApps[key]!!
                                )
                            }
                            Text(text = viewModel.defaultApps[key]!!, style = MaterialTheme.typography.labelSmall)
                        }

                    }
                }
                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = {
                        homeViewModel.toggleAppOpen()
                    }) {
                        Text(text = "取消")
                    }
                    ElevatedButton(
                        onClick = {
                            viewModel.sendToBle()
                            homeViewModel.toggleAppOpen()
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
fun PreviewAppDialog() {
    AppDialogView(viewModel = SetAppViewModel(), homeViewModel = HomeViewModel())
}