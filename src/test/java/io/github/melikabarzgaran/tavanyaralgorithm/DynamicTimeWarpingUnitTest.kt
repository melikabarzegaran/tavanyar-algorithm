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

package io.github.melikabarzgaran.tavanyaralgorithm

import io.github.melikabarzegaran.tavanyaralgorithm.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class DynamicTimeWarpingUnitTest {

    @Test
    fun `should dynamic time warping respect 1st rule of being a metric`() {
        val x = floatArrayOf(0f, 1f, 2f, 3f, 4f).to2D()
        val y = floatArrayOf(5f, 6f, 7f, 8f, 9f).to2D()

        /* d(x, y) >= 0 */
        assertTrue(
            dynamicTimeWarpingOf(
                x,
                y,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 1f
            ) >= 0f
        )
    }

    @Test
    fun `should "not" dynamic time warping respect 2nd rule of being a metric`() {
        val x = floatArrayOf(1f, 1f, 3f, 1f, 2f, 1f, 1f).to2D()
        val y = floatArrayOf(1f, 3f, 1f, 2f, 1f, 1f, 1f).to2D()

        /* d(x, y) = 0 <=> x = y */
        assertEquals(
            0f, dynamicTimeWarpingOf(
                x,
                x,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 1f
            )
        )
        assertEquals(
            0f, dynamicTimeWarpingOf(
                x,
                y,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 1f
            )
        )
    }

    @Test
    fun `should dynamic time warping respect 3rd rule of being a metric`() {
        val x = floatArrayOf(0f, 1f, 2f, 3f, 4f).to2D()
        val y = floatArrayOf(5f, 6f, 7f, 8f, 9f).to2D()

        /* d(x, y) = d(y, x) */
        assertEquals(
            dynamicTimeWarpingOf(
                x,
                y,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 1f
            ),
            dynamicTimeWarpingOf(
                y,
                x,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 1f
            )
        )
    }

    @Test
    fun `given sequences with odd or even length, when computing dynamic time warping, then computes correctly`() {
        val x = floatArrayOf(0f, 1f, 2f).to2D()
        val y = floatArrayOf(0f, 1f, 2f, 3f).to2D()

        /* Odd, odd */
        assertEquals(
            0f, dynamicTimeWarpingOf(
                x,
                x,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 1f
            )
        )

        /* Even, even */
        assertEquals(
            0f, dynamicTimeWarpingOf(
                y,
                y,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 1f
            )
        )

        /* Odd, even */
        assertEquals(
            1f, dynamicTimeWarpingOf(
                x,
                y,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 1f
            )
        )

        /* Even, odd */
        assertEquals(
            1f, dynamicTimeWarpingOf(
                y,
                x,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 1f
            )
        )
    }

    @Test
    fun `given any distance function, when computing dynamic time warping, then computes correctly`() {
        val x = floatArrayOf(0f, 1f, 2f).to2D()
        val y = floatArrayOf(3f, 4f, 5f, 6f).to2D()

        /* Manhattan distance function */
        assertEquals(
            13f, dynamicTimeWarpingOf(
                x,
                y,
                distanceFunction = ::manhattanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 1f
            )
        )

        /* Euclidean distance function */
        assertEquals(
            13f, dynamicTimeWarpingOf(
                x,
                y,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 1f
            )
        )

        /* Squared euclidean distance function */
        assertEquals(
            42f, dynamicTimeWarpingOf(
                x,
                y,
                distanceFunction = ::squaredEuclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 1f
            )
        )
    }

    @Test
    fun `given any local weights, when computing dynamic time warping, then computes correctly`() {
        val x = floatArrayOf(0f, 1f, 2f).to2D()
        val y = floatArrayOf(3f, 4f, 5f, 6f).to2D()

        /* Symmetric local weights (wh = 1, wv = 1, wd = 1) */
        assertEquals(
            13f, dynamicTimeWarpingOf(
                x,
                y,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 1f
            )
        )

        /* Asymmetric local weights (wh = 1, wv = 1, wd = 2) */
        assertEquals(
            15f, dynamicTimeWarpingOf(
                x,
                y,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.ASYMMETRIC,
                globalConstraintWidthFactor = 1f
            )
        )
    }

    @Test
    fun `given any global constraint width factor, and sequences with the same length, when computing dynamic time warping, then computes correctly`() {
        val x = floatArrayOf(0f, 1f, 0f, 1f, 2f, 3f).to2D()
        val y = floatArrayOf(0f, 0f, 0f, 1f, 0f, 1f).to2D()

        /* Global constraint width factor = 0%, w = 0 */
        assertEquals(
            5f, dynamicTimeWarpingOf(
                x,
                y,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 0f
            )
        )

        /* Global constraint width factor = 20%, w = 1 */
        assertEquals(
            5f, dynamicTimeWarpingOf(
                x,
                y,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 0.2f
            )
        )

        /* Global constraint width factor = 40%, w = 2 */
        assertEquals(
            3f, dynamicTimeWarpingOf(
                x,
                y,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 0.4f
            )
        )

        /* Global constraint width factor = 60%, w = 3 */
        assertEquals(
            3f, dynamicTimeWarpingOf(
                x,
                y,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 0.6f
            )
        )

        /* Global constraint width factor = 80%, w = 4 */
        assertEquals(
            3f, dynamicTimeWarpingOf(
                x,
                y,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 0.8f
            )
        )

        /* Global constraint width factor = 100%, w = 5 */
        assertEquals(
            3f, dynamicTimeWarpingOf(
                x,
                y,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 1f
            )
        )
    }

    @Test
    fun `given any global constraint width factor, and sequences with different lengths, when computing dynamic time warping, then computes correctly`() {
        val x = floatArrayOf(0f, 1f, 0f, 1f).to2D()
        val y = floatArrayOf(0f, 0f, 0f, 0f, 1f, 0f).to2D()

        /* Global constraint width factor = 0% but w = 2 */
        assertEquals(
            2f, dynamicTimeWarpingOf(
                x,
                y,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 0f
            )
        )

        /* Global constraint width factor = 20% but w = 2 */
        assertEquals(
            2f, dynamicTimeWarpingOf(
                x,
                y,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 0.2f
            )
        )

        /* Global constraint width factor = 40%, w = 2 */
        assertEquals(
            2f, dynamicTimeWarpingOf(
                x,
                y,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 0.4f
            )
        )

        /* Global constraint width factor = 60%, w = 3 */
        assertEquals(
            1f, dynamicTimeWarpingOf(
                x,
                y,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 0.6f
            )
        )

        /* Global constraint width factor = 80%, w = 4 */
        assertEquals(
            1f, dynamicTimeWarpingOf(
                x,
                y,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 0.8f
            )
        )

        /* Global constraint width factor = 100%, w = 5 */
        assertEquals(
            1f, dynamicTimeWarpingOf(
                x,
                y,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 1f
            )
        )

        /* Global constraint width factor = 0% but w = 2 */
        assertEquals(
            2f, dynamicTimeWarpingOf(
                y,
                x,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 0f
            )
        )

        /* Global constraint width factor = 20% but w = 2 */
        assertEquals(
            2f, dynamicTimeWarpingOf(
                y,
                x,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 0.2f
            )
        )

        /* Global constraint width factor = 40%, w = 2 */
        assertEquals(
            2f, dynamicTimeWarpingOf(
                y,
                x,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 0.4f
            )
        )

        /* Global constraint width factor = 60%, w = 3 */
        assertEquals(
            1f, dynamicTimeWarpingOf(
                y,
                x,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 0.6f
            )
        )

        /* Global constraint width factor = 80%, w = 4 */
        assertEquals(
            1f, dynamicTimeWarpingOf(
                y,
                x,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 0.8f
            )
        )

        /* Global constraint width factor = 100%, w = 5 */
        assertEquals(
            1f, dynamicTimeWarpingOf(
                y,
                x,
                distanceFunction = ::euclideanDistanceOf,
                localWeights = LocalWeights.SYMMETRIC,
                globalConstraintWidthFactor = 1f
            )
        )
    }

    @Test
    fun `given any generalization strategy, when computing dynamic time warping, then computes correctly`() =
        runBlocking {
            val x = arrayOf(
                floatArrayOf(0f, 0f),
                floatArrayOf(1f, 2f),
                floatArrayOf(2f, 4f)
            )
            val y = arrayOf(
                floatArrayOf(0f, 0f),
                floatArrayOf(1f, 3f),
                floatArrayOf(2f, 6f),
                floatArrayOf(3f, 9f)
            )

            /* Dependent generalization strategy */
            assertEquals(
                sqrt(26f) + 3f,
                dynamicTimeWarpingOf(
                    x,
                    y,
                    distanceFunction = ::euclideanDistanceOf,
                    localWeights = LocalWeights.SYMMETRIC,
                    globalConstraintWidthFactor = 1f,
                    generalizationStrategy = GeneralizationStrategy.DEPENDANT
                )
            )

            /* Independent generalization strategy */
            assertEquals(
                9f,
                dynamicTimeWarpingOf(
                    x,
                    y,
                    distanceFunction = ::euclideanDistanceOf,
                    localWeights = LocalWeights.SYMMETRIC,
                    globalConstraintWidthFactor = 1f,
                    generalizationStrategy = GeneralizationStrategy.INDEPENDENT
                )
            )
        }
}