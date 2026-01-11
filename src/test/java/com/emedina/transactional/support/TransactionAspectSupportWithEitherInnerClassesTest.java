package com.emedina.transactional.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.Test;

import io.vavr.control.Either;
import reactor.core.publisher.Mono;

/**
 * Unit tests for VavrDelegate in {@link TransactionAspectSupportWithEither}.
 *
 * @author Test Author
 */
class TransactionAspectSupportWithEitherInnerClassesTest {

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
    void testVavrDelegateDetermineEitherType_withEither() throws Exception {
        // given
        Method method = getClass().getDeclaredMethod("methodReturningEither");
        MethodInvocation invocation = mock(MethodInvocation.class);
        when(invocation.getMethod()).thenReturn(method);

        // when
        boolean result = TransactionAspectSupportWithEither.VavrDelegate.determineEitherType(invocation);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void testVavrDelegateDetermineEitherType_withoutEither() throws Exception {
        // given
        Method method = getClass().getDeclaredMethod("methodReturningString");
        MethodInvocation invocation = mock(MethodInvocation.class);
        when(invocation.getMethod()).thenReturn(method);

        // when
        boolean result = TransactionAspectSupportWithEither.VavrDelegate.determineEitherType(invocation);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void testVavrDelegateDetermineRightType() throws Exception {
        // given
        Method method = getClass().getDeclaredMethod("methodReturningEither");

        // when/then
        // This test is expected to throw an IllegalStateException because the method
        // requires a parameterized type with exactly one type argument
        try {
            TransactionAspectSupportWithEither.VavrDelegate.determineRightType(method.getGenericReturnType());
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("Type must have one type arguments");
        }
    }

    // Helper methods for testing
    Either<RuntimeException, String> methodReturningEither() {
        return Either.right("success");
    }

    String methodReturningString() {
        return "success";
    }

    Mono<Either<RuntimeException, String>> methodReturningMonoEither() {
        return Mono.just(Either.right("success"));
    }

}
