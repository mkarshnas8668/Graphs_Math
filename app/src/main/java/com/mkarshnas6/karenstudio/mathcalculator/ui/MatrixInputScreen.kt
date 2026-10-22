
package com.mkarshnas6.karenstudio.mathcalculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mkarshnas6.karenstudio.mathcalculator.GraphViewModel
import com.mkarshnas6.karenstudio.mathcalculator.model.GraphUiState

fun vertexLabel(index: Int): String {
    val letter = ('A' + (index % 26)).toString()
    val cycle = index / 26
    return if (cycle == 0) letter else "$letter$cycle"
}

@Composable
private fun VertexSelector(
    title: String,
    selectedIndex: Int?,
    count: Int,
    onSelect: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = selectedIndex?.let { vertexLabel(it) } ?: "انتخاب نشده"
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("انتخاب نشده") },
                    onClick = {
                        onSelect(null)
                        expanded = false
                    }
                )

                (0 until count).forEach { index ->
                    DropdownMenuItem(
                        text = { Text(vertexLabel(index)) },
                        onClick = {
                            onSelect(index)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MatrixInputScreen(
    state: GraphUiState,
    viewModel: GraphViewModel
) {
    val n = state.matrix.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "ماتریس مجاورت گراف",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = "عدد وزن یال را وارد کنید یا خالی بگذارید. حداقل ۲×۲",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Button(
                onClick = { viewModel.addSize() }
            ) {
                Text("افزودن رأس")
            }

            OutlinedButton(
                onClick = { viewModel.removeSize() },
                enabled = n > 2
            ) {
                Text("حذف رأس")
            }
        }

        val hScroll = rememberScrollState()

        Column(
            modifier = Modifier.horizontalScroll(hScroll)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(40.dp))

                for (c in 0 until n) {
                    Box(
                        modifier = Modifier.size(56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = vertexLabel(c),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            state.matrix.forEachIndexed { rowIndex, row ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = vertexLabel(rowIndex),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    row.forEachIndexed { colIndex, cell ->
                        BasicTextField(
                            value = cell,
                            onValueChange = {
                                viewModel.updateCell(rowIndex, colIndex, it)
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline
                                )
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(4.dp),
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            singleLine = true
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "انتخاب مبدأ و مقصد کوتاه‌ترین مسیر",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                VertexSelector(
                    title = "مبدأ",
                    selectedIndex = state.selectedSource,
                    count = n,
                    onSelect = viewModel::setSelectedSource
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                VertexSelector(
                    title = "مقصد",
                    selectedIndex = state.selectedDestination,
                    count = n,
                    onSelect = viewModel::setSelectedDestination
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "انتخاب مبدأ و مقصد برای محاسبه کوتاه‌ترین مسیر استفاده می‌شود. کوتاه‌ترین مسیر بر اساس مجموع وزن یال‌ها محاسبه می‌شود.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (state.error != null) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Button(
            onClick = { viewModel.calculatePaths() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("محاسبه مسیرها و کوتاه‌ترین مسیر")
            }
        }
    }
}
