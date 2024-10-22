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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.starmax.sdkdemo.pages.lazyKoinViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import com.starmax.sdkdemo.viewmodel.NfcCardViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun NfcCardDialog() {
    val viewModel: NfcCardViewModel = koinViewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    NfcDialogView(homeViewModel = homeViewModel, viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcDialogView(homeViewModel: HomeViewModel, viewModel: NfcCardViewModel) {
    AlertDialog(
        confirmButton = {
            ElevatedButton(
                onClick = {
                    viewModel.setData()
                    homeViewModel.toggleNfcDialog()
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
                homeViewModel.toggleNfcDialog()
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
        onDismissRequest = { homeViewModel.toggleNfcDialog() },
        text = {
            LazyColumn() {
                item {
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "卡片类型", style = MaterialTheme.typography.labelMedium)
                        LazyVerticalGrid(
                            modifier = Modifier.height(100.dp),
                            columns = GridCells.Fixed(3)) {
                            val cardTypeList = listOf(
                                1,
                                2,
                                3,
                                4
                            )

                            items(cardTypeList.size, key = {
                                cardTypeList[it]
                            }) { i ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconToggleButton(checked = viewModel.cardType == cardTypeList[i], onCheckedChange = { it ->
                                        viewModel.cardType = cardTypeList[i]
                                    }) {
                                        Icon(
                                            if (viewModel.cardType == cardTypeList[i]) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                                            cardTypeList[i].toString()
                                        )
                                    }

                                    Text(text = cardTypeList[i].toString(), style = MaterialTheme.typography.labelSmall)
                                }

                            }
                        }
                    }
                }
                item{
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "卡片名称", style = MaterialTheme.typography.labelSmall)
                        TextField(value = viewModel.cardName, onValueChange = {
                            viewModel.cardName = it
                        },
                            textStyle = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.offset(x = 15.dp),
                            placeholder = {
                                Text(text = "卡片名称",style = MaterialTheme.typography.labelSmall)
                            })
                    }
                }
                item {
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "操作", style = MaterialTheme.typography.labelMedium)
                        LazyVerticalGrid(
                            modifier = Modifier.height(150.dp),
                            columns = GridCells.Fixed(3)) {

                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconToggleButton(checked = viewModel.isCopy, onCheckedChange = { it ->
                                        viewModel.isCopy = true
                                    }) {
                                        Icon(
                                            if (viewModel.isCopy) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                                            "复制"
                                        )
                                    }

                                    Text(text = "复制", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconToggleButton(checked = !viewModel.isCopy, onCheckedChange = { it ->
                                        viewModel.isCopy = false
                                    }) {
                                        Icon(
                                            if (!viewModel.isCopy) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                                            "创建"
                                        )
                                    }

                                    Text(text = "创建", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        })
}

@Preview(showBackground = true)
@Composable
fun PreviewNfcCardDialog() {
    NfcDialogView(viewModel = NfcCardViewModel(), homeViewModel = HomeViewModel())
}