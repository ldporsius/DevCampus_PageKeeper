package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.data

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import org.junit.Test

class FindTopLevelSectionsTest {

    @Test
    fun `flat sections - all are found`() {
        val body = """
            <section><p>Chapter one</p></section>
            <section><p>Chapter two</p></section>
            <section><p>Chapter three</p></section>
        """.trimIndent()

        val result = findTopLevelSections(body)

        assertThat(result).hasSize(3)
    }

    @Test
    fun `nested sections - only the outermost section is returned`() {
        val body = """
            <section>
                <p>Outer content</p>
                <section><p>Inner content</p></section>
                <p>More outer</p>
            </section>
        """.trimIndent()

        val result = findTopLevelSections(body)

        assertThat(result).hasSize(1)
    }

    @Test
    fun `nested sections - outer result contains inner section markup`() {
        val body = "<section><p>Outer</p><section><p>Inner</p></section></section>"

        val result = findTopLevelSections(body)

        assertThat(result).hasSize(1)
        assertThat(result.first()).contains("<section><p>Inner</p></section>")
    }

    @Test
    fun `nested sections - recursive call finds the inner section`() {
        val body = "<section><p>Outer</p><section><p>Inner</p></section></section>"

        val outer = findTopLevelSections(body)
        val outerContent = outer.first().removePrefix("<section>").removeSuffix("</section>")
        val inner = findTopLevelSections(outerContent)

        assertThat(inner).hasSize(1)
        assertThat(inner.first()).contains("Inner")
    }
}