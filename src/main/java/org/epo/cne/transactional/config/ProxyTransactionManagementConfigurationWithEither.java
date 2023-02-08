package org.epo.cne.transactional.config;

import io.vavr.control.Either;
import org.epo.cne.transactional.support.SpringTransactionAnnotationParserWithEither;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.transaction.annotation.AbstractTransactionManagementConfiguration;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.config.TransactionManagementConfigUtils;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * {@code @Configuration} class that registers a {@link BeanFactoryTransactionAttributeSourceAdvisor}
 * that is aware of the monadic type {@link Either} and can be used to enable
 * Spring's annotation-driven transaction management capability.
 *
 * @author Enrique Medina Montenegro (em54029)
 */
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class ProxyTransactionManagementConfigurationWithEither extends AbstractTransactionManagementConfiguration {

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableTx = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableTransactionManagementWithEither.class.getName()));
        if (this.enableTx == null) {
            throw new IllegalArgumentException(
                    "@EnableTransactionManagementWithEither is not present on importing class " + importMetadata.getClassName());
        }
    }

    @Bean(name = TransactionManagementConfigUtils.TRANSACTION_ADVISOR_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public BeanFactoryTransactionAttributeSourceAdvisor transactionAdvisor(
            TransactionAttributeSource transactionAttributeSource, TransactionInterceptor transactionInterceptor) {

        BeanFactoryTransactionAttributeSourceAdvisor advisor = new BeanFactoryTransactionAttributeSourceAdvisor();
        advisor.setTransactionAttributeSource(transactionAttributeSource);
        advisor.setAdvice(transactionInterceptor);
        if (this.enableTx != null) {
            advisor.setOrder(this.enableTx.<Integer>getNumber("order"));
        }

        return advisor;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public TransactionAttributeSource transactionAttributeSource() {
        // Create custom transaction attribute source.
        return new AnnotationTransactionAttributeSource(new SpringTransactionAnnotationParserWithEither());
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public TransactionInterceptor transactionInterceptor(TransactionAttributeSource transactionAttributeSource) {
        TransactionInterceptor result = new TransactionInterceptor();
        result.setTransactionAttributeSource(transactionAttributeSource);

        return result;
    }

}
