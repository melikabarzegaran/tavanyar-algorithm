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

package io.github.melikabarzegaran.tavanyaralgorithm.core

import io.github.melikabarzegaran.tavanyaralgorithm.algorithm.LocalWeights
import io.github.melikabarzegaran.tavanyaralgorithm.algorithm.SubsequenceDynamicTimeWarpingReport
import io.github.melikabarzegaran.tavanyaralgorithm.algorithm.squaredEuclideanDistanceOf
import io.github.melikabarzegaran.tavanyaralgorithm.algorithm.subsequenceDynamicTimeWarpingOf
import io.github.melikabarzegaran.tavanyaralgorithm.core.report.PhysicalTherapyReport
import io.github.melikabarzegaran.tavanyaralgorithm.core.report.analytical.PhysicalTherapyAnalyticalReport
import io.github.melikabarzegaran.tavanyaralgorithm.core.report.analytical.PhysicalTherapyCountReport
import io.github.melikabarzegaran.tavanyaralgorithm.core.report.analytical.PhysicalTherapyTimeReport
import io.github.melikabarzegaran.tavanyaralgorithm.core.report.analytical.execution.PhysicalTherapyExerciseExecutionCountReport
import io.github.melikabarzegaran.tavanyaralgorithm.core.report.analytical.execution.PhysicalTherapyExerciseExecutionReport
import io.github.melikabarzegaran.tavanyaralgorithm.core.report.analytical.execution.PhysicalTherapyExerciseExecutionTimeReport
import io.github.melikabarzegaran.tavanyaralgorithm.core.report.analytical.type.PhysicalTherapyExerciseTypeCountReport
import io.github.melikabarzegaran.tavanyaralgorithm.core.report.analytical.type.PhysicalTherapyExerciseTypeReport
import io.github.melikabarzegaran.tavanyaralgorithm.core.report.analytical.type.PhysicalTherapyExerciseTypeTimeReport
import io.github.melikabarzegaran.tavanyaralgorithm.core.report.technical.PhysicalTherapyTechnicalReport
import io.github.melikabarzegaran.tavanyaralgorithm.core.report.technical.pattern.PhysicalTherapyExercisePattern
import io.github.melikabarzegaran.tavanyaralgorithm.core.report.technical.pattern.PhysicalTherapyExercisePatternIndexed
import io.github.melikabarzegaran.tavanyaralgorithm.core.report.technical.pattern.PhysicalTherapyExercisePatternRange
import io.github.melikabarzegaran.tavanyaralgorithm.core.report.technical.performance.PhysicalTherapyPerformanceReport
import io.github.melikabarzegaran.tavanyaralgorithm.util.bgDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.roundToInt

suspend fun PhysicalTherapySession.evaluateUsing(
    physicalTherapyExerciseList: List<PhysicalTherapyExercise>,
    distanceFunction: (FloatArray, FloatArray) -> Float = ::squaredEuclideanDistanceOf,
    localWeights: LocalWeights = LocalWeights.SYMMETRIC,
    lengthToleranceFactor: Float = 0.5f,
    overlappingFactor: Float = 0.05f,
    costThreshold: Float = 0.3f,
    onNextIteration: (iterationId: Int) -> Unit = {},
    onPhysicalTherapyExercisePatternFound: (pattern: PhysicalTherapyExercisePattern) -> Unit = {},
    onBestInIterationPhysicalTherapyExercisePatternChosen: (pattern: PhysicalTherapyExercisePattern) -> Unit = {},
    onFinished: (timeInMilliseconds: Long) -> Unit = {}
): PhysicalTherapyReport {
    val dataset = getDataset(physicalTherapyExerciseList)

    val normalizedDataset = normalizeDataset(dataset)

    val normalizedPhysicalTherapyExerciseList =
        getNormalizedPhysicalTherapyExerciseList(
            normalizedDataset,
            physicalTherapyExerciseList
        )

    val normalizedPhysicalTherapySession =
        getNormalizedPhysicalTherapySession(
            normalizedDataset
        )

    val physicalTherapyTechnicalReport = classify(
        normalizedPhysicalTherapyExerciseList,
        normalizedPhysicalTherapySession,
        distanceFunction,
        localWeights,
        lengthToleranceFactor,
        overlappingFactor,
        costThreshold,
        onNextIteration = onNextIteration,
        onPhysicalTherapyExercisePatternFound = onPhysicalTherapyExercisePatternFound,
        onBestInIterationPhysicalTherapyExercisePatternFound = onBestInIterationPhysicalTherapyExercisePatternChosen,
        onFinished = onFinished
    )

    val physicalTherapyAnalyticalReport =
        createPhysicalTherapyAnalyticalReport(
            physicalTherapyTechnicalReport,
            normalizedPhysicalTherapySession
        )

    return PhysicalTherapyReport(
        physicalTherapyAnalyticalReport,
        physicalTherapyTechnicalReport
    )
}

private fun PhysicalTherapySession.getDataset(
    physicalTherapyExerciseList: List<PhysicalTherapyExercise>
): List<Array<FloatArray>> {
    val physicalTherapyExerciseDataset: List<Array<FloatArray>> = physicalTherapyExerciseList.map { it.data }
    val physicalTherapySessionData: Array<FloatArray> = data
    return physicalTherapyExerciseDataset.toMutableList().also { it.add(physicalTherapySessionData) }
}

private fun normalizeDataset(dataset: List<Array<FloatArray>>): List<Array<FloatArray>> {
    val minArray = FloatArray(numberOfSensors)
    val maxArray = FloatArray(numberOfSensors)

    for (sensorIndex in 0 until numberOfSensors) {
        val sensorDataset = dataset
            .flatMap { it.asIterable() }
            .flatMap { it.asIterable() }
            .filterIndexed { index, _ -> index.toSensorIndex() == sensorIndex }

        minArray[sensorIndex] = sensorDataset.min() ?: throw Exception("Should not happen.")
        maxArray[sensorIndex] = sensorDataset.max() ?: throw Exception("Should not happen.")
    }

    return dataset.map { data ->
        data.map { row ->
            row.mapIndexed { index, value ->
                val sensorIndex = index.toSensorIndex()
                2f * (value - minArray[sensorIndex]) / (maxArray[sensorIndex] - minArray[sensorIndex]) - 1f
            }.toFloatArray()
        }.toTypedArray()
    }
}

private fun Int.toSensorIndex() = (this % (numberOfSensors * numberOfAxes)) / numberOfAxes

private fun getNormalizedPhysicalTherapyExerciseList(
    normalizedDataset: List<Array<FloatArray>>,
    physicalTherapyExerciseList: List<PhysicalTherapyExercise>
): List<PhysicalTherapyExercise> {
    return normalizedDataset
        .dropLast(1)
        .mapIndexed { index, data ->
            physicalTherapyExerciseList[index].copy(data = data)
        }
}

private fun getNormalizedPhysicalTherapySession(normalizedDataset: List<Array<FloatArray>>) =
    PhysicalTherapySession(normalizedDataset.last())

private suspend fun classify(
    physicalTherapyExerciseList: List<PhysicalTherapyExercise>,
    physicalTherapySession: PhysicalTherapySession,
    distanceFunction: (FloatArray, FloatArray) -> Float,
    localWeights: LocalWeights,
    lengthToleranceFactor: Float,
    overlappingFactor: Float,
    costThreshold: Float,
    onNextIteration: (iterationId: Int) -> Unit = {},
    onPhysicalTherapyExercisePatternFound: (pattern: PhysicalTherapyExercisePattern) -> Unit = {},
    onBestInIterationPhysicalTherapyExercisePatternFound: (pattern: PhysicalTherapyExercisePattern) -> Unit = {},
    onFinished: (timeInMilliseconds: Long) -> Unit = {}
): PhysicalTherapyTechnicalReport {
    return coroutineScope {

        val xArray = physicalTherapyExerciseList.map { it.data }
        val y = physicalTherapySession.data

        val alpha = lengthToleranceFactor.coerceIn(0.01f, 0.99f)
        val beta = overlappingFactor.coerceIn(0.01f, 0.99f)
        val gamma = costThreshold.coerceAtLeast(0f)

        val k = xArray.size
        val nArray = xArray.map { it.size }
        val yArray = Array(k) { y.map { it.clone() }.toTypedArray() }

        var iterationId = 0
        val patternList = mutableListOf<PhysicalTherapyExercisePattern>()
        val startTime = System.currentTimeMillis()

        while (true) {

            onNextIteration(iterationId++)

            val patternIndexedList: List<PhysicalTherapyExercisePatternIndexed> =
                Array(k) { index ->
                    async(bgDispatcher) {
                        val exercise = physicalTherapyExerciseList[index]

                        val xk = xArray[index]
                        val yk = yArray[index]

                        val report = subsequenceDynamicTimeWarpingOf(
                            xk,
                            yk,
                            distanceFunction,
                            localWeights
                        )

                        val cost = report.cost

                        report.copy(cost = cost / nArray[index])
                            .toPattern(exercise)
                            .also { onPhysicalTherapyExercisePatternFound(it) }
                            .toPatternIndexed(index)
                    }
                }.toList().awaitAll()

            val bestPatternIndexed = patternIndexedList.minBy { it.cost }

            if (bestPatternIndexed == null || bestPatternIndexed.cost >= gamma) break

            val minLength = (alpha * nArray[bestPatternIndexed.index]).roundToInt()

            val start = bestPatternIndexed.range.start
            val endInclusive = bestPatternIndexed.range.endInclusive
            val startApprox = ((1 - beta) * start + beta * endInclusive).roundToInt()
            val endInclusiveApprox = (beta * start + (1 - beta) * endInclusive).roundToInt()

            if (bestPatternIndexed.range.length >= minLength) {
                patternList.add(bestPatternIndexed.toPattern())
                for (yk in yArray) {
                    yk.fill(startApprox, endInclusiveApprox, Float.POSITIVE_INFINITY)
                }
            } else {
                val yk = yArray[bestPatternIndexed.index]
                yk.fill(startApprox, endInclusiveApprox, Float.POSITIVE_INFINITY)
            }

            onBestInIterationPhysicalTherapyExercisePatternFound(bestPatternIndexed.toPattern())
        }

        val endTime = System.currentTimeMillis()
        val timeTaken = endTime - startTime
        onFinished(timeTaken)

        PhysicalTherapyTechnicalReport(
            patternList,
            PhysicalTherapyPerformanceReport(timeTaken)
        )
    }
}

private fun SubsequenceDynamicTimeWarpingReport.toPattern(
    physicalTherapyExercise: PhysicalTherapyExercise
): PhysicalTherapyExercisePattern {
    return PhysicalTherapyExercisePattern(
        physicalTherapyExercise.type,
        physicalTherapyExercise.execution,
        PhysicalTherapyExercisePatternRange(
            start,
            endInclusive
        ),
        cost
    )
}

private fun Array<FloatArray>.fill(start: Int, endInclusive: Int, value: Float) {
    val slice = this.sliceArray(start..endInclusive)
    for (row in 0 until slice.size) {
        for (col in 0 until slice[row].size) {
            slice[row][col] = value
        }
    }
}

private fun createPhysicalTherapyAnalyticalReport(
    physicalTherapyTechnicalReport: PhysicalTherapyTechnicalReport,
    normalizedPhysicalTherapySession: PhysicalTherapySession
): PhysicalTherapyAnalyticalReport {
    /*
    Prepare data for physical therapy count report.
     */
    val totalCount = physicalTherapyTechnicalReport.patternList.size
    val correctCount = physicalTherapyTechnicalReport.patternList.filter { it.execution.isCorrect }.size

    /*
    Prepare data for physical therapy time report.
     */
    val totalTime =
        (normalizedPhysicalTherapySession.data.size.toFloat() / samplingFrequencyInHz.toFloat()).toLong()
    val activeTime =
        (physicalTherapyTechnicalReport.patternList.sumBy { it.range.length }.toFloat() / samplingFrequencyInHz.toFloat()).toLong()

    /*
    Prepare data for types.
     */
    val types = physicalTherapyTechnicalReport.patternList.groupBy { it.type }.mapValues { types ->

        /*
        For each type, prepare data for physical therapy exercise type count report.
         */
        val typeCount = types.value.size
        val typeCountPercentage = ((typeCount.toFloat() / totalCount.toFloat()) * 100f).roundToInt()

        /*
        For each type, prepare data for physical therapy exercise type time report.
         */
        val typeTime = (types.value.sumBy { it.range.length }.toFloat() / samplingFrequencyInHz.toFloat()).toLong()
        val typeTimePercentage = ((typeTime.toFloat() / activeTime.toFloat()) * 100f).roundToInt()

        /*
        For each type, prepare data for executions.
         */
        val executions = types.value.groupBy { it.execution }.mapValues { executions ->
            /*
            For each execution of each type, prepare data for physical therapy execution count report.
             */
            val executionCount = executions.value.size
            val executionCountPercentage = ((executionCount.toFloat() / typeCount.toFloat()) * 100f).roundToInt()

            /*
            For each execution of each type, prepare data for physical therapy execution time report.
             */
            val executionTime =
                (executions.value.sumBy { it.range.length }.toFloat() / samplingFrequencyInHz.toFloat()).toLong()
            val executionTimePercentage = ((executionTime.toFloat() / typeTime.toFloat()) * 100f).roundToInt()

            PhysicalTherapyExerciseExecutionReport(
                PhysicalTherapyExerciseExecutionCountReport(
                    executionCount,
                    executionCountPercentage
                ),
                PhysicalTherapyExerciseExecutionTimeReport(
                    executionTime,
                    executionTimePercentage
                )
            )
        }
        PhysicalTherapyExerciseTypeReport(
            PhysicalTherapyExerciseTypeCountReport(
                typeCount,
                typeCountPercentage
            ),
            PhysicalTherapyExerciseTypeTimeReport(
                typeTime,
                typeTimePercentage
            ),
            executions
        )
    }

    return PhysicalTherapyAnalyticalReport(
        count = PhysicalTherapyCountReport(
            total = totalCount,
            correct = correctCount
        ),
        time = PhysicalTherapyTimeReport(
            totalInMilliseconds = totalTime,
            activeInMilliseconds = activeTime
        ),
        types = types
    )
}