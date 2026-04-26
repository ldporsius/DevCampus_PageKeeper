package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.data

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Citation
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Epigraph
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Paragraph
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Title
import org.junit.Test

class ParseElementsTest {

    @Test
    fun `title element is parsed as Title`() {
        val body = "<title><p>Chapter One</p></title>"

        val result = parseElements(body)

        assertThat(result).hasSize(1)
        assertThat(result.first()).isInstanceOf(Title::class)
    }

    @Test
    fun `title text has p tags stripped`() {
        val body = "<title><p>Chapter One</p></title>"

        val result = parseElements(body)

        assertThat((result.first() as Title).text).isEqualTo("Chapter One")
    }

    @Test
    fun `title appears before paragraphs when it comes first`() {
        val body = """
            <title><p>Chapter One</p></title>
            <p>First paragraph.</p>
            <p>Second paragraph.</p>
        """.trimIndent()

        val result = parseElements(body)

        assertThat(result).containsExactly(
            Title("Chapter One"),
            Paragraph("<p>First paragraph.</p>"),
            Paragraph("<p>Second paragraph.</p>")
        )
    }

    @Test
    fun `section without title produces only paragraphs`() {
        val body = "<p>Only text.</p>"

        val result = parseElements(body)

        assertThat(result).containsExactly(Paragraph("<p>Only text.</p>"))
    }

    @Test
    fun `cite element is parsed as Citation`() {
        val body = "<cite><p>To be or not to be.</p></cite>"

        val result = parseElements(body)

        assertThat(result).containsExactly(Citation("To be or not to be."))
    }

    @Test
    fun `epigraph element is parsed as Epigraph`() {
        val body = "<epigraph><p>It was a dark and stormy night.</p></epigraph>"

        val result = parseElements(body)

        assertThat(result).containsExactly(Epigraph("It was a dark and stormy night."))
    }

    @Test
    fun `cite and epigraph inner p tags do not produce extra paragraphs`() {
        val body = """
            <epigraph><p>Quote.</p></epigraph>
            <cite><p>Source.</p></cite>
            <p>Body text.</p>
        """.trimIndent()

        val result = parseElements(body)

        assertThat(result).containsExactly(
            Epigraph("Quote."),
            Citation("Source."),
            Paragraph("<p>Body text.</p>")
        )
    }
}