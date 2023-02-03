package org.epo.cne.transactional.support;

import io.vavr.control.Either;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * The type Transaction interceptor.
 *
 * @author joseluis.anton
 */
public class TransactionInterceptorWithEither extends TransactionAspectSupportWithEither implements MethodInterceptor, Serializable {

    private final TransactionInterceptor delegate;


    /**
     * Create a new TransactionInterceptorWithEither.
     * <p>Transaction manager and transaction attributes still need to be set.
     *
     * @see #setTransactionManager
     * @see #setTransactionAttributes(java.util.Properties)
     * @see #setTransactionAttributeSource(TransactionAttributeSource)
     */
    public TransactionInterceptorWithEither(TransactionInterceptor delegate) {
        this.delegate = delegate;
    }

    @Override
    @Nullable
    public Object invoke(MethodInvocation invocation) throws Throwable {
        // Check whether the method return type is Either.
        if (Either.class.isAssignableFrom(invocation.getMethod().getReturnType())) {
            // Work out the target class: may be {@code null}.
            // The TransactionAttributeSource should be passed the target class
            // as well as the method, which may be from an interface.
            Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);

            // Adapt to TransactionAspectSupport's invokeWithinTransaction...
            return invokeWithinTransaction(invocation.getMethod(), targetClass, new CoroutinesInvocationCallback() {
                @Override
                @Nullable
                public Object proceedWithInvocation() throws Throwable {
                    return invocation.proceed();
                }

                @Override
                public Object getTarget() {
                    return invocation.getThis();
                }

                @Override
                public Object[] getArguments() {
                    return invocation.getArguments();
                }
            });
        }

        // Fallback to the delegate.
        return this.delegate.invoke(invocation);
    }

    //---------------------------------------------------------------------
    // Serialization support
    //---------------------------------------------------------------------

    private void writeObject(ObjectOutputStream oos) throws IOException {
        // Rely on default serialization, although this class itself doesn't carry state anyway...
        oos.defaultWriteObject();

        // Deserialize superclass fields.
        oos.writeObject(getTransactionManagerBeanName());
        oos.writeObject(getTransactionManager());
        oos.writeObject(getTransactionAttributeSource());
        oos.writeObject(getBeanFactory());
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // Rely on default serialization, although this class itself doesn't carry state anyway...
        ois.defaultReadObject();

        // Serialize all relevant superclass fields.
        // Superclass can't implement Serializable because it also serves as base class
        // for AspectJ aspects (which are not allowed to implement Serializable)!
        setTransactionManagerBeanName((String) ois.readObject());
        setTransactionManager((PlatformTransactionManager) ois.readObject());
        setTransactionAttributeSource((TransactionAttributeSource) ois.readObject());
        setBeanFactory((BeanFactory) ois.readObject());
    }

}
