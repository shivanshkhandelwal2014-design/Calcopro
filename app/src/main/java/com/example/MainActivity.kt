package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    CalculatorScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier, viewModel: CalculatorViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        DisplayArea(
            expression = state.expression,
            result = state.result,
            isError = state.isError,
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // KeypadArea is strictly stateless and takes a method reference to avoid recomposing on every keystroke!
        KeypadArea(onKeyPress = viewModel::onKeyPress)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DisplayArea(
    expression: String,
    result: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = expression.ifEmpty { " " },
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = if (expression.length > 15) 40.sp else 56.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.End,
            lineHeight = 64.sp,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth().testTag("expression_display")
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AnimatedContent(
            targetState = result,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
            },
            label = "result_animation"
        ) { res ->
            Text(
                text = res,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                fontSize = 36.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().testTag("result_display")
            )
        }
    }
}

@Composable
fun KeypadArea(onKeyPress: (String) -> Unit) {
    val haptic = LocalHapticFeedback.current
    var isScientificExpanded by remember { mutableStateOf(false) }
    
    val advancedFuncs = remember { listOf("sin(", "cos(", "tan(", "log(", "ln(", "sqrt(", "^", "!", "π", "e") }
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFF1E1E1E),
            modifier = Modifier.clip(RoundedCornerShape(50)).clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                isScientificExpanded = !isScientificExpanded
            }
        ) {
            Text(
                text = if (isScientificExpanded) "Basic" else "Scientific",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color(0xFF4DA8DA),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    AnimatedVisibility(
        visible = isScientificExpanded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(advancedFuncs) { func ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E1E1E),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { 
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onKeyPress(func) 
                        }
                ) {
                    Text(
                        text = func.replace("(", ""),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        color = Color(0xFFA0A0A0),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    val buttons = remember {
        listOf(
            listOf("AC", "(", ")", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "−"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "DEL", "=")
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        for (row in buttons) {
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (btn in row) {
                    val isZero = btn == "0"
                    CalculatorButton(
                        text = btn,
                        modifier = if (isZero) {
                            Modifier.weight(2.1f).fillMaxHeight()
                        } else {
                            Modifier.weight(1f).aspectRatio(1f)
                        },
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onKeyPress(btn)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isAction = text in listOf("AC", "DEL")
    val isEqual = text == "="
    val isOperator = text in listOf("÷", "×", "−", "+", "(", ")")
    
    val backgroundBrush = remember(text) {
        when {
            isEqual -> Brush.linearGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
            isAction -> Brush.linearGradient(listOf(Color(0xFFE94057), Color(0xFFF27121)))
            isOperator -> Brush.linearGradient(listOf(Color(0xFF262626), Color(0xFF262626)))
            else -> Brush.linearGradient(listOf(Color(0xFF141414), Color(0xFF141414)))
        }
    }
    
    val contentColor = when {
        isEqual || isAction -> Color.White
        isOperator -> Color(0xFF4DA8DA) // Neon Blue
        else -> Color.White
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundBrush)
            .clickable(onClick = onClick)
            .testTag("btn_$text")
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 32.sp,
            fontWeight = if (isAction || isOperator || isEqual) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
