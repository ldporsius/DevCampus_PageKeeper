package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.data

import assertk.assertThat
import assertk.assertions.hasSize
import org.junit.Test

class SectionRegexTest {

    @Test
    fun `flat sections - all are found`() {
        val body = """
            <section><p>Chapter one</p></section>
            <section><p>Chapter two</p></section>
            <section><p>Chapter three</p></section>
        """.trimIndent()

        val matches = sectionRegex.findAll(body).toList()

        assertThat(matches).hasSize(3)
    }

    @Test
    fun `nested sections - outer and inner are both found`() {
        val body = """
            <section>
                <p>Outer content</p>
                <section><p>Inner content</p></section>
                <p>More outer</p>
            </section>
        """.trimIndent()

        val matches = sectionRegex.findAll(body).toList()

        // Should find both the outer section and the inner section
        assertThat(matches).hasSize(2)
    }

    @Test
    fun `nested sections - inner content is captured`() {
        val body = "<section><p>Outer</p><section><p>Inner</p></section></section>"

        val matches = sectionRegex.findAll(body).toList()
        val allContent = matches.map { it.groupValues[1] }

        assertThat(matches).hasSize(2)
        assertThat(allContent.filter { it.contains("Inner") }).hasSize(1)
    }
}