package com.emedina.transactional.support;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.lang.Nullable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import io.vavr.control.Either;

/**
 * AOP Alliance MethodInterceptor for declarative transaction management using
 * the common Spring transaction infrastructure with support for Either return types.
 * 
 * <p>This implementation extends Spring's standard TransactionInterceptor to add
 * support for methods that return Either, handling transaction commit/rollback
 * based on Either's left/right state.
 *
 * @author Enrique Medina Montenegro
 * @see org.springframework.transaction.interceptor.TransactionInterceptor
 */
public class EitherAwareTransactionInterceptor extends TransactionInterceptor {

    /**
     * Create a new EitherAwareTransactionInterceptor.
     */
    public EitherAwareTransactionInterceptor() {
        super();
    }

    /**
     * Create a new EitherAwareTransactionInterceptor.
     * 
     * @param tm  the transaction manager to use
     * @param tas the transaction attribute source to use
     */
    public EitherAwareTransactionInterceptor(TransactionManager tm, TransactionAttributeSource tas) {
        super(tm, tas);
    }

    /**
     * Implementation of the MethodInterceptor interface.
     * Intercepts method invocations and applies transaction management.
     */
    @Override
    @Nullable
    public Object invoke(MethodInvocation invocation) throws Throwable {
        // Work out the target class: may be null.
        Class<?> targetClass = (invocation.getThis() != null ? invocation.getThis().getClass() : null);

        // If method returns Either, use custom handling
        if (isEitherReturnType(invocation.getMethod())) {
            return invokeWithEitherHandling(invocation, targetClass);
        }

        // Otherwise use standard Spring handling
        return super.invoke(invocation);
    }

    /**
     * Determine if the method returns an Either type.
     * 
     * @param method the method to check
     * @return true if the method returns Either
     */
    private boolean isEitherReturnType(Method method) {
        return Either.class.isAssignableFrom(method.getReturnType());
    }

    /**
     * Handle transaction management for methods returning Either.
     * 
     * @param invocation  the method invocation
     * @param targetClass the target class
     * @return the result of the method invocation
     * @throws Throwable if the invocation throws an exception
     */
    @Nullable
    protected Object invokeWithEitherHandling(MethodInvocation invocation, @Nullable Class<?> targetClass)
        throws Throwable {

        // Get transaction info using Spring's standard mechanisms
        TransactionAttributeSource tas = getTransactionAttributeSource();
        TransactionAttribute txAttr = tas.getTransactionAttribute(invocation.getMethod(), targetClass);

        // Use the non-deprecated method that takes the target class as a parameter
        // and cast to PlatformTransactionManager
        PlatformTransactionManager tm = (PlatformTransactionManager) determineTransactionManager(txAttr, targetClass);

        // Create transaction
        TransactionInfo txInfo = createTransactionIfNecessary(tm, txAttr, invocation.getMethod().toString());

        try {
            // Execute method
            Object result = invocation.proceed();

            // Handle Either result
            if (result instanceof Either) {
                Either<?, ?> either = (Either<?, ?>) result;
                if (either.isLeft()) {
                    // Rollback on Either.left
                    txInfo.getTransactionStatus().setRollbackOnly();
                }
            }

            // Commit transaction
            commitTransactionAfterReturning(txInfo);
            return result;
        } catch (Throwable ex) {
            // Handle exception - rollback the transaction
            completeTransactionAfterThrowing(txInfo, ex);
            throw ex;
        } finally {
            // Clean up the transaction info
            cleanupTransactionInfo(txInfo);
        }
    }

}
