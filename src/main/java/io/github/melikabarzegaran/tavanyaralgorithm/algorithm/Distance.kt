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

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

fun manhattanDistanceOf(x: FloatArray, y: FloatArray): Float {
    val n = x.size
    var sum = 0f
    for (i in 0 until n) {
        sum += abs(x[i] - y[i])
    }
    return sum
}

fun euclideanDistanceOf(x: FloatArray, y: FloatArray): Float {
    val n = x.size
    var sum = 0f
    for (i in 0 until n) {
        sum += (x[i] - y[i]).pow(2)
    }
    return sqrt(sum)
}

fun squaredEuclideanDistanceOf(x: FloatArray, y: FloatArray): Float {
    val n = x.size
    var sum = 0f
    for (i in 0 until n) {
        sum += (x[i] - y[i]).pow(2)
    }
    return sum
}