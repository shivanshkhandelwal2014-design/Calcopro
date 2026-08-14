package com.example

class MathEngine {
    fun evaluate(expression: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expression.length) expression[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expression.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm() // addition
                    else if (eat('-'.code)) x -= parseTerm() // subtraction
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x *= parseFactor() // multiplication
                    else if (eat('/'.code)) x /= parseFactor() // division
                    else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor() // unary plus
                if (eat('-'.code)) return -parseFactor() // unary minus

                var x: Double
                val startPos = pos
                if (eat('('.code)) { // parentheses
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) { // numbers
                    while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                    x = expression.substring(startPos, pos).toDouble()
                } else if (ch >= 'a'.code && ch <= 'z'.code) { // functions
                    while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                    val func = expression.substring(startPos, pos)
                    x = if (func == "pi") {
                        Math.PI
                    } else if (func == "e") {
                        Math.E
                    } else {
                        val arg = parseFactor()
                        when (func) {
                            "sqrt" -> Math.sqrt(arg)
                            "sin" -> Math.sin(Math.toRadians(arg))
                            "cos" -> Math.cos(Math.toRadians(arg))
                            "tan" -> Math.tan(Math.toRadians(arg))
                            "log" -> Math.log10(arg)
                            "ln" -> Math.log(arg)
                            else -> throw RuntimeException("Unknown function: $func")
                        }
                    }
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }

                if (eat('^'.code)) x = Math.pow(x, parseFactor()) // exponentiation
                if (eat('!'.code)) { // factorial
                    x = factorial(x)
                }

                return x
            }
            
            fun factorial(n: Double): Double {
                if (n < 0 || n != Math.floor(n)) throw RuntimeException("Factorial of non-integer or negative")
                var res = 1.0
                for (i in 2..n.toInt()) {
                    res *= i
                }
                return res
            }
        }.parse()
    }
}
