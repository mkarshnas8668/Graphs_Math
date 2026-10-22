package com.mkarshnas6.karenstudio.mathcalculator.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mkarshnas6.karenstudio.mathcalculator.GraphViewModel
import com.mkarshnas6.karenstudio.mathcalculator.model.GraphUiState

@Composable
fun ResultsScreen(
    state: GraphUiState,
    viewModel: GraphViewModel
) {
    BackHandler {
        viewModel.backToInput()
    }

    val n = state.adjacencyMatrix.size
    val shortest = state.shortestPathResult

    val shortestPathMatrix = List(n) { i ->
        List(n) { j ->
            val path = shortest?.path.orEmpty()

            if (
                shortest?.error == null &&
                path.zipWithNext().any { (from, to) ->
                    from == i && to == j
                } &&
                i < state.adjacencyMatrix.size &&
                j < state.adjacencyMatrix[i].size
            ) {
                state.adjacencyMatrix[i][j]
            } else {
                0
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "نتایج مسیرها",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "کوتاه‌ترین مسیر",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (shortest == null) {
            Text("کوتاه‌ترین مسیر محاسبه نشده است.")
        } else if (shortest.error != null) {
            Text(
                text = shortest.error,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Text(
                text = shortest.path.joinToString(" → ") {
                    vertexLabel(it)
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            state.selectedSource?.let { source ->
                Text(
                    text = "مبدأ: ${vertexLabel(source)}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            state.selectedDestination?.let { destination ->
                Text(
                    text = "مقصد: ${vertexLabel(destination)}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Text(
                text = "مجموع وزن مسیر: ${shortest.totalWeight}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            if (shortest.path.size > 1) {
                val steps = shortest.path.zipWithNext()

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "مراحل مسیر:",
                    fontWeight = FontWeight.SemiBold
                )

                steps.forEach { (from, to) ->
                    val weight = state.adjacencyMatrix
                        .getOrNull(from)
                        ?.getOrNull(to)

                    if (weight != null) {
                        Text(
                            text = "${vertexLabel(from)} → ${vertexLabel(to)} : وزن $weight"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "گراف کوتاه‌ترین مسیر",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            GraphCanvas(
                adjacency = shortestPathMatrix,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "ماتریس مجاورت نهایی",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        val hScroll = rememberScrollState()

        Column(
            modifier = Modifier.horizontalScroll(hScroll)
        ) {
            Row {
                Box(modifier = Modifier.size(40.dp))

                for (c in 0 until n) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline
                            )
                            .background(
                                MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = vertexLabel(c),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            state.adjacencyMatrix.forEachIndexed { r, row ->
                Row {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline
                            )
                            .background(
                                MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = vertexLabel(r),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    row.forEach { value ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline
                                )
                                .background(
                                    MaterialTheme.colorScheme.surface
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = value.toString(),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "گراف کامل",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        GraphCanvas(
            adjacency = state.adjacencyMatrix,
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        if (state.pathResults.isEmpty()) {
            Text("هیچ مسیر ساده‌ای با طول ۲ یا بیشتر یافت نشد.")
        } else {
            state.pathResults.forEach { result ->
                Text(
                    text = "مسیرهای طول ${result.length} (${result.paths.size} مسیر)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(
                        top = 12.dp,
                        bottom = 6.dp
                    )
                )

                result.paths.forEach { path ->
                    Text(
                        text = path.joinToString(" → ") {
                            vertexLabel(it)
                        },
                        modifier = Modifier.padding(
                            start = 8.dp,
                            bottom = 4.dp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                viewModel.backToInput()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("بازگشت و ویرایش ماتریس")
        }
    }
}