package com.emedina.transactional.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;

import com.emedina.transactional.support.TransactionInterceptorWithEither;

/**
 * Unit tests for {@link TransactionManagerConfigWithEither}.
 *
 * @author Enrique Medina Montenegro
 */
class TransactionManagerConfigWithEitherTest {

    @Test
    void testTransactionAdvisorWithEither() {
        // given
        TransactionManagerConfigWithEither config = new TransactionManagerConfigWithEither();
        AnnotationTransactionAttributeSource mockAttributeSource = mock(AnnotationTransactionAttributeSource.class);
        TransactionInterceptorWithEither mockInterceptor = mock(TransactionInterceptorWithEither.class);

        // when
        BeanFactoryTransactionAttributeSourceAdvisor advisor = config.transactionAdvisorWithEither(mockAttributeSource, mockInterceptor);

        // then
        assertThat(advisor).isNotNull();
        assertThat(advisor.getAdvice()).isSameAs(mockInterceptor);
        assertThat(advisor.getPointcut()).isNotNull();
        assertThat(advisor.getOrder()).isEqualTo(1);
    }

    @Test
    void testTransactionInterceptorWithEither() {
        // given
        TransactionManagerConfigWithEither config = new TransactionManagerConfigWithEither();
        AnnotationTransactionAttributeSource mockAttributeSource = mock(AnnotationTransactionAttributeSource.class);

        // when
        TransactionInterceptorWithEither interceptor = config.transactionInterceptorWithEither(mockAttributeSource);

        // then
        assertThat(interceptor).isNotNull();
        assertThat(interceptor.getTransactionAttributeSource()).isNotNull();
        assertThat(interceptor.getTransactionAttributeSource()).isSameAs(mockAttributeSource);
    }

}
