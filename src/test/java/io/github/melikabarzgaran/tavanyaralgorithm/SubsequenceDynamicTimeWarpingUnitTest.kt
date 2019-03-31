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
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.roundToInt

class SubsequenceDynamicTimeWarpingUnitTest {

    @Test
    fun `given sequence with length = 1, when interpolating, then duplicates`() {
        val x = floatArrayOf(1f).to2D()
        val interpolatedX = linearInterpolateOf(x, 10)
        assertArrayEquals(Array(10) { floatArrayOf(1f) }, interpolatedX)
    }

    @Test
    fun `given linear, 1-col sequence, when interpolating, then does it correctly`() {
        val x = floatArrayOf(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f).to2D()

        linearInterpolateOf(x, 20).let { interpolatedX ->
            assertEquals(20, interpolatedX.size)
            for (row in interpolatedX) {
                assertEquals(1, row.size)
                assertTrue(row[0] in 1f..10f)
            }
        }

        linearInterpolateOf(x, 5).let { interpolatedX ->
            assertEquals(5, interpolatedX.size)
            for (row in interpolatedX) {
                assertEquals(1, row.size)
                assertTrue(row[0] in 1f..10f)
            }
        }

        linearInterpolateOf(x, 13).let { interpolatedX ->
            assertEquals(13, interpolatedX.size)
            for (row in interpolatedX) {
                assertEquals(1, row.size)
                assertTrue(row[0] in 1f..10f)
            }
        }

        linearInterpolateOf(x, 7).let { interpolatedX ->
            assertEquals(7, interpolatedX.size)
            for (row in interpolatedX) {
                assertEquals(1, row.size)
                assertTrue(row[0] in 1f..10f)
            }
        }
    }

    @Test
    fun `given non-linear, 1-col sequence, when interpolating, then does it correctly`() {
        val x = floatArrayOf(1f, 2f, 3f, 4f, 5f, 5f, 4f, 3f, 2f, 1f).to2D()

        linearInterpolateOf(x, 20).let { interpolatedX ->
            assertEquals(20, interpolatedX.size)
            for (row in interpolatedX) {
                assertEquals(1, row.size)
                assertTrue(row[0] in 1f..5f)
            }
        }

        linearInterpolateOf(x, 5).let { interpolatedX ->
            assertEquals(5, interpolatedX.size)
            for (row in interpolatedX) {
                assertEquals(1, row.size)
                assertTrue(row[0] in 1f..5f)
            }
        }

        linearInterpolateOf(x, 13).let { interpolatedX ->
            assertEquals(13, interpolatedX.size)
            for (row in interpolatedX) {
                assertEquals(1, row.size)
                assertTrue(row[0] in 1f..5f)
            }
        }

        linearInterpolateOf(x, 7).let { interpolatedX ->
            assertEquals(7, interpolatedX.size)
            for (row in interpolatedX) {
                assertEquals(1, row.size)
                assertTrue(row[0] in 1f..5f)
            }
        }
    }

    @Test
    fun `given linear, 2-col sequence, when interpolating, then does it correctly`() {
        val x = arrayOf(
            floatArrayOf(1f, 2f),
            floatArrayOf(2f, 4f),
            floatArrayOf(3f, 6f),
            floatArrayOf(4f, 8f),
            floatArrayOf(5f, 10f),
            floatArrayOf(6f, 12f),
            floatArrayOf(7f, 14f),
            floatArrayOf(8f, 16f),
            floatArrayOf(9f, 18f),
            floatArrayOf(10f, 20f)
        )

        linearInterpolateOf(x, 20).let { interpolatedX ->
            assertEquals(20, interpolatedX.size)
            for (row in interpolatedX) {
                assertEquals(2, row.size)
                assertTrue(row[0] in 1f..10f)
                assertTrue(row[1] in 2f..20f)
            }
        }

        linearInterpolateOf(x, 5).let { interpolatedX ->
            assertEquals(5, interpolatedX.size)
            for (row in interpolatedX) {
                assertEquals(2, row.size)
                assertTrue(row[0] in 1f..10f)
                assertTrue(row[1] in 2f..20f)
            }
        }

        linearInterpolateOf(x, 13).let { interpolatedX ->
            assertEquals(13, interpolatedX.size)
            for (row in interpolatedX) {
                assertEquals(2, row.size)
                assertTrue(row[0] in 1f..10f)
                assertTrue(row[1] in 2f..20f)
            }
        }

        linearInterpolateOf(x, 7).let { interpolatedX ->
            assertEquals(7, interpolatedX.size)
            for (row in interpolatedX) {
                assertEquals(2, row.size)
                assertTrue(row[0] in 1f..10f)
                assertTrue(row[1] in 2f..20f)
            }
        }
    }

    @Test
    fun `given non-linear, 2-col sequence, when interpolating, then does it correctly`() {
        val x = arrayOf(
            floatArrayOf(1f, 5f),
            floatArrayOf(2f, 4f),
            floatArrayOf(3f, 3f),
            floatArrayOf(4f, 2f),
            floatArrayOf(5f, 1f),
            floatArrayOf(5f, 1f),
            floatArrayOf(4f, 2f),
            floatArrayOf(3f, 3f),
            floatArrayOf(2f, 4f),
            floatArrayOf(1f, 5f)
        )

        linearInterpolateOf(x, 20).let { interpolatedX ->
            assertEquals(20, interpolatedX.size)
            for (row in interpolatedX) {
                assertEquals(2, row.size)
                assertTrue(row[0] in 1f..5f)
                assertTrue(row[1] in 1f..5f)
            }
        }

        linearInterpolateOf(x, 5).let { interpolatedX ->
            assertEquals(5, interpolatedX.size)
            for (row in interpolatedX) {
                assertEquals(2, row.size)
                assertTrue(row[0] in 1f..5f)
                assertTrue(row[1] in 1f..5f)
            }
        }

        linearInterpolateOf(x, 13).let { interpolatedX ->
            assertEquals(13, interpolatedX.size)
            for (row in interpolatedX) {
                assertEquals(2, row.size)
                assertTrue(row[0] in 1f..5f)
                assertTrue(row[1] in 1f..5f)
            }
        }

        linearInterpolateOf(x, 7).let { interpolatedX ->
            assertEquals(7, interpolatedX.size)
            for (row in interpolatedX) {
                assertEquals(2, row.size)
                assertTrue(row[0] in 1f..5f)
                assertTrue(row[1] in 1f..5f)
            }
        }
    }

    @Test
    fun `given n smaller than 2, when interpolating, then interpolates to n = 2`() {
        val x = floatArrayOf(0f, 1f, 2f, 3f, 4f).to2D()
        val y = arrayOf(floatArrayOf(0f), floatArrayOf(4f))
        assertArrayEquals(y, linearInterpolateOf(x, -1))
        assertArrayEquals(y, linearInterpolateOf(x, 0))
        assertArrayEquals(y, linearInterpolateOf(x, 1))
        assertArrayEquals(y, linearInterpolateOf(x, 2))
    }

    @Test
    fun `given sequences with different lengths, and and a specific interpolation strategy, when interpolating, then order of sequences does not matter`() {
        val x = floatArrayOf(0f, 1f, 2f).to2D()
        val y = floatArrayOf(0f, 1f, 2f, 3f).to2D()

        linearInterpolateOf(x, y, InterpolationStrategy.TO_SMALLER).let { (interpolatedX, interpolatedY) ->
            val (otherInterpolateY, otherInterpolatedX) = linearInterpolateOf(y, x, InterpolationStrategy.TO_SMALLER)
            assertArrayEquals(interpolatedX, otherInterpolatedX)
            assertArrayEquals(interpolatedY, otherInterpolateY)
        }

        linearInterpolateOf(x, y, InterpolationStrategy.TO_BIGGER).let { (interpolatedX, interpolatedY) ->
            val (otherInterpolateY, otherInterpolatedX) = linearInterpolateOf(y, x, InterpolationStrategy.TO_BIGGER)
            assertArrayEquals(interpolatedX, otherInterpolatedX)
            assertArrayEquals(interpolatedY, otherInterpolateY)
        }
    }

    @Test
    fun `given sequences with the same length, and a specific interpolation strategy, when interpolating, the does not interpolate`() {
        val x = floatArrayOf(0f, 1f, 2f).to2D()

        linearInterpolateOf(x, x, InterpolationStrategy.TO_SMALLER).let { (interpolatedX, otherInterpolatedX) ->
            assertArrayEquals(x, interpolatedX)
            assertArrayEquals(x, otherInterpolatedX)
        }
        linearInterpolateOf(x, x, InterpolationStrategy.TO_BIGGER).let { (interpolatedX, otherInterpolatedX) ->
            assertArrayEquals(x, interpolatedX)
            assertArrayEquals(x, otherInterpolatedX)
        }
    }

    @Test
    fun `given 1-col sequence, when creating envelope, then does it correctly`() {
        val query = floatArrayOf(0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f).to2D()
        val envelope = envelopeOf(query, 2f)
        assertArrayEquals(floatArrayOf(0f, 0f, 0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f).to2D(), envelope.lower)
        assertArrayEquals(floatArrayOf(2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 9f, 9f).to2D(), envelope.upper)
    }

    @Test
    fun `given 2-col sequence, when creating envelope, then does it correctly`() {
        val query = arrayOf(
            floatArrayOf(1f, 5f),
            floatArrayOf(2f, 4f),
            floatArrayOf(3f, 3f),
            floatArrayOf(4f, 2f),
            floatArrayOf(5f, 1f),
            floatArrayOf(5f, 1f),
            floatArrayOf(4f, 2f),
            floatArrayOf(3f, 3f),
            floatArrayOf(2f, 4f),
            floatArrayOf(1f, 5f)
        )
        val envelope = envelopeOf(query, 2f)
        assertArrayEquals(
            arrayOf(
                floatArrayOf(1f, 3f),
                floatArrayOf(1f, 2f),
                floatArrayOf(1f, 1f),
                floatArrayOf(2f, 1f),
                floatArrayOf(3f, 1f),
                floatArrayOf(3f, 1f),
                floatArrayOf(2f, 1f),
                floatArrayOf(1f, 1f),
                floatArrayOf(1f, 2f),
                floatArrayOf(1f, 3f)
            ),
            envelope.lower
        )
        assertArrayEquals(
            arrayOf(
                floatArrayOf(3f, 5f),
                floatArrayOf(4f, 5f),
                floatArrayOf(5f, 5f),
                floatArrayOf(5f, 4f),
                floatArrayOf(5f, 3f),
                floatArrayOf(5f, 3f),
                floatArrayOf(5f, 4f),
                floatArrayOf(5f, 5f),
                floatArrayOf(4f, 5f),
                floatArrayOf(3f, 5f)
            ),
            envelope.upper
        )
    }

    @Test
    fun `given lower bound radius smaller than 0, when creating envelope, then envelopes it with lower bound radius = 0`() {
        val query = floatArrayOf(0f, 1f, 2f, 3f, 4f).to2D()
        val envelope = envelopeOf(query, -1f)
        val otherEnvelope = envelopeOf(query, 0f)
        assertArrayEquals(envelope.lower, otherEnvelope.lower)
        assertArrayEquals(envelope.upper, otherEnvelope.upper)
    }

    @Test
    fun `given manhattan distance function, when computing keogh lower bound, then computes it correctly`() {
        val candidate = floatArrayOf(0f, 2f, 2f, 0f, -2f, -2f, 0f).to2D()
        val query = floatArrayOf(0f, 1f, 1f, 0f, -1f, -1f, 0f).to2D()
        assertEquals(4f, keoghLowerBoundOf(candidate, query, 1f, ::manhattanDistanceOf))
    }

    @Test
    fun `given euclidean distance function, when computing keogh lower bound, then computes it correctly`() {
        val candidate = floatArrayOf(0f, 2f, 2f, 0f, -2f, -2f, 0f).to2D()
        val query = floatArrayOf(0f, 1f, 1f, 0f, -1f, -1f, 0f).to2D()
        assertEquals(2f, keoghLowerBoundOf(candidate, query, 1f, ::euclideanDistanceOf))
    }

    @Test
    fun `given squared euclidean distance function, when computing keogh lower bound, then computes it correctly`() {
        val candidate = floatArrayOf(0f, 2f, 2f, 0f, -2f, -2f, 0f).to2D()
        val query = floatArrayOf(0f, 1f, 1f, 0f, -1f, -1f, 0f).to2D()
        assertEquals(4f, keoghLowerBoundOf(candidate, query, 1f, ::squaredEuclideanDistanceOf))
    }

    @Test
    fun `given unknown distance function, when computing keogh lower bound, then throws exception`() {
        val candidate = floatArrayOf(0f, 2f, 2f, 0f, -2f, -2f, 0f).to2D()
        val query = floatArrayOf(0f, 1f, 1f, 0f, -1f, -1f, 0f).to2D()
        try {
            keoghLowerBoundOf(candidate, query, 1f) { _, _ -> Float.POSITIVE_INFINITY }
            fail()
        } catch (e: Exception) {
            assertEquals(e.message, "Distance function is not supported.")
        }
    }

    @Test
    fun `should compute subsequence dynamic time warping correctly`() = runBlocking {
        val x = floatArrayOf(-10f, 0f, 10f, 0f, -10f).to2D()
        val y = floatArrayOf(
            0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f,
            -10f, 0f, 10f, 0f, -10f,
            0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f
        ).to2D()
        val result = subsequenceDynamicTimeWarpingOf(
            x,
            y,
            ::euclideanDistanceOf,
            LocalWeights.SYMMETRIC,
            1f,
            GeneralizationStrategy.DEPENDANT,
            1,
            0f,
            InterpolationStrategy.TO_BIGGER,
            0.1f
        )
        val median = ((result.start + result.endInclusive).toFloat() / 2f).roundToInt()
        assertTrue(median in 10..14)
        assertEquals(0f, result.cost)
        assertEquals(21, result.numberOfPrunedOutCalculations + result.numberOfNotPrunedOutCalculations)
    }
}