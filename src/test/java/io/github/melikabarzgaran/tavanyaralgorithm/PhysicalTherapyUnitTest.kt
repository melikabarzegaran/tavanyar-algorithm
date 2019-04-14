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

class PhysicalTherapyUnitTest {

    @Test
    fun `given one subject, and one physical therapy session, when evaluating, then detect and evaluate physical therapy exercises correctly and report accordingly`() =
        runBlocking<Unit> {
            /*
            Given
             */
            val basePath = "src/test/resources/dataset/"

            val subjectId = 2
            val numberOfExerciseTypes = 3
            val numberOfExerciseExecutions = 3
            val physicalTherapyExerciseTypeList = listOf(
                PhysicalTherapyExerciseType(0, "Left Elbow Flexion/Extension"),
                PhysicalTherapyExerciseType(1, "Left Shoulder Abduction/Adduction"),
                PhysicalTherapyExerciseType(2, "Left Shoulder Flexion/Extension")
            )
            val physicalTherapyExerciseExecutionList = listOf(
                PhysicalTherapyExerciseExecution(0, "Correct", true),
                PhysicalTherapyExerciseExecution(1, "Fast", false),
                PhysicalTherapyExerciseExecution(2, "Low Amplitude", false)
            )
            val physicalTherapyExerciseList = mutableListOf<PhysicalTherapyExercise>()
            for (exerciseTypeId in 0 until numberOfExerciseTypes) {
                for (exerciseExecutionId in 0 until numberOfExerciseExecutions) {
                    val path =
                        "${basePath}exercise-subject$subjectId-type$exerciseTypeId-execution$exerciseExecutionId.csv"
                    val data = readData(path, hasTimeLabel = true)
                    physicalTherapyExerciseList += PhysicalTherapyExercise(
                        physicalTherapyExerciseTypeList[exerciseTypeId],
                        physicalTherapyExerciseExecutionList[exerciseExecutionId],
                        data
                    )
                }
            }

            val sessionTypeId = 0
            val sessionSetId = 0
            val path = "${basePath}session-subject$subjectId-type$sessionTypeId-set$sessionSetId.csv"
            val data = readData(path, hasTimeLabel = true)
            val physicalTherapySession = PhysicalTherapySession(data)

            /*
            When
             */
            val report = physicalTherapySession.evaluateUsing(physicalTherapyExerciseList,
                onNextIteration = { iterationId ->
                    println("Iteration #$iterationId...")
                },
                onPhysicalTherapyExercisePatternFound = { _ ->
                    println("Pattern found.")
                },
                onBestInIterationPhysicalTherapyExercisePatternChosen = { _ ->
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
                    appendln("|   |---time: ${performance.timeInMilliseconds}ms")
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

            val typeReport = types[physicalTherapyExerciseTypeList[0]] ?: throw Exception("Should not happen.")

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

            val executionReport1 = executions[physicalTherapyExerciseExecutionList[0]]
                ?: throw Exception("Should not happen.")
            assertEquals(5, executionReport1.count.total)
            assertEquals(33, executionReport1.count.totalPercentage)

            val executionReport2 = executions[physicalTherapyExerciseExecutionList[1]]
                ?: throw Exception("Should not happen.")
            assertEquals(5, executionReport2.count.total)
            assertEquals(33, executionReport2.count.totalPercentage)

            val executionReport3 = executions[physicalTherapyExerciseExecutionList[2]]
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
        }
}