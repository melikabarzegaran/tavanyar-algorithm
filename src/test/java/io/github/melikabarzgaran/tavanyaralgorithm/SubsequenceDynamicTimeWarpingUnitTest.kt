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

import io.github.melikabarzegaran.tavanyaralgorithm.algorithm.LocalWeights
import io.github.melikabarzegaran.tavanyaralgorithm.algorithm.euclideanDistanceOf
import io.github.melikabarzegaran.tavanyaralgorithm.algorithm.subsequenceDynamicTimeWarpingOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.roundToInt

class SubsequenceDynamicTimeWarpingUnitTest {

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
            LocalWeights.SYMMETRIC
        )

        val median = ((result.start + result.endInclusive).toFloat() / 2f).roundToInt()
        assertTrue(median in 10..14)
        assertEquals(0f, result.cost)
    }
}