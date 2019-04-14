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
import io.github.melikabarzegaran.tavanyaralgorithm.algorithm.squaredEuclideanDistanceOf
import io.github.melikabarzegaran.tavanyaralgorithm.core.*
import io.github.melikabarzegaran.tavanyaralgorithm.core.report.PhysicalTherapyReport
import io.github.melikabarzegaran.tavanyaralgorithm.core.report.technical.performance.PhysicalTherapyPerformanceReport
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt
import kotlin.math.roundToLong

fun main() = runBlocking<Unit> {
    val result = optimizedHyperParametersOf(
        distanceFunctionList = listOf(::squaredEuclideanDistanceOf),
        localWeightsList = listOf(LocalWeights.SYMMETRIC),
        lengthToleranceFactorList = listOf(0.5f),
        overlappingFactorList = listOf(0.05f),
        costThresholdList = listOf(0.3f),
        timeThresholdInMilliseconds = 15000
    )

    when (result) {
        is OptimizedHyperParametersResult.Success -> {
            val hyperParameters = result.hyperParameters
            println("+===================================================================+")
            println("| Optimized hyper parameters                                        |")
            println("+===================================================================+")
            println("|")
            println("|---Distance function: ${hyperParameters.distanceFunction}")
            println("|---Local weights: ${hyperParameters.localWeights}")
            println("|---Length tolerance factor: ${hyperParameters.lengthToleranceFactor}")
            println("|---Overlapping factor: ${hyperParameters.overlappingFactor}")
            println("|---Cost threshold: ${hyperParameters.costThreshold}")
            println()
            println("+===================================================================+")
            println("| Measures and performance                                          |")
            println("+===================================================================+")
            println("|")
            println("|----Total micro f-score: ${result.measure}%")
            println("|----Average time: ${result.timeInMilliseconds}ms")
            println()
        }
        is OptimizedHyperParametersResult.Failure -> {
            println("+===================================================================+")
            println("| Optimized hyper parameters                                        |")
            println("+===================================================================+")
            println("|")
            println("|---Nothing found.")
            println()
            println("+===================================================================+")
            println("| Measures and performance                                          |")
            println("+===================================================================+")
            println("|")
            println("|---Nothing found.")
            println()
        }
    }
}

private suspend fun optimizedHyperParametersOf(
    distanceFunctionList: List<(FloatArray, FloatArray) -> Float>,
    localWeightsList: List<LocalWeights>,
    lengthToleranceFactorList: List<Float>,
    overlappingFactorList: List<Float>,
    costThresholdList: List<Float>,
    timeThresholdInMilliseconds: Long
): OptimizedHyperParametersResult {
    var bestHyperParameters: HyperParameters? = null
    var bestMeasure = 0f
    var bestTimeInMilliseconds = Long.MAX_VALUE

    for (distanceFunction in distanceFunctionList) {
        for (localWeights in localWeightsList) {
            for (lengthToleranceFactor in lengthToleranceFactorList) {
                for (overlappingFactor in overlappingFactorList) {
                    for (costThreshold in costThresholdList) {
                        println("+===================================================================+")
                        println("| Running physical therapy evaluator with hyper parameters          |")
                        println("+===================================================================+")
                        println("|")
                        println("|---Distance function: $distanceFunction")
                        println("|---Local weights: $localWeights")
                        println("|---Length tolerance factor: $lengthToleranceFactor")
                        println("|---Overlapping factor: $overlappingFactor")
                        println("|---Cost threshold: $costThreshold")
                        println()

                        val evaluationResult = evaluationOf(
                            distanceFunction,
                            localWeights,
                            lengthToleranceFactor,
                            overlappingFactor,
                            costThreshold
                        )

                        println("+===================================================================+")
                        println("| Measures and performance                                          |")
                        println("+===================================================================+")
                        println("|")
                        println("|----Total micro f-score: ${evaluationResult.measure}%")
                        println("|----Average time: ${evaluationResult.timeInMilliseconds}ms")
                        println()

                        if (evaluationResult.measure > bestMeasure && evaluationResult.timeInMilliseconds <= timeThresholdInMilliseconds) {
                            bestHyperParameters = HyperParameters(
                                distanceFunction,
                                localWeights,
                                lengthToleranceFactor,
                                overlappingFactor,
                                costThreshold
                            )
                            bestMeasure = evaluationResult.measure
                            bestTimeInMilliseconds = evaluationResult.timeInMilliseconds
                        }
                    }
                }
            }
        }
    }

    return bestHyperParameters?.let {
        OptimizedHyperParametersResult.Success(it, bestMeasure, bestTimeInMilliseconds)
    } ?: run {
        OptimizedHyperParametersResult.Failure
    }
}

private sealed class OptimizedHyperParametersResult {
    object Failure : OptimizedHyperParametersResult()

    data class Success(
        val hyperParameters: HyperParameters,
        val measure: Float,
        val timeInMilliseconds: Long
    ) : OptimizedHyperParametersResult()
}

private data class HyperParameters(
    val distanceFunction: (FloatArray, FloatArray) -> Float,
    val localWeights: LocalWeights,
    val lengthToleranceFactor: Float,
    val overlappingFactor: Float,
    val costThreshold: Float
)

private suspend fun evaluationOf(
    distanceFunction: (FloatArray, FloatArray) -> Float,
    localWeights: LocalWeights,
    lengthToleranceFactor: Float,
    overlappingFactor: Float,
    costThreshold: Float
): EvaluationResult {

    val typeAndExecutionConfusionMatrixList = mutableListOf<Array<IntArray>>()
    val typeConfusionMatrixList = mutableListOf<Array<IntArray>>()

    var totalTypeAndExecutionConfusionMatrix = Array(numberOfRowsInTypeAndExecutionConfusionMatrix) {
        IntArray(numberOfColsInTypeAndExecutionConfusionMatrix)
    }
    var totalTypeConfusionMatrix = Array(numberOfRowsInTypeConfusionMatrix) {
        IntArray(numberOfColsInTypeConfusionMatrix)
    }

    val performanceReportList = mutableListOf<PhysicalTherapyPerformanceReport>()

    for (subjectId in 0 until numberOfSubjects) {
        val confusionMatrixResult = confusionMatrixOf(
            subjectId,
            distanceFunction,
            localWeights,
            lengthToleranceFactor,
            overlappingFactor,
            costThreshold
        )
        val (typeAndExecutionConfusionMatrix, performanceReport) = confusionMatrixResult

        val typeConfusionMatrix = typeAndExecutionConfusionMatrix.toType()

        typeAndExecutionConfusionMatrixList += typeAndExecutionConfusionMatrix
        typeConfusionMatrixList += typeConfusionMatrix

        totalTypeAndExecutionConfusionMatrix += typeAndExecutionConfusionMatrix
        totalTypeConfusionMatrix += typeConfusionMatrix

        performanceReportList += performanceReport
    }

    val totalPerformanceReport = performanceReportList.average()

    val typeAndExecutionEvaluationReportList = mutableListOf<EvaluationReport>()
    val typeEvaluationReportList = mutableListOf<EvaluationReport>()
    for (subjectId in 0 until numberOfSubjects) {
        typeAndExecutionEvaluationReportList += evaluationReportOf(typeAndExecutionConfusionMatrixList[subjectId])
        typeEvaluationReportList += evaluationReportOf(typeConfusionMatrixList[subjectId])
    }

    val totalTypeAndExecutionEvaluationReport = evaluationReportOf(totalTypeAndExecutionConfusionMatrix)
    val totalTypeEvaluationReport = evaluationReportOf(totalTypeConfusionMatrix)

    println()
    for (subjectId in 0 until numberOfSubjects) {
        println("+===================================================================+")
        println("| Subject #$subjectId                                                        |")
        println("+===================================================================+")
        println()

        println("Confusion matrix (type and execution):")
        typeAndExecutionConfusionMatrixList[subjectId].print()
        println()

        println("Evaluation report (type and execution):")
        println(typeAndExecutionEvaluationReportList[subjectId])
        println()

        println("Confusion matrix (type):")
        typeConfusionMatrixList[subjectId].print()
        println()

        println("Evaluation report (type):")
        println(typeEvaluationReportList[subjectId])
        println()

        println("Performance:")
        println(performanceReportList[subjectId])
        println()
    }

    println("+===================================================================+")
    println("| Total                                                             |")
    println("+===================================================================+")

    println("Total confusion matrix (type, execution):")
    totalTypeAndExecutionConfusionMatrix.print()
    println()

    println("Total evaluation report (type, execution):")
    println(totalTypeAndExecutionEvaluationReport)
    println()

    println("Total confusion matrix (type):")
    totalTypeConfusionMatrix.print()
    println()

    println("Total evaluation report (type):")
    println(totalTypeEvaluationReport)
    println()

    println("Total performance:")
    println(totalPerformanceReport)
    println()

    return EvaluationResult(
        totalTypeAndExecutionEvaluationReport.micro.f1Score,
        totalPerformanceReport.timeInMilliseconds
    )
}

private data class EvaluationResult(
    val measure: Float,
    val timeInMilliseconds: Long
)

private fun Array<IntArray>.toType(): Array<IntArray> {
    return Array(numberOfRowsInTypeConfusionMatrix) { i ->
        IntArray(numberOfColsInTypeConfusionMatrix) { j ->
            val rowRange = if (i == 3) {
                9..9
            } else {
                i * 3..(i * 3 + 2)
            }
            val colRange = if (j == 3) {
                9..9
            } else {
                j * 3..(j * 3 + 2)
            }

            var sum = 0
            for (row in rowRange) {
                for (col in colRange) {
                    sum += this[row][col]
                }
            }
            sum
        }
    }
}

private operator fun Array<IntArray>.plus(other: Array<IntArray>): Array<IntArray> {
    return Array(this.size) { i ->
        IntArray(this[i].size) { j ->
            this[i][j] + other[i][j]
        }
    }
}

private fun List<PhysicalTherapyPerformanceReport>.average(): PhysicalTherapyPerformanceReport {
    val n = this.size.toFloat()

    return this.reduce { sum, physicalTherapyPerformanceReport -> sum + physicalTherapyPerformanceReport }
        .let {
            PhysicalTherapyPerformanceReport(
                (it.timeInMilliseconds.toFloat() / n).roundToLong()
            )
        }
}

private suspend fun confusionMatrixOf(
    subjectId: Int,
    distanceFunction: (FloatArray, FloatArray) -> Float,
    localWeights: LocalWeights,
    lengthToleranceFactor: Float,
    overlappingFactor: Float,
    costThreshold: Float
): ConfusionMatrixResult {
    /*
    Setup physical therapy exercise list.
     */
    val exerciseList = mutableListOf<PhysicalTherapyExercise>()
    for (typeId in 0 until numberOfExerciseTypes) {
        for (executionId in 0 until numberOfExerciseExecutions) {
            val path = "${basePath}exercise-subject$subjectId-type$typeId-execution$executionId.csv"
            val data = readData(path, hasTimeLabel = true)
            exerciseList += PhysicalTherapyExercise(
                physicalTherapyExerciseTypeList[typeId],
                physicalTherapyExerciseExecutionList[executionId],
                data
            )
        }
    }

    /*
    Setup physical therapy session list.
     */
    val sessionList = mutableListOf<PhysicalTherapySession>()
    for (typeId in 0 until numberOfSessionTypes) {
        for (setId in 0 until numberOfSessionSets) {
            val path = "${basePath}session-subject$subjectId-type$typeId-set$setId.csv"
            val data = readData(path, hasTimeLabel = true)
            sessionList += PhysicalTherapySession(data)
        }
    }

    /*
    Setup physical therapy session label list.
     */
    val sessionLabelList = mutableListOf<PhysicalTherapySessionLabel>()
    for (typeId in 0 until numberOfSessionTypes) {
        for (sessionId in 0 until numberOfSessionSets) {
            val path = "${basePath}session-label-subject$subjectId-type$typeId-set$sessionId.csv"
            val data = readLabelData(path)
            sessionLabelList += PhysicalTherapySessionLabel(data)
        }
    }

    /*
    Compute physical therapy report list.
     */
    val reportList = mutableListOf<PhysicalTherapyReport>()
    for ((sessionId, session) in sessionList.withIndex()) {
        reportList += session.evaluateUsing(exerciseList,
            distanceFunction,
            localWeights,
            lengthToleranceFactor,
            overlappingFactor,
            costThreshold,
            onNextIteration = { iterationNumber -> println("Subject #$subjectId, Session #$sessionId, Iteration #$iterationNumber...") }
        )
    }

    /*
    Setup confusion matrix.
     */
    val confusionMatrix = Array(numberOfRowsInTypeAndExecutionConfusionMatrix) {
        IntArray(numberOfColsInTypeAndExecutionConfusionMatrix) { 0 }
    }

    /*
    Fill confusion matrix.
     */
    for (sessionId in 0 until numberOfSessions) {
        val report = reportList[sessionId]
        val patternList = report.technical.patternList

        val sessionLabel = sessionLabelList[sessionId]
        val sessionLabelData = sessionLabel.data

        for (pattern in patternList) {
            for (sessionLabelPart in sessionLabelData) {
                val patternStart = pattern.range.start
                val patternEnd = pattern.range.endInclusive
                val patternMedian = ((patternStart + patternEnd).toFloat() / 2f).toInt()
                val sessionLabelPartRange = sessionLabelPart[2]..sessionLabelPart[3]

                if (patternMedian in sessionLabelPartRange) {
                    val sessionTypeId = sessionLabelPart[0]
                    val sessionExecutionId = sessionLabelPart[1]
                    val rowIndex = if (sessionTypeId == -1 && sessionExecutionId == -1) {
                        numberOfRowsInTypeAndExecutionConfusionMatrix - 1
                    } else {
                        sessionTypeId * 3 + sessionExecutionId
                    }

                    val patternTypeId = pattern.type.id
                    val patternExecutionId = pattern.execution.id
                    val colIndex = patternTypeId * 3 + patternExecutionId

                    confusionMatrix[rowIndex][colIndex]++
                }
            }
        }
    }

    /*
    Fill confusion matrix with missed detections.
     */
    for (row in 0 until numberOfRowsInTypeAndExecutionConfusionMatrix - 1) {
        val numberOftypeAndExecutionDetections = confusionMatrix[row].sum()
        confusionMatrix[row][idleIntervalColIndex] = numberOfRepetitions - numberOftypeAndExecutionDetections
    }

    /*
    Add true idle interval count to confusion matrix (estimating).
     */
    var idleIntervalCount = 0
    for (sessionLabel in sessionLabelList) {
        val idleIntervalLengthInSession = sessionLabel
            .data
            .filter { it[0] == -1 && it[1] == -1 }
            .map { it[3] - it[2] + 1 }
            .sum()

        val averageExerciseLengthInSession = sessionLabel
            .data
            .first()
            .let { it[3] - it[2] + 1 }

        idleIntervalCount += (idleIntervalLengthInSession.toFloat() / averageExerciseLengthInSession.toFloat()).roundToInt()
    }
    val falseIdleIntervalCount = confusionMatrix[idleIntervalRowIndex].sum()
    val trueIdleIntervalCount = idleIntervalCount - falseIdleIntervalCount
    confusionMatrix[idleIntervalRowIndex][idleIntervalColIndex] = trueIdleIntervalCount

    /*
    Compute average physical therapy performance report.
     */
    val performanceReportList = mutableListOf<PhysicalTherapyPerformanceReport>()
    for (report in reportList) {
        performanceReportList += report.technical.performance
    }
    val averagePerformanceReport = performanceReportList.average()

    /*
    Return confusion matrix, and physical therapy performance report.
     */
    return ConfusionMatrixResult(
        confusionMatrix,
        averagePerformanceReport
    )
}

private data class PhysicalTherapySessionLabel(val data: Array<IntArray>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PhysicalTherapySessionLabel

        if (!data.contentDeepEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        return data.contentDeepHashCode()
    }
}

private data class ConfusionMatrixResult(
    val confusionMatrix: Array<IntArray>,
    val performanceReport: PhysicalTherapyPerformanceReport
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConfusionMatrixResult

        if (!confusionMatrix.contentDeepEquals(other.confusionMatrix)) return false
        if (performanceReport != other.performanceReport) return false

        return true
    }

    override fun hashCode(): Int {
        var result = confusionMatrix.contentDeepHashCode()
        result = 31 * result + performanceReport.hashCode()
        return result
    }
}

private fun evaluationReportOf(confusionMatrix: Array<IntArray>): EvaluationReport {
    val (macro, binary) = macroEvaluationReportOf(confusionMatrix)
    val micro = microEvaluationReportOf(confusionMatrix)
    return EvaluationReport(macro, micro, binary)
}

private fun macroEvaluationReportOf(confusionMatrix: Array<IntArray>): Pair<BinaryEvaluationReport, List<BinaryEvaluationReport>> {
    val n = confusionMatrix.size

    val binaryEvaluationReportList = mutableListOf<BinaryEvaluationReport>()
    for (row in 0 until n) {
        binaryEvaluationReportList += binaryEvaluationReportOf(confusionMatrix, row)
    }

    return binaryEvaluationReportList.macroAverage() to binaryEvaluationReportList.toList()
}

private fun binaryEvaluationReportOf(confusionMatrix: Array<IntArray>, pIndex: Int): BinaryEvaluationReport {
    val notation = notationOf(confusionMatrix, pIndex)
    return binaryEvaluationReportOf(notation)
}

private fun notationOf(confusionMatrix: Array<IntArray>, pIndex: Int): Notation {
    val tp = confusionMatrix[pIndex][pIndex]
    val fn = confusionMatrix[pIndex].sum() - tp
    val fp = confusionMatrix.map { it[pIndex] }.sum() - tp
    val tn = confusionMatrix.flatMap { it.asIterable() }.sum() - (tp + fp + fn)
    return Notation(tp, fn, fp, tn)
}

private data class Notation(
    val tp: Int,
    val fn: Int,
    val fp: Int,
    val tn: Int
) {
    operator fun plus(other: Notation): Notation {
        return Notation(
            tp + other.tp,
            fn + other.fn,
            fp + other.fp,
            tn + other.tn
        )
    }
}

private fun binaryEvaluationReportOf(notation: Notation): BinaryEvaluationReport {
    val (tp, fn, fp, tn) = notation
    val p: Int = tp + fn
    val n: Int = tn + fp
    val accuracy: Float = (tp + tn).toFloat() / (p + n).toFloat() * 100f
    val errorRate: Float = (fn + fp).toFloat() / (p + n).toFloat() * 100f
    val precision: Float = tp.toFloat() / (tp + fp).toFloat() * 100f
    val recall: Float = tp.toFloat() / p.toFloat() * 100f
    val specificity: Float = tn.toFloat() / n.toFloat() * 100f
    val falseAlarmRate: Float = 100 - specificity
    val missedDetectionRate: Float = 100 - recall
    val f1Score: Float = 2f * precision * recall / (precision + recall)

    return BinaryEvaluationReport(
        accuracy,
        errorRate,
        precision,
        recall,
        specificity,
        falseAlarmRate,
        missedDetectionRate,
        f1Score
    )
}

private data class BinaryEvaluationReport(
    val accuracy: Float,
    val errorRate: Float,
    val precision: Float,
    val recall: Float,
    val specificity: Float,
    val falseAlarmRate: Float,
    val missedDetectionRate: Float,
    val f1Score: Float
) {
    operator fun plus(other: BinaryEvaluationReport): BinaryEvaluationReport {
        return BinaryEvaluationReport(
            accuracy + other.accuracy,
            errorRate + other.errorRate,
            precision + other.precision,
            recall + other.recall,
            specificity + other.specificity,
            falseAlarmRate + other.falseAlarmRate,
            missedDetectionRate + other.missedDetectionRate,
            f1Score + other.f1Score
        )
    }
}

private fun List<BinaryEvaluationReport>.macroAverage(): BinaryEvaluationReport {
    val n = size.toFloat()
    return this
        .reduce { sum, binaryEvaluationReport -> sum + binaryEvaluationReport }
        .let {
            it.copy(
                accuracy = it.accuracy / n,
                errorRate = it.errorRate / n,
                precision = it.precision / n,
                recall = it.recall / n,
                specificity = it.specificity / n,
                falseAlarmRate = it.falseAlarmRate / n,
                missedDetectionRate = it.missedDetectionRate / n,
                f1Score = it.f1Score / n
            )
        }
}

private fun microEvaluationReportOf(confusionMatrix: Array<IntArray>): BinaryEvaluationReport {
    val n = confusionMatrix.size

    val notationList = mutableListOf<Notation>()
    for (row in 0 until n) {
        notationList += notationOf(confusionMatrix, row)
    }

    val notation = notationList.reduce { sum, notation -> sum + notation }

    return binaryEvaluationReportOf(notation)
}

private data class EvaluationReport(
    val macro: BinaryEvaluationReport,
    val micro: BinaryEvaluationReport,
    val binaryList: List<BinaryEvaluationReport>
)

private const val basePath = "src/test/resources/dataset/"

private const val numberOfSubjects = 3
private const val numberOfExerciseTypes = 3
private const val numberOfExerciseExecutions = 3
private const val numberOfSessionTypes = 3
private const val numberOfSessionSets = 2
private const val numberOfExercises = numberOfExerciseTypes * numberOfExerciseExecutions
private const val numberOfSessions = numberOfSessionTypes * numberOfSessionSets
private const val numberOfRowsInTypeAndExecutionConfusionMatrix = numberOfExercises + 1
private const val numberOfColsInTypeAndExecutionConfusionMatrix = numberOfExercises + 1
private const val numberOfRowsInTypeConfusionMatrix = numberOfExerciseTypes + 1
private const val numberOfColsInTypeConfusionMatrix = numberOfExerciseTypes + 1
private const val numberOfRepetitions = 10
private const val idleIntervalRowIndex = numberOfRowsInTypeAndExecutionConfusionMatrix - 1
private const val idleIntervalColIndex = numberOfColsInTypeAndExecutionConfusionMatrix - 1

private val physicalTherapyExerciseTypeList = listOf(
    PhysicalTherapyExerciseType(0, "Left Elbow Flexion/Extension"),
    PhysicalTherapyExerciseType(1, "Left Shoulder Abduction/Adduction"),
    PhysicalTherapyExerciseType(2, "Left Shoulder Flexion/Extension")
)

private val physicalTherapyExerciseExecutionList = listOf(
    PhysicalTherapyExerciseExecution(0, "Correct", true),
    PhysicalTherapyExerciseExecution(1, "Fast", false),
    PhysicalTherapyExerciseExecution(2, "Low Amplitude", false)
)