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

import java.io.BufferedReader
import java.io.FileReader

internal fun FloatArray.to2D() = map { floatArrayOf(it) }.toTypedArray()

internal fun readData(
    path: String,
    hasTimeLabel: Boolean = false
): Array<FloatArray> {
    val data = mutableListOf<FloatArray>()
    BufferedReader(FileReader(path)).useLines { lines ->
        lines.forEach { line ->
            if (hasTimeLabel) {
                line.drop(16)
            } else {
                line
            }.split(",")
                .map { it.toFloat() }
                .toFloatArray()
                .also { data.add(it) }
        }
    }
    return data.toTypedArray()
}

internal fun readLabelData(path: String): Array<IntArray> {
    val data = mutableListOf<IntArray>()
    BufferedReader(FileReader(path)).useLines { lines ->
        lines.forEach { line ->
            line.split(",")
                .map { it.trim().toInt() }
                .toIntArray()
                .also { data.add(it) }
        }
    }
    return data.toTypedArray()
}

internal fun Array<IntArray>.print() {
    for (row in this) row.joinToString(transform = { String.format("%3d", it) }).also { println(it) }
}