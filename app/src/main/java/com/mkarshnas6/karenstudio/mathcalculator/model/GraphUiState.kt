package com.mkarshnas6.karenstudio.mathcalculator.model

data class PathResult(
    val length: Int,
    val paths: List<List<Int>>
)

data class ShortestPathResult(
    val path: List<Int>,
    val totalWeight: Int,
    val error: String? = null
)

data class GraphUiState(
    val matrix: List<List<String>> = List(2) {
        List(2) { "" }
    },
    val isLoading: Boolean = false,
    val error: String? = null,
    val pathResults: List<PathResult> = emptyList(),
    val adjacencyMatrix: List<List<Int>> = emptyList(),
    val showResults: Boolean = false,
    val showShortestPath: Boolean = true,
    val selectedSource: Int? = null,
    val selectedDestination: Int? = null,
    val shortestPathResult: ShortestPathResult? = null
)