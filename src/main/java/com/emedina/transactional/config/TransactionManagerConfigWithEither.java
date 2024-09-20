package com.emedina.transactional.config;

import io.vavr.control.Either;
import com.emedina.sharedkernel.transactional.Transactional;
import com.emedina.transactional.support.SpringTransactionAnnotationParserWithEither;
import com.emedina.transactional.support.TransactionInterceptorWithEither;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;

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
    public BeanFactoryTransactionAttributeSourceAdvisor transactionAdvisorWithEither(final TransactionInterceptorWithEither transactionInterceptor) {
        BeanFactoryTransactionAttributeSourceAdvisor advisor = new BeanFactoryTransactionAttributeSourceAdvisor();
        advisor.setTransactionAttributeSource(new AnnotationTransactionAttributeSource(new SpringTransactionAnnotationParserWithEither()));
        advisor.setAdvice(transactionInterceptor);
        advisor.setOrder(1);

        return advisor;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public TransactionInterceptorWithEither transactionInterceptorWithEither() {
        TransactionInterceptorWithEither result = new TransactionInterceptorWithEither();
        result.setTransactionAttributeSource(new AnnotationTransactionAttributeSource(new SpringTransactionAnnotationParserWithEither()));

        return result;
    }

}
