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

internal fun subsequenceDynamicTimeWarpingOf(
    x: Array<FloatArray>,
    y: Array<FloatArray>,
    distanceFunction: (FloatArray, FloatArray) -> Float,
    localWeights: LocalWeights
): SubsequenceDynamicTimeWarpingReport {

    val accumulatedCostMatrix = accumulatedCostMatrixOf(
        x,
        y,
        distanceFunction,
        localWeights
    )

    val (endInclusive, cost) = accumulatedCostMatrix
        .last()
        .withIndex()
        .minBy { it.value } ?: throw Exception()

    val start = try {
        optimalWarpingPathOf(accumulatedCostMatrix, endInclusive)
            .first()
            .yIndex
    } catch (e: NoSuchElementException) {
        return SubsequenceDynamicTimeWarpingReport(0, 0, Float.POSITIVE_INFINITY)
    }

    return SubsequenceDynamicTimeWarpingReport(
        start - 1,
        endInclusive - 1,
        cost
    )
}

internal data class SubsequenceDynamicTimeWarpingReport(
    val start: Int,
    val endInclusive: Int,
    val cost: Float
)