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

package io.github.melikabarzegaran.tavanyaralgorithm.algorithm

internal fun accumulatedCostMatrixOf(
    x: Array<FloatArray>,
    y: Array<FloatArray>,
    distanceFunction: (FloatArray, FloatArray) -> Float,
    localWeights: LocalWeights
): Array<FloatArray> {
    val n = x.size
    val m = y.size

    val matrix = Array(n + 1) { FloatArray(m + 1) }

    matrix[0][0] = 0f

    for (i in 1..n) {
        matrix[i][0] = Float.POSITIVE_INFINITY
    }

    for (j in 1..m) {
        matrix[0][j] = 0f
    }

    for (i in 1..n) {
        for (j in 1..m) {
            val distance = distanceFunction(x[i - 1], y[j - 1])
            matrix[i][j] = distance + minOf(
                localWeights.wh * matrix[i][j - 1],
                localWeights.wv * matrix[i - 1][j],
                localWeights.wd * matrix[i - 1][j - 1]
            )
        }
    }

    return matrix
}

internal fun optimalWarpingPathOf(
    accumulatedCostMatrix: Array<FloatArray>,
    endInclusive: Int
): List<WarpingPathCell> {
    val n = accumulatedCostMatrix.size - 1
    val m = accumulatedCostMatrix[0].size - 1

    var i = n
    var j = endInclusive.coerceIn(1, m)
    val optimalWarpingPath = mutableListOf<WarpingPathCell>()

    while (i > 1) {
        optimalWarpingPath += WarpingPathCell(i, j)
        when {
            j == 1 -> i--
            else -> {
                when (minOf(
                    accumulatedCostMatrix[i][j - 1],
                    accumulatedCostMatrix[i - 1][j],
                    accumulatedCostMatrix[i - 1][j - 1]
                )) {
                    accumulatedCostMatrix[i - 1][j - 1] -> {
                        i--
                        j--
                    }
                    accumulatedCostMatrix[i - 1][j] -> i--
                    accumulatedCostMatrix[i][j - 1] -> j--
                }
            }
        }
    }

    optimalWarpingPath += WarpingPathCell(i, j)

    return optimalWarpingPath.asReversed()
}

enum class LocalWeights(val wh: Int, val wv: Int, val wd: Int) {
    SYMMETRIC(1, 1, 1),
    ASYMMETRIC(1, 1, 2)
}

internal data class WarpingPathCell(val xIndex: Int, val yIndex: Int)