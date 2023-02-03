package org.epo.cne.transactional.config;

import org.epo.cne.transactional.support.SpringTransactionAnnotationParserWithEither;
import org.epo.cne.transactional.support.TransactionInterceptorWithEither;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.AbstractTransactionManagementConfiguration;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.config.TransactionManagementConfigUtils;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

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
            TransactionAttributeSource transactionAttributeSource, TransactionInterceptorWithEither transactionInterceptorWithEither) {

        BeanFactoryTransactionAttributeSourceAdvisor advisor = new BeanFactoryTransactionAttributeSourceAdvisor();
        advisor.setTransactionAttributeSource(transactionAttributeSource);
        advisor.setAdvice(transactionInterceptorWithEither);
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
    public TransactionInterceptorWithEither transactionInterceptor(TransactionAttributeSource transactionAttributeSource, TransactionManager txManager) {
        org.springframework.transaction.interceptor.TransactionInterceptor delegate =
                new org.springframework.transaction.interceptor.TransactionInterceptor(txManager, transactionAttributeSource);

        TransactionInterceptorWithEither result = new TransactionInterceptorWithEither(delegate);
        result.setTransactionAttributeSource(transactionAttributeSource);
        result.setTransactionManager(delegate.getTransactionManager());
        return result;
    }

}
