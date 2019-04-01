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

import io.github.melikabarzegaran.tavanyaralgorithm.algorithm.*
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
import io.github.melikabarzegaran.tavanyaralgorithm.core.report.technical.pattern.PhysicalTherapyExercisePatternRange
import io.github.melikabarzegaran.tavanyaralgorithm.core.report.technical.performance.PhysicalTherapyCalculationsPerformanceReport
import io.github.melikabarzegaran.tavanyaralgorithm.core.report.technical.performance.PhysicalTherapyPerformanceReport
import io.github.melikabarzegaran.tavanyaralgorithm.core.report.technical.performance.PhysicalTherapyTimePerformanceReport
import io.github.melikabarzegaran.tavanyaralgorithm.util.bgDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.roundToInt

suspend fun PhysicalTherapySession.evaluateUsing(
    physicalTherapyExerciseList: List<PhysicalTherapyExercise>,
    distanceFunction: (FloatArray, FloatArray) -> Float = ::euclideanDistanceOf,
    localWeights: LocalWeights = LocalWeights.SYMMETRIC,
    globalConstraintWidthFactor: Float = 0.1f,
    generalizationStrategy: GeneralizationStrategy = GeneralizationStrategy.DEPENDANT,
    downSamplingStep: Int = 10,
    lengthToleranceFactor: Float = 0.25f,
    interpolationStrategy: InterpolationStrategy = InterpolationStrategy.TO_SMALLER,
    lowerBoundingRadius: Float = 0.1f,
    costThreshold: Float = 0.5f,
    onNextIteration: (iterationId: Int) -> Unit = {},
    onPhysicalTherapyExercisePatternFound: (pattern: PhysicalTherapyExercisePattern, calculations: PhysicalTherapyCalculationsPerformanceReport) -> Unit = { _, _ -> },
    onBestInIterationPhysicalTherapyExercisePatternChosen: (pattern: PhysicalTherapyExercisePattern, calculations: PhysicalTherapyCalculationsPerformanceReport) -> Unit = { _, _ -> },
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
        globalConstraintWidthFactor,
        generalizationStrategy,
        downSamplingStep,
        lengthToleranceFactor,
        interpolationStrategy,
        lowerBoundingRadius,
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
    globalConstraintWidthFactor: Float,
    generalizationStrategy: GeneralizationStrategy,
    downSamplingStep: Int,
    lengthToleranceFactor: Float,
    interpolationStrategy: InterpolationStrategy,
    lowerBoundingRadius: Float,
    costThreshold: Float,
    onNextIteration: (iterationId: Int) -> Unit = {},
    onPhysicalTherapyExercisePatternFound: (pattern: PhysicalTherapyExercisePattern, calculations: PhysicalTherapyCalculationsPerformanceReport) -> Unit = { _, _ -> },
    onBestInIterationPhysicalTherapyExercisePatternFound: (pattern: PhysicalTherapyExercisePattern, calculations: PhysicalTherapyCalculationsPerformanceReport) -> Unit = { _, _ -> },
    onFinished: (timeInMilliseconds: Long) -> Unit = {}
): PhysicalTherapyTechnicalReport {
    return coroutineScope {

        var iterationId = 0
        val physicalTherapyExercisePatternList = mutableListOf<PhysicalTherapyExercisePattern>()
        var numberOfPrunedOutCalculations = 0L
        var numberOfNotPrunedOutCalculations = 0L
        val startTime = System.currentTimeMillis()
        val timeTaken: Long

        while (true) {
            /*
            Start the iteration.
            Notify observers of iteration id.
             */
            onNextIteration(iterationId++)

            /*
            Search for each physical therapy exercise in the physical therapy session.
            Gather physical therapy search reports.
            Notify observers of each search report.
             */
            val physicalTherapySearchReportList: List<PhysicalTherapySearchReport> =
                Array(physicalTherapyExerciseList.size) { index ->
                    async(bgDispatcher) {
                        val physicalTherapyExercise = physicalTherapyExerciseList[index]
                        val subsequenceDynamicTimeWarpingReport =
                            subsequenceDynamicTimeWarpingOf(
                                physicalTherapyExercise.data,
                                physicalTherapySession.data,
                                distanceFunction,
                                localWeights,
                                globalConstraintWidthFactor,
                                generalizationStrategy,
                                downSamplingStep,
                                lengthToleranceFactor,
                                interpolationStrategy,
                                lowerBoundingRadius
                            )
                        PhysicalTherapySearchReport.of(
                            physicalTherapyExercise,
                            subsequenceDynamicTimeWarpingReport
                        ).also {
                            onPhysicalTherapyExercisePatternFound(
                                it.pattern,
                                it.performance
                            )
                        }
                    }
                }.toList().awaitAll()

            /*
            Choose the best physical therapy exercise pattern found in searches of the iteration.
             */
            val bestPhysicalTherapyExercisePattern = physicalTherapySearchReportList.minBy { it.pattern.cost }?.pattern

            /*
            If there is no best physical therapy exercise pattern or it doesn't satisfy necessary conditions,
            then finish iterating and notify observers of the time taken.
             */
            if (bestPhysicalTherapyExercisePattern == null || bestPhysicalTherapyExercisePattern.cost > costThreshold) {
                val endTime = System.currentTimeMillis()
                timeTaken = endTime - startTime
                onFinished(timeTaken)
                break
            }

            /*
            Otherwise, add the best physical therapy exercise pattern to the list.
             */
            physicalTherapyExercisePatternList.add(bestPhysicalTherapyExercisePattern)

            /*
            Be sure that we won't be searching the same part again in the next iterations.
             */
            val physicalTherapySessionPart =
                physicalTherapySession.data.sliceArray(bestPhysicalTherapyExercisePattern.range.start..bestPhysicalTherapyExercisePattern.range.endInclusive)
            for (row in 0 until physicalTherapySessionPart.size) {
                for (col in 0 until physicalTherapySessionPart[row].size) {
                    physicalTherapySessionPart[row][col] = Float.POSITIVE_INFINITY
                }
            }

            /*
            Notify observers of the best physical therapy exercise pattern found in the iteration.
             */
            numberOfPrunedOutCalculations += physicalTherapySearchReportList
                .sumBy { it.performance.prunedOut.toInt() }

            numberOfNotPrunedOutCalculations += physicalTherapySearchReportList
                .sumBy { it.performance.notPrunedOut.toInt() }

            val calculations =
                PhysicalTherapyCalculationsPerformanceReport(
                    numberOfPrunedOutCalculations,
                    numberOfNotPrunedOutCalculations
                )

            onBestInIterationPhysicalTherapyExercisePatternFound(bestPhysicalTherapyExercisePattern, calculations)
        }

        /*
        Return a technical report containing all the physical therapy exercise patterns found in the physical therapy session.
         */
        PhysicalTherapyTechnicalReport(
            physicalTherapyExercisePatternList,
            PhysicalTherapyPerformanceReport(
                PhysicalTherapyCalculationsPerformanceReport(
                    numberOfPrunedOutCalculations,
                    numberOfNotPrunedOutCalculations
                ),
                PhysicalTherapyTimePerformanceReport(
                    timeTaken
                )
            )
        )
    }
}

private data class PhysicalTherapySearchReport(
    val pattern: PhysicalTherapyExercisePattern,
    val performance: PhysicalTherapyCalculationsPerformanceReport
) {
    companion object {
        fun of(
            physicalTherapyExercise: PhysicalTherapyExercise,
            subsequenceDynamicTimeWarpingReport: SubsequenceDynamicTimeWarpingReport
        ): PhysicalTherapySearchReport {
            val physicalTherapyExercisePattern =
                PhysicalTherapyExercisePattern(
                    physicalTherapyExercise.type,
                    physicalTherapyExercise.execution,
                    PhysicalTherapyExercisePatternRange(
                        subsequenceDynamicTimeWarpingReport.start,
                        subsequenceDynamicTimeWarpingReport.endInclusive
                    ),
                    subsequenceDynamicTimeWarpingReport.cost
                )

            val physicalTherapyCalculationsPerformanceReport =
                PhysicalTherapyCalculationsPerformanceReport(
                    subsequenceDynamicTimeWarpingReport.numberOfPrunedOutCalculations,
                    subsequenceDynamicTimeWarpingReport.numberOfNotPrunedOutCalculations
                )

            return PhysicalTherapySearchReport(
                physicalTherapyExercisePattern,
                physicalTherapyCalculationsPerformanceReport
            )
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