package com.mkarshnas6.karenstudio.mathcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.mkarshnas6.karenstudio.mathcalculator.ui.MatrixInputScreen
import com.mkarshnas6.karenstudio.mathcalculator.ui.ResultsScreen
import com.mkarshnas6.karenstudio.mathcalculator.ui.theme.MathCalculatorTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GraphViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MathCalculatorTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val state by viewModel.uiState.collectAsState()
                    if (state.showResults) {
                        ResultsScreen(state = state, viewModel = viewModel)
                    } else {
                        MatrixInputScreen(state = state, viewModel = viewModel)
                    }
                }
            }
        }
    }
}