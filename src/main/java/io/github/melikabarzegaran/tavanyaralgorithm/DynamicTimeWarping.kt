/*
 * MIT License
 *
 * Copyright 2019 Melika Barzegaran <melika.barzegaran.hosseini@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.melikabarzegaran.tavanyaralgorithm

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

internal suspend fun dynamicTimeWarpingOf(
    x: Array<FloatArray>,
    y: Array<FloatArray>,
    distanceFunction: (FloatArray, FloatArray) -> Float,
    localWeights: LocalWeights,
    globalConstraintWidthFactor: Float,
    generalizationStrategy: GeneralizationStrategy
): Float {
    return coroutineScope {
        when (generalizationStrategy) {
            GeneralizationStrategy.DEPENDANT -> {
                dynamicTimeWarpingOf(
                    x,
                    y,
                    distanceFunction,
                    localWeights,
                    globalConstraintWidthFactor
                )
            }
            GeneralizationStrategy.INDEPENDENT -> {
                val n = x.size
                val m = y.size
                val d = x[0].size

                val costList = mutableListOf<Deferred<Float>>()
                for (j in 0 until d) {
                    val colX = Array(n) { i ->
                        floatArrayOf(x[i][j])
                    }
                    val colY = Array(m) { i ->
                        floatArrayOf(y[i][j])
                    }
                    costList += async(bgDispatcher) {
                        dynamicTimeWarpingOf(
                            colX,
                            colY,
                            distanceFunction,
                            localWeights,
                            globalConstraintWidthFactor
                        )
                    }
                }

                costList
                    .awaitAll()
                    .sum()
            }
        }
    }
}

internal fun dynamicTimeWarpingOf(
    x: Array<FloatArray>,
    y: Array<FloatArray>,
    distanceFunction: (FloatArray, FloatArray) -> Float,
    localWeights: LocalWeights,
    globalConstraintWidthFactor: Float
): Float {
    val n = x.size
    val m = y.size

    val matrix = Array(2) {
        FloatArray(m + 1) {
            Float.POSITIVE_INFINITY
        }
    }
    matrix[0][0] = 0f

    val validGlobalConstraintWidthFactor = globalConstraintWidthFactor.coerceIn(0f, 1f)
    val w = max(abs(n - m), ((max(n, m) - 1) * validGlobalConstraintWidthFactor).roundToInt())

    var previousRow = 0
    var currentRow = 1

    for (i in 1..n) {
        matrix[currentRow].fill(Float.POSITIVE_INFINITY)
        for (j in max(1, i - w)..min(m, i + w)) {
            val distance = distanceFunction(x[i - 1], y[j - 1])
            matrix[currentRow][j] = distance + minOf(
                localWeights.wh * matrix[currentRow][j - 1],
                localWeights.wv * matrix[previousRow][j],
                localWeights.wd * matrix[previousRow][j - 1]
            )
        }
        previousRow = currentRow.also { currentRow = previousRow }
    }

    return matrix[previousRow][m]
}

enum class LocalWeights(val wh: Int, val wv: Int, val wd: Int) {
    SYMMETRIC(1, 1, 1),
    ASYMMETRIC(1, 1, 2)
}

enum class GeneralizationStrategy {
    DEPENDANT,
    INDEPENDENT
}