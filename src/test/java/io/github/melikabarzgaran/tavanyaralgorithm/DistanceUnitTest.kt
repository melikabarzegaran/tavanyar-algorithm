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

import io.github.melikabarzegaran.tavanyaralgorithm.euclideanDistanceOf
import io.github.melikabarzegaran.tavanyaralgorithm.manhattanDistanceOf
import io.github.melikabarzegaran.tavanyaralgorithm.squaredEuclideanDistanceOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class DistanceUnitTest {

    @Test
    fun `should manhattan distance be a metric`() {
        val x = floatArrayOf(0f, 1f, 2f, 3f, 4f)
        val y = floatArrayOf(5f, 6f, 7f, 8f, 9f)
        val z = floatArrayOf(10f, 11f, 12f, 13f, 14f)

        /* Should respect 1st rule of being a metric: d(x, y) >= 0 */
        assertTrue(manhattanDistanceOf(x, y) >= 0f)

        /* Should respect 2nd rule of being a metric: d(x, y) = 0 <=> x = y */
        assertEquals(0f, manhattanDistanceOf(x, x))

        /* Should respect 3rd rule of being a metric: d(x, y) = d(y, x) */
        assertEquals(manhattanDistanceOf(x, y), manhattanDistanceOf(y, x))

        /* Should respect 4th rule of being a metric: d(x, z) <= d(x, y) + d(y, z) */
        assertTrue(manhattanDistanceOf(x, z) <= manhattanDistanceOf(x, y) + manhattanDistanceOf(y, z))
    }

    @Test
    fun `should euclidean distance be a metric`() {
        val x = floatArrayOf(0f, 1f, 2f, 3f, 4f)
        val y = floatArrayOf(5f, 6f, 7f, 8f, 9f)
        val z = floatArrayOf(10f, 11f, 12f, 13f, 14f)

        /* Should respect 1st rule of being a metric: d(x, y) >= 0 */
        assertTrue(euclideanDistanceOf(x, y) >= 0f)

        /* Should respect 2nd rule of being a metric: d(x, y) = 0 <=> x = y */
        assertEquals(0f, euclideanDistanceOf(x, x))

        /* Should respect 3rd rule of being a metric: d(x, y) = d(y, x) */
        assertEquals(euclideanDistanceOf(x, y), euclideanDistanceOf(y, x))

        /* Should respect 4th rule of being a metric: d(x, z) <= d(x, y) + d(y, z) */
        assertTrue(euclideanDistanceOf(x, z) <= euclideanDistanceOf(x, y) + euclideanDistanceOf(y, z))
    }

    @Test
    fun `should "not" squared euclidean distance be a metric`() {
        val x = floatArrayOf(0f, 1f, 2f, 3f, 4f)
        val y = floatArrayOf(5f, 6f, 7f, 8f, 9f)

        /* Should respect 1st rule of being a metric: d(x, y) >= 0 */
        assertTrue(squaredEuclideanDistanceOf(x, y) >= 0f)

        /* Should respect 2nd rule of being a metric: d(x, y) = 0 <=> x = y */
        assertEquals(0f, squaredEuclideanDistanceOf(x, x))

        /* Should respect 3rd rule of being a metric: d(x, y) = d(y, x) */
        assertEquals(squaredEuclideanDistanceOf(x, y), squaredEuclideanDistanceOf(y, x))

        /* But does "not" respect 4th rule of being a metric: d(x, z) <= d(x, y) + d(y, z) */
    }

    @Test
    fun `given samples of any positive dimension, when computing manhattan distance, then computes correctly`() {
        assertEquals(1f * 1f, manhattanDistanceOf(floatArrayOf(0f), floatArrayOf(1f)))
        assertEquals(2f * 2f, manhattanDistanceOf(floatArrayOf(0f, 1f), floatArrayOf(2f, 3f)))
        assertEquals(
            12f * 12f,
            manhattanDistanceOf(
                floatArrayOf(0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 11f),
                floatArrayOf(12f, 13f, 14f, 15f, 16f, 17f, 18f, 19f, 20f, 21f, 22f, 23f)
            )
        )
    }

    @Test
    fun `given samples of any positive dimension, when computing euclidean distance, then computes correctly`() {
        assertEquals(sqrt(1f * 1f * 1f), euclideanDistanceOf(floatArrayOf(0f), floatArrayOf(1f)))
        assertEquals(sqrt(2f * 2f * 2f), euclideanDistanceOf(floatArrayOf(0f, 1f), floatArrayOf(2f, 3f)))
        assertEquals(
            sqrt(12f * 12f * 12f),
            euclideanDistanceOf(
                floatArrayOf(0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 11f),
                floatArrayOf(12f, 13f, 14f, 15f, 16f, 17f, 18f, 19f, 20f, 21f, 22f, 23f)
            )
        )
    }

    @Test
    fun `given samples of any positive dimension, when computing squared euclidean distance, then computes correctly`() {
        assertEquals(1f * 1f * 1f, squaredEuclideanDistanceOf(floatArrayOf(0f), floatArrayOf(1f)))
        assertEquals(2f * 2f * 2f, squaredEuclideanDistanceOf(floatArrayOf(0f, 1f), floatArrayOf(2f, 3f)))
        assertEquals(
            12f * 12f * 12f,
            squaredEuclideanDistanceOf(
                floatArrayOf(0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 11f),
                floatArrayOf(12f, 13f, 14f, 15f, 16f, 17f, 18f, 19f, 20f, 21f, 22f, 23f)
            )
        )
    }
}