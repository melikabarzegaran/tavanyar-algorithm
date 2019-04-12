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

import binByFloat
import io.github.melikabarzegaran.tavanyaralgorithm.core.*
import io.github.melikabarzegaran.tavanyaralgorithm.core.report.PhysicalTherapyReport
import kotlinx.coroutines.runBlocking
import org.junit.Test

class RobustnessEvaluationUnitTest {

    @Test
    fun leaveOneExerciseOutEvaluationTest() = runBlocking {
        val subjectIdList = listOf(0, 1, 2)
        val sessionTypeIdList = listOf(0, 1, 2)
        val exerciseTypeList = listOf(
            PhysicalTherapyExerciseType(0, "Left Elbow Flexion/Extension"),
            PhysicalTherapyExerciseType(1, "Left Shoulder Abduction/Adduction"),
            PhysicalTherapyExerciseType(2, "Left Shoulder Flexion/Extension")
        )
        val exerciseExecutionList = listOf(
            PhysicalTherapyExerciseExecution(0, "Correct", true)
        )
        val sessionSetIdList = listOf(0, 1)

        val totalReportList = mutableListOf<PhysicalTherapyReport>()
        val matrix = Array(subjectIdList.size) { i ->
            IntArray(exerciseTypeList.size) { j ->
                val reportList = mutableListOf<PhysicalTherapyReport>()
                for (k in sessionSetIdList) {
                    reportList += reportOf(
                        subjectIdList[i],
                        exerciseTypeList - exerciseTypeList[j],
                        exerciseExecutionList,
                        sessionTypeIdList[j],
                        sessionSetIdList[k]
                    )
                }
                totalReportList += reportList
                reportList.map { it.analytical.count.total }.sum()
            }
        }

        totalReportList.flatMap { it.technical.patternList.asIterable() }
            .binByFloat(binSize = 0.1f, valueSelector = { it.cost }, rangeStart = 0f)
            .also {
                println("bins:")
                it.forEach { bin -> println(bin) }
                println()
            }
            .map { it.range to it.value.size }
            .also {
                println("bins count:")
                it.forEach { binCount -> println(binCount) }
                println()
            }

        println("robustness matrix:")
        matrix.print()
    }

    private suspend fun reportOf(
        subjectId: Int,
        physicalTherapyExerciseTypeList: List<PhysicalTherapyExerciseType>,
        physicalTherapyExerciseExecutionList: List<PhysicalTherapyExerciseExecution>,
        sessionTypeId: Int,
        sessionSetId: Int,
        costThreshold: Float = 0.4f
    ): PhysicalTherapyReport {
        /*
        Given
         */
        val basePath = "src/test/resources/dataset/"

        val physicalTherapyExerciseList = mutableListOf<PhysicalTherapyExercise>()
        for (exerciseType in physicalTherapyExerciseTypeList) {
            for (exerciseExecution in physicalTherapyExerciseExecutionList) {
                val exerciseTypeId = exerciseType.id
                val exerciseExecutionId = exerciseExecution.id
                val path =
                    "${basePath}exercise-subject$subjectId-type$exerciseTypeId-execution$exerciseExecutionId.csv".also {
                        println("exercise: $it")
                    }
                val data = readData(path, hasTimeLabel = true)
                physicalTherapyExerciseList += PhysicalTherapyExercise(exerciseType, exerciseExecution, data)
            }
        }

        val path = "${basePath}session-subject$subjectId-type$sessionTypeId-set$sessionSetId.csv".also {
            println("session: $it")
            println()
        }
        val data = readData(path, hasTimeLabel = true)
        val physicalTherapySession = PhysicalTherapySession(data)

        /*
        When
         */
        val report = physicalTherapySession.evaluateUsing(physicalTherapyExerciseList, costThreshold = costThreshold,
            onNextIteration = { iterationId ->
                println("Iteration #$iterationId...")
            },
            onPhysicalTherapyExercisePatternFound = { _, _ ->
                println("Pattern found.")
            },
            onBestInIterationPhysicalTherapyExercisePatternChosen = { _, _ ->
                println("Best pattern chosen.")
            },
            onFinished = { timeInMilliseconds ->
                println("Time taken is ${timeInMilliseconds}ms.")
                println()
            }
        )

        /*
        Then
         */
        buildString {
            appendln("+========================================================================================+")
            appendln("|                                         Report                                         |")
            appendln("+========================================================================================+")
            with(report.analytical.count) {
                appendln("|")
                appendln("|---count: $total")
                appendln("|   |---correct: $correct ($correctPercentage%)")
                appendln("|   |---wrong: $wrong ($wrongPercentage%)")
                null
            }
            with(report.analytical.time) {
                appendln("|")
                appendln("|---time: ${totalInMilliseconds}ms")
                appendln("|   |---active: ${activeInMilliseconds}ms ($activePercentage%)")
                appendln("|   |---inactive: ${inactiveInMilliseconds}ms ($inactivePercentage%)")
                null
            }
            @Suppress("NAME_SHADOWING")
            for ((type, typeReport) in report.analytical.types.toList().sortedBy { it.first.id }) {
                appendln("|")
                appendln("|---type(${type.id},${type.description}):")
                appendln("|   |")
                appendln("|   |---count: ${typeReport.count.total} (${typeReport.count.totalPercentage}%)")
                appendln("|   |---time: ${typeReport.time.totalInMilliseconds}ms (${typeReport.time.totalPercentage}%)")

                for ((execution, executionReport) in typeReport.executions.toList().sortedBy { it.first.id }) {
                    appendln("|   |")
                    appendln("|   |---execution(${execution.id}, ${execution.description}):")
                    appendln("|   |   |---count: ${executionReport.count.total} (${executionReport.count.totalPercentage}%)")
                    appendln("|   |   |---time: ${executionReport.time.totalInMilliseconds}ms (${executionReport.time.totalPercentage}%)")
                }
            }
            with(report.technical) {
                appendln("|")
                appendln("|---performance:")
                appendln("|   |")
                appendln("|   |---calculations:")
                appendln("|   |   |---pruned out: ${performance.calculations.prunedOut}")
                appendln("|   |   |---not pruned out: ${performance.calculations.notPrunedOut}")
                appendln("|   |   |---total: ${performance.calculations.total}")
                appendln("|   |   |---gain: ${performance.calculations.gainPercentage}%")
                appendln("|   |")
                appendln("|   |---time: ${performance.time.totalInMilliseconds}ms")
                appendln("|")
                null
            }
            appendln("+========================================================================================+")
        }.also { println(it) }

        return report
    }
}