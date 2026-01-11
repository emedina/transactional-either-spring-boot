package com.emedina.transactional.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.interceptor.TransactionAttribute;

/**
 * Unit tests for {@link DelegatingTransactionAttributeWithEither}.
 *
 * @author Enrique Medina Montenegro
 */
class DelegatingTransactionAttributeWithEitherTest {

    /**
     * Concrete implementation of DelegatingTransactionAttributeWithEither for testing.
     */
    static class TestDelegatingTransactionAttributeWithEither extends DelegatingTransactionAttributeWithEither {
        public TestDelegatingTransactionAttributeWithEither(TransactionAttribute targetAttribute) {
            super(targetAttribute);
        }
    }

    @Test
    void testGetQualifier() {
        // given
        TransactionAttribute mockTarget = mock(TransactionAttribute.class);
        String expectedQualifier = "testQualifier";
        when(mockTarget.getQualifier()).thenReturn(expectedQualifier);

        DelegatingTransactionAttributeWithEither attribute = new TestDelegatingTransactionAttributeWithEither(
            mockTarget);

        // when
        String qualifier = attribute.getQualifier();

        // then
        assertThat(qualifier).isEqualTo(expectedQualifier);
    }

    @Test
    void testGetLabels() {
        // given
        TransactionAttribute mockTarget = mock(TransactionAttribute.class);
        java.util.Collection<String> expectedLabels = java.util.List.of("label1", "label2");
        when(mockTarget.getLabels()).thenReturn(expectedLabels);

        DelegatingTransactionAttributeWithEither attribute = new TestDelegatingTransactionAttributeWithEither(
            mockTarget);

        // when
        java.util.Collection<String> labels = attribute.getLabels();

        // then
        assertThat(labels).isEqualTo(expectedLabels);
    }

    @Test
    void testRollbackOn() {
        // given
        TransactionAttribute mockTarget = mock(TransactionAttribute.class);
        Exception testException = new RuntimeException("Test exception");
        when(mockTarget.rollbackOn(testException)).thenReturn(true);

        DelegatingTransactionAttributeWithEither attribute = new TestDelegatingTransactionAttributeWithEither(
            mockTarget);

        // when
        boolean shouldRollback = attribute.rollbackOn(testException);

        // then
        assertThat(shouldRollback).isTrue();
    }

    @Test
    void testRollbackOnErrorValue_withRuleBasedAttribute() {
        // given
        RuleBasedTransactionAttributeWithEither mockTarget = mock(RuleBasedTransactionAttributeWithEither.class);
        Class<?> errorClass = IllegalArgumentException.class;
        when(mockTarget.rollbackOnErrorValue(errorClass)).thenReturn(true);

        DelegatingTransactionAttributeWithEither attribute = new TestDelegatingTransactionAttributeWithEither(
            mockTarget);

        // when
        boolean shouldRollback = attribute.rollbackOnErrorValue(errorClass);

        // then
        assertThat(shouldRollback).isTrue();
    }

    @Test
    void testRollbackOnErrorValue_withNonRuleBasedAttribute() {
        // given
        TransactionAttribute mockTarget = mock(TransactionAttribute.class);
        Class<?> errorClass = IllegalArgumentException.class;

        DelegatingTransactionAttributeWithEither attribute = new TestDelegatingTransactionAttributeWithEither(
            mockTarget);

        // when
        boolean shouldRollback = attribute.rollbackOnErrorValue(errorClass);

        // then
        assertThat(shouldRollback).isFalse();
    }

}
