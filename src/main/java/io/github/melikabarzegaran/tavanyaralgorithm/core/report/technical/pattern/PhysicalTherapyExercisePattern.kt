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

package io.github.melikabarzegaran.tavanyaralgorithm.core.report.technical.pattern

import io.github.melikabarzegaran.tavanyaralgorithm.core.PhysicalTherapyExerciseExecution
import io.github.melikabarzegaran.tavanyaralgorithm.core.PhysicalTherapyExerciseType

data class PhysicalTherapyExercisePattern(
    val type: PhysicalTherapyExerciseType,
    val execution: PhysicalTherapyExerciseExecution,
    val range: PhysicalTherapyExercisePatternRange,
    val cost: Float
) {
    internal fun toPatternIndexed(index: Int) =
        PhysicalTherapyExercisePatternIndexed(index, type, execution, range, cost)
}

internal data class PhysicalTherapyExercisePatternIndexed(
    val index: Int,
    val type: PhysicalTherapyExerciseType,
    val execution: PhysicalTherapyExerciseExecution,
    val range: PhysicalTherapyExercisePatternRange,
    val cost: Float
) {
    internal fun toPattern() = PhysicalTherapyExercisePattern(type, execution, range, cost)
}