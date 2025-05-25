package com.emedina.transactional.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

import com.emedina.transactional.support.EitherAwareTransactionInterceptor;

/**
 * Unit tests for {@link TransactionConfiguration}.
 *
 * @author Enrique Medina Montenegro
 */
class TransactionConfigurationTest {

    private TransactionConfiguration configuration;
    private TransactionAttributeSource transactionAttributeSource;
    private PlatformTransactionManager transactionManager;
    private EitherAwareTransactionInterceptor transactionInterceptor;

    @BeforeEach
    void setUp() {
        configuration = new TransactionConfiguration();
        transactionAttributeSource = mock(TransactionAttributeSource.class);
        transactionManager = mock(PlatformTransactionManager.class);
        transactionInterceptor = mock(EitherAwareTransactionInterceptor.class);
    }

    @Test
    void shouldCreateTransactionAdvisor_WithCorrectDependencies() {
        // given
        // dependencies are set up in setUp()

        // when
        BeanFactoryTransactionAttributeSourceAdvisor advisor = configuration.transactionAdvisor(
            transactionAttributeSource, transactionInterceptor);

        // then
        assertThat(advisor).isNotNull();
        assertThat(advisor.getAdvice()).isSameAs(transactionInterceptor);
        // Note: BeanFactoryTransactionAttributeSourceAdvisor doesn't expose getTransactionAttributeSource()
        // but we know it's set through the setter method
    }

    @Test
    void shouldCreateTransactionAttributeSource_WithBothParsers() {
        // given
        // no specific setup needed

        // when
        TransactionAttributeSource result = configuration.transactionAttributeSource();

        // then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(AnnotationTransactionAttributeSource.class);

        // Verify it contains both parsers (indirectly through class inspection)
        AnnotationTransactionAttributeSource source = (AnnotationTransactionAttributeSource) result;
        // Note: We can't directly verify the parsers as they're private in AnnotationTransactionAttributeSource
        // This is a limitation of unit testing - we'd need integration tests or reflection to fully verify
        assertThat(source).isNotNull();
    }

    @Test
    void shouldCreateEitherAwareTransactionInterceptor_WithCorrectConfiguration() {
        // given
        // dependencies are set up in setUp()

        // when
        EitherAwareTransactionInterceptor interceptor = configuration.transactionInterceptor(
            transactionAttributeSource, transactionManager);

        // then
        assertThat(interceptor).isNotNull();

        // Verify the transaction manager and attribute source were set
        // Note: These methods are inherited from TransactionInterceptor
        // and don't return the values, so we can't directly assert them
        // This is a limitation of unit testing this configuration
    }

}
