package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.data

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import org.junit.Assert.*
import org.junit.Test

class PageBuilderTest {

    private val builder = PageBuilder()

    @Test
    fun `test adding section`(){
        val section = Section(0)
        builder.addElementToSection(section, Paragraph("test"))

        val result = builder.sections
        assertEquals(1, result.size)
        assertThat(result[0]!!.elements).containsExactly(Paragraph("test"))
    }

    @Test
    fun `test adding paragraph`(){
        val section =  Section(0)
        builder.addElementToSection(section, Paragraph("test2"))
            .addElementToSection(section, Paragraph("test3"))


        val result = builder.sections
        assertEquals(1, result.size)
        assertThat(result[0]!!.elements).contains(Paragraph("test2"))
        assertThat(result[0]!!.elements).contains(Paragraph("test3"))
    }

    @Test
    fun `test adding multiple sections`() {
        val section1 = Section(0)
        val section2 = Section(1)
        builder.addElementToSection(section1, Paragraph("test1"))
            .addElementToSection(section2, Paragraph("test2"))

        val result = builder.sections
        assertEquals(2, result.size)
        assertThat(result[0]!!.elements).contains(Paragraph("test1"))
        assertThat(result[1]!!.elements).contains(Paragraph("test2"))
    }


}
