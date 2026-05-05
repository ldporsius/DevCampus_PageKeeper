package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.data

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.runTest
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.PageElement
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Paragraph
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.InnerSection
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Section
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Title
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ParseSectionTest {

    @Test
    fun `flat section assigns sequential ids starting from counter`() = runTest {
        val body = """
            <title><p>Chapter One</p></title>
            <p>First.</p>
            <p>Second.</p>
        """.trimIndent()

        val section = parseSection(sectionId = 0, body = body)

        assertThat(section.elements).containsExactly(
            Title(id = 0, text = "Chapter One"),
            Paragraph(id = 1, text = "<p>First.</p>"),
            Paragraph(id = 2, text = "<p>Second.</p>")
        )
    }

    @Test
    fun `shared counter continues numbering across multiple parseSection calls`() = runTest {
        val first = "<p>A.</p><p>B.</p>"
        val second = "<p>C.</p><p>D.</p>"
        val counter = AtomicInteger(0)

        val sectionA = parseSection(sectionId = 0, body = first, idCounter = counter)
        val sectionB = parseSection(sectionId = 1, body = second, idCounter = counter)

        assertThat(sectionA.elements.map { it.id }).containsExactly(0, 1)
        assertThat(sectionB.elements.map { it.id }).containsExactly(2, 3)
        assertThat(counter.get()).isEqualTo(4)
    }

    @Test
    fun `nested sections share the id space with the outer counter`() = runTest {
        val body = """
            <section><p>Outer A.</p><p>Outer B.</p></section>
            <section><p>Outer C.</p></section>
        """.trimIndent()

        val section = parseSection(sectionId = 0, body = body)

        val nestedIds: List<Int> = section.elements
            .filterIsInstance<InnerSection>()
            .flatMap { it.elements }
            .map { it.id }

        assertThat(nestedIds).containsExactly(0, 1, 2)
    }

    @Test
    fun `nested section ids continue from a non-zero starting counter`() = runTest {
        val body = "<section><p>Inner.</p></section>"
        val counter = AtomicInteger(10)

        val section = parseSection(sectionId = 0, body = body, idCounter = counter)

        val innerElements: List<PageElement> = (section.elements.first() as InnerSection).elements
        assertThat(innerElements.first().id).isEqualTo(10)
        assertThat(counter.get()).isEqualTo(11)
    }
}