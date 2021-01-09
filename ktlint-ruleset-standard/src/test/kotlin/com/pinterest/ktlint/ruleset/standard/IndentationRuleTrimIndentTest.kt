package com.pinterest.ktlint.ruleset.standard // ktlint-disable

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

/**
 * Unit tests for trimIndent are added to explain and validate changes in the behavior of trimIndent as the logic for
 * the indentation rule relies on this.
 */
internal class IndentationRuleTrimIndentTest {
    @Test
    fun `Text is indented with equal amount of spaces which are all removed -1-`() {
        val foo = """
            line 1
            line 2
        """.trimIndent()

        assertThat(foo).isEqualTo(
            "" +
                "line 1\n" +
                "line 2"
        )
    }

    @Test
    fun `Text is indented with equal amount of spaces which are all removed -2-`() {
        val foo = """
            line 1
            line 2
            """.trimIndent()

        assertThat(foo).isEqualTo(
            "" +
                "line 1\n" +
                "line 2"
        )
    }

    @Test
    fun `Text is indented with different amount of spaces -1-`() {
        val foo = """
            line 1
              line 2
            """.trimIndent()

        assertThat(foo).isEqualTo(
            "" +
                "line 1\n" +
                "  line 2"
        )
    }

    @Test
    fun `Text is indented with different amount of spaces -2-`() {
        val foo = """
              line 1
            line 2
            """.trimIndent()

        assertThat(foo).isEqualTo(
            "" +
                "  line 1\n" +
                "line 2"
        )
    }

    @Test
    fun `Text is indented with spaces but not well formatted -1-`() {
        // Next multiline string is formatted improperly on purpose
        val foo = """
      line 1
        line 2
            """.trimIndent()

        assertThat(foo).isEqualTo(
            "" +
                "line 1\n" +
                "  line 2"
        )
    }

    @Test
    fun `Text is indented with spaces but not well formatted -2-`() {
        // Next multiline string is formatted improperly on purpose
        val foo = """line 1
          line 2""".trimIndent()

        assertThat(foo).isEqualTo(
            "" +
                "line 1\n" +
                "          line 2"
        )
    }

    @Test
    fun `Text is indented with spaces but not well formatted -3-`() {
        // Next multiline string is formatted improperly on purpose
        val foo =
"""
      line 1
        line 2
        """.trimIndent()

        assertThat(foo).isEqualTo(
            "" +
                "line 1\n" +
                "  line 2"
        )
    }

    @Test
    fun `Text is indented with equal amount of tabs which are all removed -1-`() {
        val foo = """
            ${TAB}line 1
            ${TAB}line 2
        """.trimIndent()

        assertThat(foo).isEqualTo(
            "" +
                "line 1\n" +
                "line 2"
        )
    }

    @Test
    fun `Text is indented with equal amount of tabs which are all removed -2-`() {
        val foo = """
            ${TAB}line 1
            ${TAB}line 2
            """.trimIndent()

        assertThat(foo).isEqualTo(
            "" +
                "line 1\n" +
                "line 2"
        )
    }

    @Test
    fun `Text is indented with different amount of tabs -1-`() {
        val foo = """
            line 1
            ${TAB}line 2
            """.trimIndent()

        assertThat(foo).isEqualTo(
            "" +
                "line 1\n" +
                "\tline 2"
        )
    }

    @Test
    fun `Text is indented with different amount of tabs -2-`() {
        val foo = """
            ${TAB}line 1
            line 2
            """.trimIndent()

        assertThat(foo).isEqualTo(
            "" +
                "\tline 1\n" +
                "line 2"
        )
    }

    @Test
    fun `Text is indented with tabs but not well formatted -1-`() {
        // Next multiline string is formatted improperly on purpose
        val foo = """
${TAB}line 1
${TAB}${TAB}line 2
            """.trimIndent()

        assertThat(foo).isEqualTo(
            "" +
                "line 1\n" +
                "\tline 2"
        )
    }

    @Test
    fun `Text is indented with tabs but not well formatted -2-`() {
        // Next multiline string is formatted improperly on purpose
        val foo = """line 1
${TAB}${TAB}line 2""".trimIndent()

        assertThat(foo).isEqualTo(
            "" +
                "line 1\n" +
                "\t\tline 2"
        )
    }

    @Test
    fun `Text is indented with tabs but not well formatted -3-`() {
        val foo =
            // Next multiline string is formatted improperly on purpose
            """
${TAB}${TAB}line 1
${TAB}${TAB}${TAB}line 2
        """.trimIndent()

        assertThat(foo).isEqualTo(
            "" +
                "line 1\n" +
                "\tline 2"
        )
    }

    @Test
    fun `Text is indented with same amount of spaces and tabs which are all removed (one space equals one tab) -1-`() {
        val foo = """
            ${TAB}${SPACE}line 1
            ${SPACE}${TAB}line 2
        """.trimIndent()

        assertThat(foo).isEqualTo(
            "" +
                "line 1\n" +
                "line 2"
        )
    }

    @Test
    fun `Text is indented with same amount of spaces and tabs which are all removed (one space equals one tab) -2-`() {
        val foo = """
            ${TAB}${TAB}line 1
            ${SPACE}${SPACE}line 2
        """.trimIndent()

        assertThat(foo).isEqualTo(
            "" +
                "line 1\n" +
                "line 2"
        )
    }

    @Test
    fun `Text is indented with different amount of spaces and tabs (one space equals one tab)`() {
        val foo = """
            ${TAB}line 1
            ${SPACE}${SPACE}line 2
            line 3
        """.trimIndent()

        assertThat(foo).isEqualTo(
            "" +
                "\tline 1\n" +
                "  line 2\n" +
                "line 3"
        )
    }

    private companion object {
        /**
         * Replace the ${TAB} and ${SPACE} tokens before trimming the indent. Tabs can used in multiline string as ${'\t'}
         * but it tends to read a lot hard than the token. The space token is helpful for emphasising  when mixing tabs and
         * spaces.
         */
        const val TAB = "${'\t'}"
        const val SPACE = " "
    }
}