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

import kotlin.math.*

internal suspend fun subsequenceDynamicTimeWarpingOf(
    candidate: Array<FloatArray>,
    query: Array<FloatArray>,
    distanceFunction: (FloatArray, FloatArray) -> Float,
    localWeights: LocalWeights,
    globalConstraintWidthFactor: Float,
    generalizationStrategy: GeneralizationStrategy,
    downSamplingStep: Int,
    lengthToleranceFactor: Float,
    interpolationStrategy: InterpolationStrategy,
    lowerBoundRadius: Float
): SubsequenceDynamicTimeWarpingReport {

    val n = candidate.size
    val m = query.size

    val validDownSamplingStep = downSamplingStep.coerceIn(1..m)

    val validLengthToleranceFactor = lengthToleranceFactor.coerceIn(0f, 0.99f)
    val minLength = (n * (1 - validLengthToleranceFactor)).roundToInt()
    val maxLength = (n * (1 + validLengthToleranceFactor)).roundToInt()

    var bestSubsequenceStart = 0
    var bestSubsequenceEndInclusive = 0
    var bestSubsequenceCost = Float.POSITIVE_INFINITY

    var numberOfPrunedOutCalculations = 0L
    var numberOfNotPrunedOutCalculations = 0L

    for (i in 0..(m - minLength) step validDownSamplingStep) {
        for (j in (i + minLength - 1)..min(i + maxLength - 1, m - 1) step validDownSamplingStep) {

            val querySubsequence = query.sliceArray(i..j)

            val (linearInterpolatedCandidate, linearInterpolatedQuerySubsequence) = linearInterpolateOf(
                candidate,
                querySubsequence,
                interpolationStrategy
            )

            val keoghLowerBoundCost = keoghLowerBoundOf(
                linearInterpolatedCandidate,
                linearInterpolatedQuerySubsequence,
                lowerBoundRadius,
                distanceFunction
            )

            if (keoghLowerBoundCost < bestSubsequenceCost) {
                val cost = dynamicTimeWarpingOf(
                    linearInterpolatedCandidate,
                    linearInterpolatedQuerySubsequence,
                    distanceFunction,
                    localWeights,
                    globalConstraintWidthFactor,
                    generalizationStrategy
                )
                if (cost < bestSubsequenceCost) {
                    bestSubsequenceStart = i
                    bestSubsequenceEndInclusive = j
                    bestSubsequenceCost = cost
                }
                numberOfNotPrunedOutCalculations++
            } else {
                numberOfPrunedOutCalculations++
            }
        }
    }

    val bestSubsequenceLength = bestSubsequenceEndInclusive - bestSubsequenceStart + 1
    val bestSubsequenceNormalizedCost = bestSubsequenceCost / bestSubsequenceLength

    return SubsequenceDynamicTimeWarpingReport(
        bestSubsequenceStart,
        bestSubsequenceEndInclusive,
        bestSubsequenceNormalizedCost,
        numberOfPrunedOutCalculations,
        numberOfNotPrunedOutCalculations
    )
}

internal fun linearInterpolateOf(
    x: Array<FloatArray>,
    y: Array<FloatArray>,
    interpolationStrategy: InterpolationStrategy
): Pair<Array<FloatArray>, Array<FloatArray>> {
    val n = x.size
    val m = y.size

    return if (n != m) {
        when (interpolationStrategy) {
            InterpolationStrategy.TO_SMALLER -> {
                if (n < m) {
                    x to linearInterpolateOf(y, n)
                } else {
                    linearInterpolateOf(x, m) to y
                }
            }
            InterpolationStrategy.TO_BIGGER -> {
                if (n > m) {
                    x to linearInterpolateOf(y, n)
                } else {
                    linearInterpolateOf(x, m) to y
                }
            }
        }
    } else {
        x to y
    }
}

internal fun linearInterpolateOf(
    x: Array<FloatArray>,
    n: Int
): Array<FloatArray> {
    val oldN = x.size
    val newN = n.coerceAtLeast(2)
    val delta = (oldN - 1).toFloat() / (newN - 1).toFloat()
    val d = x[0].size

    return Array(newN) { i ->
        val fi = i * delta
        val absolutePart = fi.absoluteValue.toInt().coerceIn(0 until oldN)
        val nextAbsolutePart = (absolutePart + 1).coerceIn(0 until oldN)
        val fractionalPart = fi - absolutePart

        if (fractionalPart < 0.000_001) {
            x[absolutePart]
        } else {
            FloatArray(d) { j ->
                x[absolutePart][j] + fractionalPart * (x[nextAbsolutePart][j] - x[absolutePart][j])
            }
        }
    }
}

enum class InterpolationStrategy {
    TO_SMALLER,
    TO_BIGGER
}

internal fun keoghLowerBoundOf(
    candidate: Array<FloatArray>,
    query: Array<FloatArray>,
    lowerBoundRadius: Float,
    distanceFunction: (FloatArray, FloatArray) -> Float
): Float {
    val n = candidate.size
    val d = candidate[0].size
    val envelope = envelopeOf(query, lowerBoundRadius)

    return when (distanceFunction) {
        ::manhattanDistanceOf -> {
            var sum = 0f
            for (i in 0 until n) {
                for (j in 0 until d) {
                    sum += when {
                        candidate[i][j] < envelope.lower[i][j] -> abs(envelope.lower[i][j] - candidate[i][j])
                        candidate[i][j] > envelope.upper[i][j] -> abs(envelope.upper[i][j] - candidate[i][j])
                        else -> 0f
                    }
                }
            }
            sum
        }

        ::euclideanDistanceOf -> {
            var sum = 0f
            for (i in 0 until n) {
                for (j in 0 until d) {
                    sum += when {
                        candidate[i][j] < envelope.lower[i][j] -> (envelope.lower[i][j] - candidate[i][j]).pow(2)
                        candidate[i][j] > envelope.upper[i][j] -> (envelope.upper[i][j] - candidate[i][j]).pow(2)
                        else -> 0f
                    }
                }
            }
            sqrt(sum)
        }

        ::squaredEuclideanDistanceOf -> {
            var sum = 0f
            for (i in 0 until n) {
                for (j in 0 until d) {
                    sum += when {
                        candidate[i][j] < envelope.lower[i][j] -> (envelope.lower[i][j] - candidate[i][j]).pow(2)
                        candidate[i][j] > envelope.upper[i][j] -> (envelope.upper[i][j] - candidate[i][j]).pow(2)
                        else -> 0f
                    }
                }
            }
            sum
        }

        else -> throw Exception("Distance function is not supported.")
    }
}

internal fun envelopeOf(
    query: Array<FloatArray>,
    lowerBoundRadius: Float
): Envelope {
    val n = query.size
    val d = query[0].size

    val validRadius = lowerBoundRadius.coerceAtLeast(0f)

    val min = FloatArray(d) { Float.POSITIVE_INFINITY }
    val max = FloatArray(d) { Float.NEGATIVE_INFINITY }

    for (i in 0 until n) {
        for (j in 0 until d) {
            min[j] = min(query[i][j], min[j])
            max[j] = max(query[i][j], max[j])
        }
    }

    val lower = Array(n) { i ->
        FloatArray(d) { j ->
            max(query[i][j] - validRadius, min[j])
        }
    }

    val upper = Array(n) { i ->
        FloatArray(d) { j ->
            min(query[i][j] + validRadius, max[j])
        }
    }

    return Envelope(lower, upper)
}

internal data class Envelope(
    val lower: Array<FloatArray>,
    val upper: Array<FloatArray>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Envelope

        if (!lower.contentDeepEquals(other.lower)) return false
        if (!upper.contentDeepEquals(other.upper)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lower.contentDeepHashCode()
        result = 31 * result + upper.contentDeepHashCode()
        return result
    }
}

internal data class SubsequenceDynamicTimeWarpingReport(
    val start: Int,
    val endInclusive: Int,
    val cost: Float,
    val numberOfPrunedOutCalculations: Long,
    val numberOfNotPrunedOutCalculations: Long
)