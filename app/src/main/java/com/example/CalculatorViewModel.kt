package com.example

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CalculatorState(
    val expression: String = "",
    val result: String = "",
    val isError: Boolean = false,
    val history: List<Pair<String, String>> = emptyList()
)

class CalculatorViewModel : ViewModel() {
    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    private val mathEngine = MathEngine()

    fun onKeyPress(key: String) {
        when (key) {
            "AC" -> clearAll()
            "DEL" -> deleteLast()
            "=" -> evaluate(force = true)
            else -> appendKey(key)
        }
    }

    private fun clearAll() {
        _state.update { it.copy(expression = "", result = "", isError = false) }
    }

    private fun deleteLast() {
        _state.update { currentState ->
            if (currentState.expression.isNotEmpty()) {
                val newExpr = currentState.expression.dropLast(1)
                currentState.copy(expression = newExpr, isError = false)
            } else {
                currentState
            }
        }
        evaluate(force = false)
    }

    private fun appendKey(key: String) {
        _state.update { currentState ->
            currentState.copy(
                expression = currentState.expression + key,
                isError = false
            )
        }
        evaluate(force = false)
    }

    private fun evaluate(force: Boolean = false) {
        val expr = _state.value.expression
        if (expr.isEmpty()) {
            if (force) _state.update { it.copy(result = "") }
            return
        }

        // Pre-process for evaluation
        val evalExpr = expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "pi")

        try {
            val res = mathEngine.evaluate(evalExpr)
            // Format result: remove .0 if it's an integer, or handle NaN/Infinity
            val resStr = if (res.isNaN()) {
                "Error"
            } else if (res.isInfinite()) {
                "Infinity"
            } else {
                if (res == res.toLong().toDouble()) {
                    res.toLong().toString()
                } else {
                    // Limit decimals nicely to avoid floating point precision issues like 0.300000000004
                    val rounded = Math.round(res * 1000000000.0) / 1000000000.0
                    if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
                }
            }

            _state.update { 
                if (force) {
                    // On equals, move expression to history and result to expression
                    val newHistory = it.history + Pair(it.expression, resStr)
                    it.copy(
                        expression = if (resStr == "Error" || resStr == "Infinity") "" else resStr,
                        result = "",
                        isError = resStr == "Error",
                        history = newHistory.takeLast(10) // Keep last 10
                    )
                } else {
                    it.copy(result = resStr, isError = false)
                }
            }
        } catch (e: Exception) {
            if (force) {
                _state.update { it.copy(isError = true, result = "Error") }
            } else {
                // Ignore intermediate parse errors gracefully while typing
            }
        }
    }
}
