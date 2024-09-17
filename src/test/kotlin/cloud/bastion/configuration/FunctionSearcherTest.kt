package cloud.bastion.configuration

import cloud.bastion.configuration.searcher.function.FunctionFilter
import cloud.bastion.configuration.searcher.function.FunctionSearcher
import com.google.inject.Injector
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.reflect.full.declaredFunctions
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FunctionSearcherTest {
    @Mock
    private lateinit var injector: Injector

    @Mock
    private lateinit var classLoader: ClassLoader

    @InjectMocks
    private lateinit var functionSearcher: FunctionSearcher

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `matchFunctionAnnotatedWith`() {
        open class TestClass {
            @TestAnnotation
            fun annotatedFunction() {
            }

            fun notAnnotatedFunction() {}
        }

        val filter = FunctionFilter.annotatedWith(TestAnnotation::class)
        val annotatedFunction = TestClass::class.declaredFunctions.find { it.name == "annotatedFunction" }
        val notAnnotatedFunction = TestClass::class.declaredFunctions.find { it.name == "notAnnotatedFunction" }

        assertTrue(filter.test(annotatedFunction!!))
        assertFalse(filter.test(notAnnotatedFunction!!))
    }
}