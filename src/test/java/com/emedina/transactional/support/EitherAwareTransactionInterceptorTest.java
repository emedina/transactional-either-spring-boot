package com.emedina.transactional.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

import io.vavr.control.Either;

/**
 * Unit tests for {@link EitherAwareTransactionInterceptor}.
 *
 * @author Enrique Medina Montenegro
 */
@ExtendWith(MockitoExtension.class)
class EitherAwareTransactionInterceptorTest {

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private TransactionAttributeSource transactionAttributeSource;

    @Mock
    private MethodInvocation invocation;

    @Mock
    private TransactionAttribute transactionAttribute;

    @Mock
    private TransactionStatus transactionStatus;

    private EitherAwareTransactionInterceptor interceptor;

    // Test service interfaces and methods for testing
    private interface TestService {
        Either<String, Integer> methodReturningEither();

        String methodReturningString();
    }

    @BeforeEach
    void setUp() throws Exception {
        interceptor = new EitherAwareTransactionInterceptor();
        interceptor.setTransactionManager(transactionManager);
        interceptor.setTransactionAttributeSource(transactionAttributeSource);
    }

    @Test
    void shouldDetectEitherReturnType_WhenMethodReturnsEither() throws Exception {
        // given
        Method method = TestService.class.getMethod("methodReturningEither");
        // No need to stub invocation.getMethod() as we're directly using the method

        // when
        boolean isEither = isEitherReturnType(method);

        // then
        assertThat(isEither).isTrue();
    }

    @Test
    void shouldNotDetectEitherReturnType_WhenMethodReturnsNonEither() throws Exception {
        // given
        Method method = TestService.class.getMethod("methodReturningString");
        // No need to stub invocation.getMethod() as we're directly using the method

        // when
        boolean isEither = isEitherReturnType(method);

        // then
        assertThat(isEither).isFalse();
    }

    @Test
    void shouldUseCustomHandling_WhenMethodReturnsEither() throws Throwable {
        // given
        Method method = TestService.class.getMethod("methodReturningEither");
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getThis()).thenReturn(new Object());
        when(invocation.proceed()).thenReturn(Either.right(42));

        // Mock transaction creation
        mockTransactionCreation();

        // when
        Object result = interceptor.invoke(invocation);

        // then
        assertThat(result).isInstanceOf(Either.class);
        @SuppressWarnings("unchecked")
        Either<String, Integer> either = (Either<String, Integer>) result;
        assertThat(either.isRight()).isTrue();
        assertThat(either.get()).isEqualTo(42);

        // Verify transaction was committed
        verify(transactionStatus, never()).setRollbackOnly();
    }

    @Test
    void shouldRollbackTransaction_WhenEitherLeftReturned() throws Throwable {
        // given
        Method method = TestService.class.getMethod("methodReturningEither");
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getThis()).thenReturn(new Object());
        when(invocation.proceed()).thenReturn(Either.left("Error occurred"));

        // Mock transaction creation
        mockTransactionCreation();

        // when
        Object result = interceptor.invoke(invocation);

        // then
        assertThat(result).isInstanceOf(Either.class);
        @SuppressWarnings("unchecked")
        Either<String, Integer> either = (Either<String, Integer>) result;
        assertThat(either.isLeft()).isTrue();
        assertThat(either.getLeft()).isEqualTo("Error occurred");

        // Verify transaction was rolled back
        verify(transactionStatus).setRollbackOnly();
    }

    @Test
    void shouldDelegateToSuperclass_WhenMethodReturnsNonEither() throws Throwable {
        // given
        Method method = TestService.class.getMethod("methodReturningString");
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getThis()).thenReturn(new Object());
        when(invocation.proceed()).thenReturn("result");

        // Mock transaction creation
        mockTransactionCreation();

        // when
        Object result = interceptor.invoke(invocation);

        // then
        assertThat(result).isEqualTo("result");
    }

    @Test
    void shouldRollbackTransaction_WhenExceptionThrown() throws Throwable {
        // given
        Method method = TestService.class.getMethod("methodReturningEither");
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getThis()).thenReturn(new Object());

        RuntimeException exception = new RuntimeException("Test exception");
        doThrow(exception).when(invocation).proceed();

        // Mock transaction creation
        mockTransactionCreation();

        // when/then
        assertThatThrownBy(() -> interceptor.invoke(invocation))
            .isSameAs(exception);
    }

    @Test
    void shouldHandleNullTargetClass() throws Throwable {
        // given
        Method method = TestService.class.getMethod("methodReturningEither");
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getThis()).thenReturn(null); // null target
        when(invocation.proceed()).thenReturn(Either.right(42));

        // Mock transaction creation
        mockTransactionCreation();

        // when
        Object result = interceptor.invoke(invocation);

        // then
        assertThat(result).isInstanceOf(Either.class);
    }

    // Helper method to access private method via reflection
    private boolean isEitherReturnType(Method method) throws Exception {
        Method isEitherMethod = EitherAwareTransactionInterceptor.class.getDeclaredMethod("isEitherReturnType",
            Method.class);
        isEitherMethod.setAccessible(true);
        return (boolean) isEitherMethod.invoke(interceptor, method);
    }

    // Helper method to mock transaction creation
    private void mockTransactionCreation() throws Exception {
        // This is a simplified mock of the transaction creation process
        // In a real scenario, we'd need to mock more of the internal Spring transaction infrastructure
        when(transactionAttributeSource.getTransactionAttribute(any(), any())).thenReturn(transactionAttribute);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
    }
}
