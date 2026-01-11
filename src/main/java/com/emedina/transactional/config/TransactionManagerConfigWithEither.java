package com.emedina.transactional.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;

import com.emedina.sharedkernel.transactional.Transactional;
import com.emedina.transactional.support.SpringTransactionAnnotationParserWithEither;
import com.emedina.transactional.support.TransactionInterceptorWithEither;

import io.vavr.control.Either;

/**
 * {@code @Configuration} class that registers a custom {@link AnnotationTransactionAttributeSource}
 * that can work with the custom {@link Transactional} to be aware of the monadic type {@link Either}.
 *
 * @author Enrique Medina Montenegro
 */
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class TransactionManagerConfigWithEither {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public AnnotationTransactionAttributeSource transactionAttributeSourceWithEither() {
        return new AnnotationTransactionAttributeSource(new SpringTransactionAnnotationParserWithEither());
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public BeanFactoryTransactionAttributeSourceAdvisor transactionAdvisorWithEither(
        final AnnotationTransactionAttributeSource transactionAttributeSource,
        final TransactionInterceptorWithEither transactionInterceptor) {
        BeanFactoryTransactionAttributeSourceAdvisor advisor = new BeanFactoryTransactionAttributeSourceAdvisor();
        advisor.setTransactionAttributeSource(transactionAttributeSource);
        advisor.setAdvice(transactionInterceptor);
        advisor.setOrder(1);

        return advisor;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public TransactionInterceptorWithEither transactionInterceptorWithEither(
        final AnnotationTransactionAttributeSource transactionAttributeSource) {
        TransactionInterceptorWithEither result = new TransactionInterceptorWithEither();
        result.setTransactionAttributeSource(transactionAttributeSource);

        return result;
    }

}
