package com.emedina.transactional.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionAttribute;

import io.vavr.control.Either;

/**
 * Unit tests for {@link TransactionAspectSupportWithEither}.
 *
 * @author Enrique Medina Montenegro
 */
class TransactionAspectSupportWithEitherTest {

    /**
     * Concrete implementation of TransactionAspectSupportWithEither for testing.
     */
    static class TestTransactionAspectSupportWithEither extends TransactionAspectSupportWithEither {
        public Object invokeWithinTransactionForTest(Method method, Class<?> targetClass, InvocationCallback invocation)
            throws Throwable {
            return invokeWithinTransaction(method, targetClass, invocation);
        }
    }

    private TestTransactionAspectSupportWithEither aspectSupport;
    private PlatformTransactionManager transactionManager;
    private TransactionAttribute transactionAttribute;
    private TransactionStatus transactionStatus;
    private BeanFactory beanFactory;

    @BeforeEach
    void setUp() {
        aspectSupport = new TestTransactionAspectSupportWithEither();
        transactionManager = mock(PlatformTransactionManager.class);
        transactionAttribute = mock(TransactionAttribute.class);
        transactionStatus = mock(TransactionStatus.class);
        beanFactory = mock(BeanFactory.class);

        when(transactionManager.getTransaction(any(TransactionAttribute.class))).thenReturn(transactionStatus);
        aspectSupport.setTransactionManager(transactionManager);
        aspectSupport.setBeanFactory(beanFactory);
    }

    @Test
    void testInvokeWithinTransaction_withNoTransactionAttribute() throws Throwable {
        // given
        Method method = String.class.getMethod("toString");
        TransactionAspectSupportWithEither.InvocationCallback callback = mock(
            TransactionAspectSupportWithEither.InvocationCallback.class);
        when(callback.proceedWithInvocation()).thenReturn("result");

        // when
        Object result = aspectSupport.invokeWithinTransactionForTest(method, String.class, callback);

        // then
        assertThat(result).isEqualTo("result");
        verify(callback).proceedWithInvocation();
        verify(transactionManager, never()).getTransaction(any());
        verify(transactionManager, never()).commit(any());
        verify(transactionManager, never()).rollback(any());
    }

    @Test
    void testInvokeWithinTransaction_withTransactionAttribute() throws Throwable {
        // given
        aspectSupport.setTransactionAttributeSource(
            new org.springframework.transaction.interceptor.TransactionAttributeSource() {
                @Override
                public TransactionAttribute getTransactionAttribute(Method method, Class<?> targetClass) {
                    return transactionAttribute;
                }
            });

        Method method = String.class.getMethod("toString");
        TransactionAspectSupportWithEither.InvocationCallback callback = mock(
            TransactionAspectSupportWithEither.InvocationCallback.class);
        when(callback.proceedWithInvocation()).thenReturn("result");

        // when
        Object result = aspectSupport.invokeWithinTransactionForTest(method, String.class, callback);

        // then
        assertThat(result).isEqualTo("result");
        verify(callback).proceedWithInvocation();
        verify(transactionManager).getTransaction(any(TransactionAttribute.class));
        verify(transactionManager).commit(eq(transactionStatus));
        verify(transactionManager, never()).rollback(any());
    }

    @Test
    void testInvokeWithinTransaction_withException() throws Throwable {
        // given
        aspectSupport.setTransactionAttributeSource(
            new org.springframework.transaction.interceptor.TransactionAttributeSource() {
                @Override
                public TransactionAttribute getTransactionAttribute(Method method, Class<?> targetClass) {
                    return transactionAttribute;
                }
            });
        when(transactionAttribute.rollbackOn(any(Exception.class))).thenReturn(true);

        Method method = String.class.getMethod("toString");
        TransactionAspectSupportWithEither.InvocationCallback callback = mock(
            TransactionAspectSupportWithEither.InvocationCallback.class);
        RuntimeException exception = new RuntimeException("Test exception");
        when(callback.proceedWithInvocation()).thenThrow(exception);

        // when
        try {
            aspectSupport.invokeWithinTransactionForTest(method, String.class, callback);
        } catch (RuntimeException ex) {
            // Expected exception
            assertThat(ex).isSameAs(exception);
        }

        // then
        verify(callback).proceedWithInvocation();
        verify(transactionManager).getTransaction(any(TransactionAttribute.class));
        verify(transactionManager).rollback(eq(transactionStatus));
        verify(transactionManager, never()).commit(any());
    }

    @Test
    void testInvokeWithinTransaction_withEitherLeft() throws Throwable {
        // given
        aspectSupport.setTransactionAttributeSource(
            new org.springframework.transaction.interceptor.TransactionAttributeSource() {
                @Override
                public TransactionAttribute getTransactionAttribute(Method method, Class<?> targetClass) {
                    return transactionAttribute;
                }
            });
        when(transactionAttribute.rollbackOn(any(Exception.class))).thenReturn(true);

        Method method = getClass().getDeclaredMethod("methodReturningEither");
        TransactionAspectSupportWithEither.InvocationCallback callback = mock(
            TransactionAspectSupportWithEither.InvocationCallback.class);
        Either<RuntimeException, String> eitherLeft = Either.left(new RuntimeException("Test exception"));
        when(callback.proceedWithInvocation()).thenReturn(eitherLeft);

        // when
        Object result = aspectSupport.invokeWithinTransactionForTest(method, getClass(), callback);

        // then
        assertThat(result).isSameAs(eitherLeft);
        verify(callback).proceedWithInvocation();
        verify(transactionManager).getTransaction(any(TransactionAttribute.class));
        verify(transactionStatus).setRollbackOnly();
        verify(transactionManager).commit(eq(transactionStatus));
        verify(transactionManager, never()).rollback(any());
    }

    @Test
    void testInvokeWithinTransaction_withEitherRight() throws Throwable {
        // given
        aspectSupport.setTransactionAttributeSource(
            new org.springframework.transaction.interceptor.TransactionAttributeSource() {
                @Override
                public TransactionAttribute getTransactionAttribute(Method method, Class<?> targetClass) {
                    return transactionAttribute;
                }
            });

        Method method = getClass().getDeclaredMethod("methodReturningEither");
        TransactionAspectSupportWithEither.InvocationCallback callback = mock(
            TransactionAspectSupportWithEither.InvocationCallback.class);
        Either<RuntimeException, String> eitherRight = Either.right("success");
        when(callback.proceedWithInvocation()).thenReturn(eitherRight);

        // when
        Object result = aspectSupport.invokeWithinTransactionForTest(method, getClass(), callback);

        // then
        assertThat(result).isSameAs(eitherRight);
        verify(callback).proceedWithInvocation();
        verify(transactionManager).getTransaction(any(TransactionAttribute.class));
        verify(transactionStatus, never()).setRollbackOnly();
        verify(transactionManager).commit(eq(transactionStatus));
        verify(transactionManager, never()).rollback(any());
    }

    @Test
    void testSetTransactionAttributes() {
        // given
        Properties props = new Properties();
        props.setProperty("testMethod", "PROPAGATION_REQUIRED,readOnly");

        // when
        aspectSupport.setTransactionAttributes(props);

        // then
        assertThat(aspectSupport.getTransactionAttributeSource()).isNotNull();
    }

    @Test
    void testSetTransactionAttributeSources() {
        // given
        org.springframework.transaction.interceptor.TransactionAttributeSource[] sources = new org.springframework.transaction.interceptor.TransactionAttributeSource[] {
            mock(org.springframework.transaction.interceptor.TransactionAttributeSource.class)
        };

        // when
        aspectSupport.setTransactionAttributeSources(sources);

        // then
        assertThat(aspectSupport.getTransactionAttributeSource()).isNotNull();
    }

    @Test
    void testClearTransactionManagerCache() {
        // given
        // Set up the transaction manager cache with a value
        aspectSupport.setTransactionManager(transactionManager);
        aspectSupport.setBeanFactory(beanFactory);

        // when
        aspectSupport.clearTransactionManagerCache();

        // then
        // Verify that the bean factory is null after clearing the cache
        assertThat(aspectSupport.getBeanFactory()).isNull();
    }

    @Test
    void testVavrDelegateIsVavrEither() {
        // given
        Either<String, String> either = Either.right("test");

        // when
        boolean result = TransactionAspectSupportWithEither.VavrDelegate.isVavrEither(either);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void testVavrDelegateIsVavrEither_notEither() {
        // given
        String notEither = "test";

        // when
        boolean result = TransactionAspectSupportWithEither.VavrDelegate.isVavrEither(notEither);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void testVavrDelegateEvaluateEitherLeft() {
        // given
        RuntimeException exception = new RuntimeException("test");
        Either<RuntimeException, String> either = Either.left(exception);
        TransactionStatus status = mock(TransactionStatus.class);

        // Configure the transactionAttribute to roll back on the exception
        when(transactionAttribute.rollbackOn(exception)).thenReturn(true);

        // when
        Either<?, ?> result = TransactionAspectSupportWithEither.VavrDelegate.evaluateEitherLeft(either,
            transactionAttribute, status);

        // then
        assertThat(result).isSameAs(either);
        verify(status).setRollbackOnly();
    }

    @Test
    void testVavrDelegateEvaluateEitherRight() {
        // given
        Either<RuntimeException, String> either = Either.right("test");
        TransactionStatus status = mock(TransactionStatus.class);

        // when
        Either<?, ?> result = TransactionAspectSupportWithEither.VavrDelegate.evaluateEitherLeft(either,
            transactionAttribute, status);

        // then
        assertThat(result).isSameAs(either);
        verify(status, never()).setRollbackOnly();
    }

    @Test
    void testDetermineTransactionManager_withDefault() {
        // given
        // Default transaction manager already set in setUp()

        // when
        Object result = aspectSupport.determineTransactionManager(transactionAttribute);

        // then
        assertThat(result).isSameAs(transactionManager);
    }

    @Test
    void testAfterPropertiesSet() {
        // given
        aspectSupport.setTransactionAttributeSource(mock(
            org.springframework.transaction.interceptor.TransactionAttributeSource.class));

        // when
        aspectSupport.afterPropertiesSet();

        // then
        // No exception should be thrown
    }

    // Helper method for testing Either return type
    Either<RuntimeException, String> methodReturningEither() {
        return Either.right("success");
    }
}
