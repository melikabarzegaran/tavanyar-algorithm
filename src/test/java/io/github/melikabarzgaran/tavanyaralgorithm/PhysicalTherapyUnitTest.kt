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

import io.github.melikabarzegaran.tavanyaralgorithm.core.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.BufferedReader
import java.io.FileReader

class PhysicalTherapyUnitTest {

    @Test
    fun `given one subject, and one physical therapy session, when evaluating, then detect and evaluate physical therapy exercises correctly and report accordingly`() =
        runBlocking<Unit> {
            /*
            Given
             */
            val subject = 3
            val type = 1
            val set = 1

            val basePath = "src/test/resources/dataset/"

            val numberOfTypes = 3
            val numberOfExecutions = 3
            val typeDescriptions = mapOf(
                0 to "Left Elbow Flexion/Extension",
                1 to "Left Shoulder Abduction/Adduction",
                2 to "Left Shoulder Flexion/Extension"
            )
            val executionDescriptions = mapOf(
                0 to "Correct",
                1 to "Fast",
                2 to "Low Amplitude"
            )
            val physicalTherapyExerciseList = mutableListOf<PhysicalTherapyExercise>()
            for (typeNumber in 1..numberOfTypes) {
                for (executionNumber in 1..numberOfExecutions) {
                    val path = "${basePath}candidate-subject$subject-type$typeNumber-execution$executionNumber.csv"
                    val data = readData(path, hasTimeLabel = true)

                    val typeId = typeNumber - 1
                    val typeDescription = typeDescriptions[typeId] ?: throw Exception()
                    val physicalTherapyExerciseType =
                        PhysicalTherapyExerciseType(
                            typeId,
                            typeDescription
                        )

                    val executionId = executionNumber - 1
                    val executionDescription = executionDescriptions[executionId] ?: throw Exception()
                    val physicalTherapyExerciseExecution =
                        PhysicalTherapyExerciseExecution(
                            executionId,
                            executionDescription,
                            executionNumber == 1
                        )

                    physicalTherapyExerciseList += PhysicalTherapyExercise(
                        physicalTherapyExerciseType,
                        physicalTherapyExerciseExecution,
                        data
                    )
                }
            }

            val path = "${basePath}query-subject$subject-type$type-$set.csv"
            val data = readData(path, hasTimeLabel = true)
            val physicalTherapySession =
                PhysicalTherapySession(data)

            /*
            When
             */
            val report = physicalTherapySession.evaluateUsing(physicalTherapyExerciseList,
                onNextIteration = { iterationNumber ->
                    println("Iteration #$iterationNumber...")
                },
                onPhysicalTherapyExercisePatternFound = { _, _ ->
                    println("Pattern found.")
                },
                onBestInIterationPhysicalTherapyExercisePatternChosen = { pattern, _ ->
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

            /*
            Analytical
             */
            val analyticalReport = report.analytical

            assertEquals(15, analyticalReport.count.total)
            assertEquals(5, analyticalReport.count.correct)
            assertEquals(10, analyticalReport.count.wrong)
            assertEquals(33, analyticalReport.count.correctPercentage)
            assertEquals(67, analyticalReport.count.wrongPercentage)

            assertTrue(analyticalReport.time.totalInMilliseconds in 140L..160L)
            assertTrue(analyticalReport.time.inactiveInMilliseconds in 40L..90L)
            assertTrue(analyticalReport.time.activeInMilliseconds in 60L..110L)
            assertEquals(100, analyticalReport.time.activePercentage + analyticalReport.time.inactivePercentage)

            /*
            Types
             */
            val types = analyticalReport.types
            assertEquals(1, types.size)
            assertTrue(types.all { it.key.id == 0 })

            val physicalTherapyExerciseType = PhysicalTherapyExerciseType(
                0,
                typeDescriptions[0] ?: throw Exception("Should not happen.")
            )
            val typeReport = types[physicalTherapyExerciseType] ?: throw Exception("Should not happen.")

            assertEquals(15, typeReport.count.total)
            assertEquals(100, typeReport.count.totalPercentage)
            assertTrue(typeReport.time.totalInMilliseconds in 40L..90L)
            assertEquals(100, typeReport.time.totalPercentage)

            /*
            Executions
             */
            val executions = typeReport.executions
            assertEquals(3, executions.size)
            assertTrue(executions.all { it.key.id in 0..2 })

            val physicalTherapyExerciseExecution1 = PhysicalTherapyExerciseExecution(
                0,
                executionDescriptions[0] ?: throw Exception("Should not happen."),
                true
            )
            val executionReport1 = executions[physicalTherapyExerciseExecution1]
                ?: throw Exception("Should not happen.")
            assertEquals(5, executionReport1.count.total)
            assertEquals(33, executionReport1.count.totalPercentage)

            val physicalTherapyExerciseExecution2 = PhysicalTherapyExerciseExecution(
                1,
                executionDescriptions[1] ?: throw Exception("Should not happen."),
                false
            )
            val executionReport2 = executions[physicalTherapyExerciseExecution2]
                ?: throw Exception("Should not happen.")
            assertEquals(5, executionReport2.count.total)
            assertEquals(33, executionReport2.count.totalPercentage)

            val physicalTherapyExerciseExecution3 = PhysicalTherapyExerciseExecution(
                2,
                executionDescriptions[2] ?: throw Exception("Should not happen."),
                false
            )
            val executionReport3 = executions[physicalTherapyExerciseExecution3]
                ?: throw Exception("Should not happen.")
            assertEquals(5, executionReport3.count.total)
            assertEquals(33, executionReport3.count.totalPercentage)

            assertTrue(executionReport1.time.totalInMilliseconds > executionReport3.time.totalInMilliseconds)
            assertTrue(executionReport3.time.totalInMilliseconds > executionReport2.time.totalInMilliseconds)

            assertTrue(executionReport1.time.totalPercentage > executionReport3.time.totalPercentage)
            assertTrue(executionReport3.time.totalPercentage > executionReport2.time.totalPercentage)

            /*
            Technical
             */
            val technicalReport = report.technical

            /*
            Patterns
             */
            assertTrue(technicalReport.patternList.all { it.type.id == 0 })
            assertTrue(technicalReport.patternList.all { it.execution.id in 0..2 })
            assertEquals(15, technicalReport.patternList.size)
            assertEquals(5, technicalReport.patternList.filter { it.execution.id == 0 }.count())
            assertEquals(5, technicalReport.patternList.filter { it.execution.id == 1 }.count())
            assertEquals(5, technicalReport.patternList.filter { it.execution.id == 2 }.count())

            /*
            Performance
             */
            assertEquals(
                technicalReport.performance.calculations.total,
                technicalReport.performance.calculations.prunedOut
                        + technicalReport.performance.calculations.notPrunedOut
            )
        }
}

private fun readData(
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