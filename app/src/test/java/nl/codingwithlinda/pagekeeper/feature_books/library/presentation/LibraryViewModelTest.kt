package nl.codingwithlinda.pagekeeper.feature_books.library.presentation

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import nl.codingwithlinda.pagekeeper.core.domain.FakeBookFormatValidator
import nl.codingwithlinda.pagekeeper.core.domain.FakeBookParser
import nl.codingwithlinda.pagekeeper.core.domain.FakeBookRepository
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.LibraryAction
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(
        parser: FakeBookParser = FakeBookParser(),
        repository: FakeBookRepository = FakeBookRepository(),
        validator: FakeBookFormatValidator = FakeBookFormatValidator(supported = true)
    ) = LibraryViewModel(repository, parser, validator)

    // --- format validation ---

    @Test
    fun `unsupported format shows dialog and does not start import`() = runTest {
        val vm = viewModel(validator = FakeBookFormatValidator(supported = false))

        vm.onAction(LibraryAction.OnImportBookClick("file.fb2"))

        assertThat(vm.state.value.showUnsupportedFormatDialog).isTrue()
        assertThat(vm.state.value.isImporting).isFalse()
    }

    @Test
    fun `dismiss unsupported format dialog clears flag`() = runTest {
        val vm = viewModel(validator = FakeBookFormatValidator(supported = false))

        vm.onAction(LibraryAction.OnImportBookClick("file.fb2"))
        vm.onAction(LibraryAction.DismissUnsupportedFormatDialog)

        assertThat(vm.state.value.showUnsupportedFormatDialog).isFalse()
    }

    // --- in-progress state ---

    @Test
    fun `import is in progress while parser is running`() = runTest {
        val parser = FakeBookParser()
        val vm = viewModel(parser = parser)

        vm.onAction(LibraryAction.OnImportBookClick("file.fb2"))
        // parser is suspended at deferred.await() — not completed yet

        assertThat(vm.state.value.isImporting).isTrue()
    }

    // --- success ---

    @Test
    fun `successful import clears isImporting`() = runTest {
        val parser = FakeBookParser()
        val vm = viewModel(parser = parser)

        vm.onAction(LibraryAction.OnImportBookClick("file.fb2"))
        parser.complete(aBook())

        assertThat(vm.state.value.isImporting).isFalse()
        assertThat(vm.state.value.importFailed).isFalse()
    }

    @Test
    fun `successful import saves book to repository`() = runTest {
        val parser = FakeBookParser()
        val repo = FakeBookRepository()
        val book = aBook()
        val vm = viewModel(parser = parser, repository = repo)

        vm.onAction(LibraryAction.OnImportBookClick("file.fb2"))
        parser.complete(book)

        assertThat(repo.savedBooks).containsExactly(book)
    }

    // --- parse failure ---

    @Test
    fun `failed parse sets importFailed and clears isImporting`() = runTest {
        val parser = FakeBookParser()
        val vm = viewModel(parser = parser)

        vm.onAction(LibraryAction.OnImportBookClick("file.fb2"))
        parser.complete(null)

        assertThat(vm.state.value.isImporting).isFalse()
        assertThat(vm.state.value.importFailed).isTrue()
    }

    @Test
    fun `dismiss import failed clears flag`() = runTest {
        val parser = FakeBookParser()
        val vm = viewModel(parser = parser)

        vm.onAction(LibraryAction.OnImportBookClick("file.fb2"))
        parser.complete(null)
        vm.onAction(LibraryAction.DismissImportFailed)

        assertThat(vm.state.value.importFailed).isFalse()
    }

    // --- cancellation ---

    @Test
    fun `cancelling import clears isImporting`() = runTest {
        val parser = FakeBookParser()
        val vm = viewModel(parser = parser)

        vm.onAction(LibraryAction.OnImportBookClick("file.fb2"))
        assertThat(vm.state.value.isImporting).isTrue()

        vm.onAction(LibraryAction.CancelImport)

        assertThat(vm.state.value.isImporting).isFalse()
    }

    @Test
    fun `cancelling import does not set importFailed`() = runTest {
        val parser = FakeBookParser()
        val vm = viewModel(parser = parser)

        vm.onAction(LibraryAction.OnImportBookClick("file.fb2"))
        vm.onAction(LibraryAction.CancelImport)

        assertThat(vm.state.value.importFailed).isFalse()
    }

    // --- helpers ---

    private fun aBook() = Book(
        ISBN = "9780195034929",
        title = "Dead Souls",
        author = "Nikolai Gogol",
        imgUrl = "",
        dateCreated = 0L
    )
}