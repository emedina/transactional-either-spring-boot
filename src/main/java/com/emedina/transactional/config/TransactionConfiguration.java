package com.emedina.transactional.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.SpringTransactionAnnotationParser;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

import com.emedina.transactional.support.CustomTransactionAnnotationParser;
import com.emedina.transactional.support.EitherAwareTransactionInterceptor;

/**
 * {@code @Configuration} class that registers the components needed for
 * transaction management with Either support.
 *
 * <p>This configuration registers a custom transaction interceptor that
 * handles Either return types and supports both Spring's standard
 * 
 * @Transactional annotation and the custom annotation.
 *
 * @author Enrique Medina Montenegro
 */
@Configuration
public class TransactionConfiguration {

    /**
     * Creates the transaction advisor that will apply transactions to methods.
     * 
     * @param transactionAttributeSource the source of transaction attributes
     * @param transactionInterceptor     the transaction interceptor
     * @return the transaction advisor
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public BeanFactoryTransactionAttributeSourceAdvisor transactionAdvisor(
        TransactionAttributeSource transactionAttributeSource,
        EitherAwareTransactionInterceptor transactionInterceptor) {
        BeanFactoryTransactionAttributeSourceAdvisor advisor = new BeanFactoryTransactionAttributeSourceAdvisor();
        advisor.setTransactionAttributeSource(transactionAttributeSource);
        advisor.setAdvice(transactionInterceptor);
        return advisor;
    }

    /**
     * Creates the transaction attribute source that recognizes both Spring's
     * standard @Transactional annotation and the custom annotation.
     * 
     * @return the transaction attribute source
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public TransactionAttributeSource transactionAttributeSource() {
        return new AnnotationTransactionAttributeSource(
            new SpringTransactionAnnotationParser(),
            new CustomTransactionAnnotationParser()
        );
    }

    /**
     * Creates the transaction interceptor that handles Either return types.
     * 
     * @param transactionAttributeSource the source of transaction attributes
     * @param transactionManager         the transaction manager
     * @return the transaction interceptor
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public EitherAwareTransactionInterceptor transactionInterceptor(
        TransactionAttributeSource transactionAttributeSource,
        PlatformTransactionManager transactionManager) {
        EitherAwareTransactionInterceptor interceptor = new EitherAwareTransactionInterceptor();
        interceptor.setTransactionAttributeSource(transactionAttributeSource);
        interceptor.setTransactionManager(transactionManager);
        return interceptor;
    }

}
