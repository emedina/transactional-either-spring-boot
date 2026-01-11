package com.emedina.transactional.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TransactionInterceptorWithEither}.
 *
 * @author Enrique Medina Montenegro
 */
class TransactionInterceptorWithEitherTest {

    @Test
    void testInvoke() throws Throwable {
        // given
        TransactionInterceptorWithEither interceptor = spy(new TransactionInterceptorWithEither());
        MethodInvocation mockInvocation = mock(MethodInvocation.class);
        Method method = String.class.getMethod("toString");
        Object target = "test";
        Object[] args = new Object[0];

        when(mockInvocation.getMethod()).thenReturn(method);
        when(mockInvocation.getThis()).thenReturn(target);
        when(mockInvocation.getArguments()).thenReturn(args);
        when(mockInvocation.proceed()).thenReturn("result");

        doReturn("result").when(interceptor).invokeWithinTransaction(eq(method), any(), any());

        // when
        Object result = interceptor.invoke(mockInvocation);

        // then
        assertThat(result).isEqualTo("result");
        verify(interceptor).invokeWithinTransaction(eq(method), any(), any());
    }

    @Test
    void testInvoke_withNullTarget() throws Throwable {
        // given
        TransactionInterceptorWithEither interceptor = spy(new TransactionInterceptorWithEither());
        MethodInvocation mockInvocation = mock(MethodInvocation.class);
        Method method = String.class.getMethod("toString");
        Object[] args = new Object[0];

        when(mockInvocation.getMethod()).thenReturn(method);
        when(mockInvocation.getThis()).thenReturn(null);
        when(mockInvocation.getArguments()).thenReturn(args);
        when(mockInvocation.proceed()).thenReturn("result");

        doReturn("result").when(interceptor).invokeWithinTransaction(eq(method), isNull(), any());

        // when
        Object result = interceptor.invoke(mockInvocation);

        // then
        assertThat(result).isEqualTo("result");
        verify(interceptor).invokeWithinTransaction(eq(method), isNull(), any());
    }

    @Test
    void testSerialization() throws Exception {
        // given
        TransactionInterceptorWithEither original = new TransactionInterceptorWithEither();

        // Set only the bean name, not the actual mock objects which aren't serializable
        original.setTransactionManagerBeanName("txManager");

        // when
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        TransactionInterceptorWithEither deserialized = (TransactionInterceptorWithEither) ois.readObject();
        ois.close();

        // then
        assertThat(deserialized).isNotNull();
        assertThat(deserialized).isNotSameAs(original);
        assertThat(deserialized.getTransactionManagerBeanName()).isEqualTo("txManager");
    }

}
