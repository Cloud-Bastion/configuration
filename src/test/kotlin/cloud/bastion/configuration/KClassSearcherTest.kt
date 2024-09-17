package cloud.bastion.configuration

import cloud.bastion.configuration.searcher.kclass.KClassFilter
import cloud.bastion.configuration.searcher.kclass.KClassSearcher
import com.google.inject.Injector
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.reflect.KClass
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KClassSearcherTest {
    @Mock
    private lateinit var injector: Injector

    @Mock
    private lateinit var classLoader: ClassLoader

    @InjectMocks
    private lateinit var kclassSearcher: KClassSearcher

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `test matchSubClassOf`() {
        val filter = KClassFilter.subClassOf(Number::class)

        // Example classes
        val intClass: KClass<*> = Int::class
        val stringClass: KClass<*> = String::class

        assertTrue { filter.test(intClass) }
        assertFalse { filter.test(stringClass) }
    }

    @Test
    fun `test matchAnnotatedWith`() {
        @TestAnnotation
        class AnnotatedClass

        class NonAnnotatedClass

        val filter = KClassFilter.annotatedWith(TestAnnotation::class)

        assertTrue { filter.test(AnnotatedClass::class) }
        assertFalse { filter.test(NonAnnotatedClass::class) }
    }

    @Test
    fun `test matchFunctionFilter`() {
        class TestClass {
            @TestAnnotation
            fun annotatedFunction() {
            }

            fun nonAnnotatedFunction() {}
        }

        class MultipleAnnotatedClass {
            @TestAnnotation
            fun annotatedFunction1() {
            }

            @TestAnnotation
            fun annotatedFunction2() {
            }
        }
    }
}