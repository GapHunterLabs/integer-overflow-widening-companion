package dev.gaphunter.integeroverflowwideningcompanion.inspection

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class OverflowInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(OverflowInspection::class.java)
    }

    fun `test int times int assigned directly to a long local variable is flagged`() {
        myFixture.configureByText(
            "Calc.java",
            """
            class Calc {
                long compute(int a, int b) {
                    long result = a * b;
                    return result;
                }
            }
            """.trimIndent(),
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.any { it.description?.contains("Integer Overflow") == true })
    }

    fun `test int plus int assigned to a long via assignment is flagged`() {
        myFixture.configureByText(
            "Calc2.java",
            """
            class Calc2 {
                long total;
                void add(int a, int b) {
                    total = a + b;
                }
            }
            """.trimIndent(),
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.any { it.description?.contains("Integer Overflow") == true })
    }

    fun `test int times int passed directly to a long parameter is flagged`() {
        myFixture.configureByText(
            "Calc3.java",
            """
            class Calc3 {
                void consume(long value) {}

                void compute(int a, int b) {
                    consume(a * b);
                }
            }
            """.trimIndent(),
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.any { it.description?.contains("Integer Overflow") == true })
    }

    fun `test int times int returned directly from a long-returning method is flagged`() {
        myFixture.configureByText(
            "Calc4.java",
            """
            class Calc4 {
                long compute(int a, int b) {
                    return a * b;
                }
            }
            """.trimIndent(),
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.any { it.description?.contains("Integer Overflow") == true })
    }

    fun `test a widening cast on one operand is not flagged`() {
        myFixture.configureByText(
            "Calc5.java",
            """
            class Calc5 {
                long compute(int a, int b) {
                    long result = (long) a * b;
                    return result;
                }
            }
            """.trimIndent(),
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.none { it.description?.contains("Integer Overflow") == true })
    }

    fun `test int arithmetic assigned to an int variable is not flagged`() {
        myFixture.configureByText(
            "Calc6.java",
            """
            class Calc6 {
                int compute(int a, int b) {
                    int result = a * b;
                    return result;
                }
            }
            """.trimIndent(),
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.none { it.description?.contains("Integer Overflow") == true })
    }

    fun `test long times int is already safe arithmetic, not flagged`() {
        myFixture.configureByText(
            "Calc7.java",
            """
            class Calc7 {
                long compute(long a, int b) {
                    long result = a * b;
                    return result;
                }
            }
            """.trimIndent(),
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.none { it.description?.contains("Integer Overflow") == true })
    }
}
