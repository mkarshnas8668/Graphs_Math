package com.mkarshnas6.karenstudio.mathcalculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkarshnas6.karenstudio.mathcalculator.model.GraphUiState
import com.mkarshnas6.karenstudio.mathcalculator.model.PathResult
import com.mkarshnas6.karenstudio.mathcalculator.model.ShortestPathResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GraphViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GraphUiState())
    val uiState: StateFlow<GraphUiState> = _uiState.asStateFlow()

    fun updateCell(row: Int, col: Int, value: String) {
        _uiState.update { state ->
            val newMatrix = state.matrix.mapIndexed { r, rowList ->
                if (r == row) {
                    rowList.mapIndexed { c, cell ->
                        if (c == col) {
                            value.filter { it.isDigit() || it == '-' }
                                .take(6)
                        } else {
                            cell
                        }
                    }
                } else {
                    rowList
                }
            }

            state.copy(
                matrix = newMatrix,
                error = null,
                shortestPathResult = null,
                selectedDestination = null,
                pathResults = emptyList(),
                adjacencyMatrix = emptyList()
            )
        }
    }

    fun addSize() {
        _uiState.update { state ->
            val size = state.matrix.size
            val newRow = List(size + 1) { "" }

            state.copy(
                matrix = state.matrix.map { it + "" } + listOf(newRow),
                error = null,
                selectedDestination = null,
                shortestPathResult = null,
                pathResults = emptyList(),
                adjacencyMatrix = emptyList()
            )
        }
    }

    fun removeSize() {
        _uiState.update { state ->
            if (state.matrix.size <= 2) {
                return@update state
            }

            val newMatrix = state.matrix.dropLast(1).map {
                it.dropLast(1)
            }

            state.copy(
                matrix = newMatrix,
                selectedSource = state.selectedSource?.coerceAtMost(newMatrix.size - 1),
                selectedDestination = null,
                error = null,
                shortestPathResult = null,
                pathResults = emptyList(),
                adjacencyMatrix = emptyList()
            )
        }
    }

    fun setShowShortestPath(enabled: Boolean) {
        _uiState.update {
            it.copy(
                showShortestPath = enabled,
                shortestPathResult = null,
                error = null
            )
        }
    }

    fun setSelectedSource(source: Int?) {
        _uiState.update { state ->
            state.copy(
                selectedSource = source,
                selectedDestination = null,
                shortestPathResult = null,
                error = null
            )
        }
    }

    fun setSelectedDestination(destination: Int?) {
        _uiState.update { state ->
            state.copy(
                selectedDestination = destination,
                shortestPathResult = null,
                error = null
            )
        }
    }

    fun getReachableDestinations(source: Int?): List<Int> {
        val state = _uiState.value
        val n = state.matrix.size

        if (source == null || source !in 0 until n) {
            return emptyList()
        }

        val adjacency = try {
            parseMatrix(state.matrix)
        } catch (_: IllegalArgumentException) {
            return emptyList()
        }

        val visited = BooleanArray(n)
        val queue = ArrayDeque<Int>()

        visited[source] = true
        queue.addLast(source)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()

            for (next in 0 until n) {
                if (adjacency[current][next] != 0 && !visited[next]) {
                    visited[next] = true
                    queue.addLast(next)
                }
            }
        }

        return (0 until n).filter { it != source && visited[it] }
    }

    fun calculatePaths() {
        val input = _uiState.value

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    showResults = false,
                    shortestPathResult = null
                )
            }

            try {
                val result = withContext(Dispatchers.Default) {
                    val adjacency = parseMatrix(input.matrix)
                    val paths = computeAllSimplePaths(adjacency)

                    val shortest = if (
                        input.showShortestPath &&
                        input.selectedSource != null &&
                        input.selectedDestination != null
                    ) {
                        calculateShortestPath(
                            adjacency,
                            input.selectedSource,
                            input.selectedDestination
                        )
                    } else {
                        null
                    }

                    Triple(paths, adjacency, shortest)
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pathResults = result.first,
                        adjacencyMatrix = result.second.map { row -> row.toList() },
                        shortestPathResult = result.third,
                        showResults = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "خطای ناشناخته در محاسبه",
                        showResults = false
                    )
                }
            }
        }
    }

    private fun parseMatrix(rawMatrix: List<List<String>>): Array<IntArray> {
        val n = rawMatrix.size

        require(n >= 2) {
            "حداقل اندازه ماتریس باید ۲×۲ باشد."
        }

        val adjacency = Array(n) { IntArray(n) }

        for (i in 0 until n) {
            require(rawMatrix[i].size == n) {
                "ماتریس باید مربعی باشد."
            }

            for (j in 0 until n) {
                val cell = rawMatrix[i][j].trim()

                adjacency[i][j] = if (cell.isEmpty()) {
                    0
                } else {
                    cell.toIntOrNull()
                        ?: throw IllegalArgumentException(
                            "مقدار نامعتبر در سطر ${i + 1} ستون ${j + 1}"
                        )
                }
            }
        }

        return adjacency
    }

    private fun computeAllSimplePaths(
        adjacency: Array<IntArray>
    ): List<PathResult> {
        val n = adjacency.size
        val results = mutableListOf<PathResult>()

        for (edgeCount in 1 until n) {
            val found = mutableListOf<List<Int>>()

            for (start in 0 until n) {
                val path = mutableListOf(start)
                val visited = BooleanArray(n)
                visited[start] = true

                dfs(
                    current = start,
                    remaining = edgeCount,
                    path = path,
                    visited = visited,
                    adjacency = adjacency,
                    found = found
                )
            }

            if (found.isNotEmpty()) {
                results.add(
                    PathResult(
                        length = edgeCount,
                        paths = found.sortedBy { it.joinToString(",") }
                    )
                )
            }
        }

        return results
    }

    private fun dfs(
        current: Int,
        remaining: Int,
        path: MutableList<Int>,
        visited: BooleanArray,
        adjacency: Array<IntArray>,
        found: MutableList<List<Int>>
    ) {
        if (remaining == 0) {
            found.add(path.toList())
            return
        }

        for (next in adjacency[current].indices) {
            if (adjacency[current][next] != 0 && !visited[next]) {
                visited[next] = true
                path.add(next)

                dfs(
                    current = next,
                    remaining = remaining - 1,
                    path = path,
                    visited = visited,
                    adjacency = adjacency,
                    found = found
                )

                path.removeAt(path.lastIndex)
                visited[next] = false
            }
        }
    }

    private fun calculateShortestPath(
        adjacency: Array<IntArray>,
        source: Int,
        destination: Int
    ): ShortestPathResult {
        val n = adjacency.size

        if (source !in 0 until n || destination !in 0 until n) {
            return ShortestPathResult(
                path = emptyList(),
                totalWeight = 0,
                error = "مبدأ یا مقصد معتبر نیست."
            )
        }

        if (source == destination) {
            return ShortestPathResult(
                path = listOf(source),
                totalWeight = 0
            )
        }

        val edges = buildList {
            for (from in 0 until n) {
                for (to in 0 until n) {
                    val weight = adjacency[from][to]
                    if (weight != 0) {
                        add(Triple(from, to, weight))
                    }
                }
            }
        }

        val infinity = Long.MAX_VALUE / 4
        val distance = LongArray(n) { infinity }
        val previous = IntArray(n) { -1 }

        distance[source] = 0L

        for (iteration in 0 until n - 1) {
            var changed = false

            for ((from, to, weight) in edges) {
                if (distance[from] == infinity) continue

                val candidate = distance[from] + weight.toLong()

                if (candidate < distance[to]) {
                    distance[to] = candidate
                    previous[to] = from
                    changed = true
                }
            }

            if (!changed) break
        }

        for ((from, to, weight) in edges) {
            if (
                distance[from] != infinity &&
                distance[from] + weight.toLong() < distance[to]
            ) {
                return ShortestPathResult(
                    path = emptyList(),
                    totalWeight = 0,
                    error = "چرخه با وزن منفی از مبدأ قابل دسترسی است؛ کوتاه‌ترین مسیر مشخص نیست."
                )
            }
        }

        if (distance[destination] == infinity) {
            return ShortestPathResult(
                path = emptyList(),
                totalWeight = 0,
                error = "هیچ مسیری از مبدأ به مقصد وجود ندارد."
            )
        }

        if (distance[destination] !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
            return ShortestPathResult(
                path = emptyList(),
                totalWeight = 0,
                error = "وزن کل مسیر خارج از محدوده عدد قابل نمایش است."
            )
        }

        val path = mutableListOf<Int>()
        var current = destination

        while (current != -1) {
            path.add(current)

            if (current == source) break

            current = previous[current]

            if (path.size > n) {
                return ShortestPathResult(
                    path = emptyList(),
                    totalWeight = 0,
                    error = "امکان بازسازی مسیر وجود ندارد."
                )
            }
        }

        if (path.lastOrNull() != source) {
            return ShortestPathResult(
                path = emptyList(),
                totalWeight = 0,
                error = "امکان بازسازی مسیر وجود ندارد."
            )
        }

        path.reverse()

        return ShortestPathResult(
            path = path,
            totalWeight = distance[destination].toInt()
        )
    }

    fun backToInput() {
        _uiState.update {
            it.copy(
                showResults = false,
                error = null
            )
        }
    }
}