package com.emedina.transactional.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.AnnotatedElement;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.NoRollbackRuleAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;

import com.emedina.sharedkernel.transactional.Isolation;
import com.emedina.sharedkernel.transactional.Propagation;
import com.emedina.sharedkernel.transactional.Transactional;

/**
 * Unit tests for {@link CustomTransactionAnnotationParser}.
 *
 * @author Enrique Medina Montenegro
 */
class CustomTransactionAnnotationParserTest {

    private CustomTransactionAnnotationParser parser;
    private AnnotatedElement element;
    private Transactional annotation;

    @BeforeEach
    void setUp() {
        parser = new CustomTransactionAnnotationParser();
        element = mock(AnnotatedElement.class);
        annotation = mock(Transactional.class);

        // Default behavior for the annotation
        when(annotation.value()).thenReturn("");
        when(annotation.propagation()).thenReturn(Propagation.REQUIRED);
        when(annotation.isolation()).thenReturn(Isolation.DEFAULT);
        when(annotation.timeout()).thenReturn(TransactionDefinition.TIMEOUT_DEFAULT);
        when(annotation.readOnly()).thenReturn(false);
        when(annotation.rollbackFor()).thenReturn(new Class[0]);
        when(annotation.rollbackForClassName()).thenReturn(new String[0]);
        when(annotation.noRollbackFor()).thenReturn(new Class[0]);
        when(annotation.noRollbackForClassName()).thenReturn(new String[0]);
        when(annotation.label()).thenReturn(new String[0]);
    }

    @Test
    void shouldReturnNull_WhenNoAnnotationPresent() {
        // given
        try (MockedStatic<AnnotatedElementUtils> utilities = Mockito.mockStatic(AnnotatedElementUtils.class)) {
            utilities.when(() -> AnnotatedElementUtils.findMergedAnnotation(element, Transactional.class))
                .thenReturn(null);

            // when
            TransactionAttribute result = parser.parseTransactionAnnotation(element);

            // then
            assertThat(result).isNull();
        }
    }

    @Test
    void shouldParseAnnotation_WhenAnnotationPresent() {
        // given
        try (MockedStatic<AnnotatedElementUtils> utilities = Mockito.mockStatic(AnnotatedElementUtils.class)) {
            utilities.when(() -> AnnotatedElementUtils.findMergedAnnotation(element, Transactional.class))
                .thenReturn(annotation);

            // when
            TransactionAttribute result = parser.parseTransactionAnnotation(element);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isInstanceOf(RuleBasedTransactionAttribute.class);
            assertThat(result.getPropagationBehavior()).isEqualTo(TransactionDefinition.PROPAGATION_REQUIRED);
            assertThat(result.getIsolationLevel()).isEqualTo(TransactionDefinition.ISOLATION_DEFAULT);
            assertThat(result.getTimeout()).isEqualTo(TransactionDefinition.TIMEOUT_DEFAULT);
            assertThat(result.isReadOnly()).isFalse();
        }
    }

    @ParameterizedTest
    @EnumSource(Propagation.class)
    void shouldMapPropagationBehavior_ForAllPropagationValues(Propagation propagation) {
        // given
        try (MockedStatic<AnnotatedElementUtils> utilities = Mockito.mockStatic(AnnotatedElementUtils.class)) {
            utilities.when(() -> AnnotatedElementUtils.findMergedAnnotation(element, Transactional.class))
                .thenReturn(annotation);
            when(annotation.propagation()).thenReturn(propagation);

            // when
            TransactionAttribute result = parser.parseTransactionAnnotation(element);

            // then
            assertThat(result).isNotNull();

            int expectedPropagation;
            switch (propagation) {
                case REQUIRED:
                    expectedPropagation = TransactionDefinition.PROPAGATION_REQUIRED;
                    break;
                case SUPPORTS:
                    expectedPropagation = TransactionDefinition.PROPAGATION_SUPPORTS;
                    break;
                case MANDATORY:
                    expectedPropagation = TransactionDefinition.PROPAGATION_MANDATORY;
                    break;
                case REQUIRES_NEW:
                    expectedPropagation = TransactionDefinition.PROPAGATION_REQUIRES_NEW;
                    break;
                case NOT_SUPPORTED:
                    expectedPropagation = TransactionDefinition.PROPAGATION_NOT_SUPPORTED;
                    break;
                case NEVER:
                    expectedPropagation = TransactionDefinition.PROPAGATION_NEVER;
                    break;
                case NESTED:
                    expectedPropagation = TransactionDefinition.PROPAGATION_NESTED;
                    break;
                default:
                    expectedPropagation = TransactionDefinition.PROPAGATION_REQUIRED;
            }

            assertThat(result.getPropagationBehavior()).isEqualTo(expectedPropagation);
        }
    }

    @ParameterizedTest
    @EnumSource(Isolation.class)
    void shouldMapIsolationLevel_ForAllIsolationValues(Isolation isolation) {
        // given
        try (MockedStatic<AnnotatedElementUtils> utilities = Mockito.mockStatic(AnnotatedElementUtils.class)) {
            utilities.when(() -> AnnotatedElementUtils.findMergedAnnotation(element, Transactional.class))
                .thenReturn(annotation);
            when(annotation.isolation()).thenReturn(isolation);

            // when
            TransactionAttribute result = parser.parseTransactionAnnotation(element);

            // then
            assertThat(result).isNotNull();

            int expectedIsolation;
            switch (isolation) {
                case DEFAULT:
                    expectedIsolation = TransactionDefinition.ISOLATION_DEFAULT;
                    break;
                case READ_UNCOMMITTED:
                    expectedIsolation = TransactionDefinition.ISOLATION_READ_UNCOMMITTED;
                    break;
                case READ_COMMITTED:
                    expectedIsolation = TransactionDefinition.ISOLATION_READ_COMMITTED;
                    break;
                case REPEATABLE_READ:
                    expectedIsolation = TransactionDefinition.ISOLATION_REPEATABLE_READ;
                    break;
                case SERIALIZABLE:
                    expectedIsolation = TransactionDefinition.ISOLATION_SERIALIZABLE;
                    break;
                default:
                    expectedIsolation = TransactionDefinition.ISOLATION_DEFAULT;
            }

            assertThat(result.getIsolationLevel()).isEqualTo(expectedIsolation);
        }
    }

    @Test
    void shouldSetTimeout_WhenSpecified() {
        // given
        try (MockedStatic<AnnotatedElementUtils> utilities = Mockito.mockStatic(AnnotatedElementUtils.class)) {
            utilities.when(() -> AnnotatedElementUtils.findMergedAnnotation(element, Transactional.class))
                .thenReturn(annotation);
            when(annotation.timeout()).thenReturn(30);

            // when
            TransactionAttribute result = parser.parseTransactionAnnotation(element);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTimeout()).isEqualTo(30);
        }
    }

    @Test
    void shouldSetReadOnly_WhenSpecified() {
        // given
        try (MockedStatic<AnnotatedElementUtils> utilities = Mockito.mockStatic(AnnotatedElementUtils.class)) {
            utilities.when(() -> AnnotatedElementUtils.findMergedAnnotation(element, Transactional.class))
                .thenReturn(annotation);
            when(annotation.readOnly()).thenReturn(true);

            // when
            TransactionAttribute result = parser.parseTransactionAnnotation(element);

            // then
            assertThat(result).isNotNull();
            assertThat(result.isReadOnly()).isTrue();
        }
    }

    @Test
    void shouldSetQualifier_WhenValueSpecified() {
        // given
        try (MockedStatic<AnnotatedElementUtils> utilities = Mockito.mockStatic(AnnotatedElementUtils.class)) {
            utilities.when(() -> AnnotatedElementUtils.findMergedAnnotation(element, Transactional.class))
                .thenReturn(annotation);
            when(annotation.value()).thenReturn("customTxManager");

            // when
            TransactionAttribute result = parser.parseTransactionAnnotation(element);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getQualifier()).isEqualTo("customTxManager");
        }
    }

    @Test
    void shouldAddRollbackRules_WhenRollbackForSpecified() {
        // given
        try (MockedStatic<AnnotatedElementUtils> utilities = Mockito.mockStatic(AnnotatedElementUtils.class)) {
            utilities.when(() -> AnnotatedElementUtils.findMergedAnnotation(element, Transactional.class))
                .thenReturn(annotation);
            when(annotation.rollbackFor()).thenReturn(new Class[] { IllegalArgumentException.class,
                NullPointerException.class });

            // when
            TransactionAttribute result = parser.parseTransactionAnnotation(element);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isInstanceOf(RuleBasedTransactionAttribute.class);

            RuleBasedTransactionAttribute ruleAttr = (RuleBasedTransactionAttribute) result;
            List<?> rollbackRules = ruleAttr.getRollbackRules();

            assertThat(rollbackRules).hasSize(2);
            assertThat(rollbackRules.get(0)).isInstanceOf(RollbackRuleAttribute.class);
            assertThat(((RollbackRuleAttribute) rollbackRules.get(0)).getExceptionName())
                .isEqualTo(IllegalArgumentException.class.getName());
            assertThat(((RollbackRuleAttribute) rollbackRules.get(1)).getExceptionName())
                .isEqualTo(NullPointerException.class.getName());
        }
    }

    @Test
    void shouldAddRollbackRules_WhenRollbackForClassNameSpecified() {
        // given
        try (MockedStatic<AnnotatedElementUtils> utilities = Mockito.mockStatic(AnnotatedElementUtils.class)) {
            utilities.when(() -> AnnotatedElementUtils.findMergedAnnotation(element, Transactional.class))
                .thenReturn(annotation);
            when(annotation.rollbackForClassName()).thenReturn(new String[] { "java.lang.IllegalArgumentException",
                "java.lang.NullPointerException" });

            // when
            TransactionAttribute result = parser.parseTransactionAnnotation(element);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isInstanceOf(RuleBasedTransactionAttribute.class);

            RuleBasedTransactionAttribute ruleAttr = (RuleBasedTransactionAttribute) result;
            List<?> rollbackRules = ruleAttr.getRollbackRules();

            assertThat(rollbackRules).hasSize(2);
            assertThat(rollbackRules.get(0)).isInstanceOf(RollbackRuleAttribute.class);
            assertThat(((RollbackRuleAttribute) rollbackRules.get(0)).getExceptionName())
                .isEqualTo("java.lang.IllegalArgumentException");
            assertThat(((RollbackRuleAttribute) rollbackRules.get(1)).getExceptionName())
                .isEqualTo("java.lang.NullPointerException");
        }
    }

    @Test
    void shouldAddNoRollbackRules_WhenNoRollbackForSpecified() {
        // given
        try (MockedStatic<AnnotatedElementUtils> utilities = Mockito.mockStatic(AnnotatedElementUtils.class)) {
            utilities.when(() -> AnnotatedElementUtils.findMergedAnnotation(element, Transactional.class))
                .thenReturn(annotation);
            when(annotation.noRollbackFor()).thenReturn(new Class[] { IllegalArgumentException.class });

            // when
            TransactionAttribute result = parser.parseTransactionAnnotation(element);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isInstanceOf(RuleBasedTransactionAttribute.class);

            RuleBasedTransactionAttribute ruleAttr = (RuleBasedTransactionAttribute) result;
            List<?> rollbackRules = ruleAttr.getRollbackRules();

            assertThat(rollbackRules).hasSize(1);
            assertThat(rollbackRules.get(0)).isInstanceOf(NoRollbackRuleAttribute.class);
            assertThat(((NoRollbackRuleAttribute) rollbackRules.get(0)).getExceptionName())
                .isEqualTo(IllegalArgumentException.class.getName());
        }
    }

    @Test
    void shouldAddNoRollbackRules_WhenNoRollbackForClassNameSpecified() {
        // given
        try (MockedStatic<AnnotatedElementUtils> utilities = Mockito.mockStatic(AnnotatedElementUtils.class)) {
            utilities.when(() -> AnnotatedElementUtils.findMergedAnnotation(element, Transactional.class))
                .thenReturn(annotation);
            when(annotation.noRollbackForClassName()).thenReturn(new String[] { "java.lang.IllegalArgumentException" });

            // when
            TransactionAttribute result = parser.parseTransactionAnnotation(element);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isInstanceOf(RuleBasedTransactionAttribute.class);

            RuleBasedTransactionAttribute ruleAttr = (RuleBasedTransactionAttribute) result;
            List<?> rollbackRules = ruleAttr.getRollbackRules();

            assertThat(rollbackRules).hasSize(1);
            assertThat(rollbackRules.get(0)).isInstanceOf(NoRollbackRuleAttribute.class);
            assertThat(((NoRollbackRuleAttribute) rollbackRules.get(0)).getExceptionName())
                .isEqualTo("java.lang.IllegalArgumentException");
        }
    }

    @Test
    void shouldSetLabels_WhenSpecified() {
        // given
        try (MockedStatic<AnnotatedElementUtils> utilities = Mockito.mockStatic(AnnotatedElementUtils.class)) {
            utilities.when(() -> AnnotatedElementUtils.findMergedAnnotation(element, Transactional.class))
                .thenReturn(annotation);
            when(annotation.label()).thenReturn(new String[] { "label1", "label2" });

            // when
            TransactionAttribute result = parser.parseTransactionAnnotation(element);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isInstanceOf(RuleBasedTransactionAttribute.class);

            RuleBasedTransactionAttribute ruleAttr = (RuleBasedTransactionAttribute) result;
            assertThat(ruleAttr.getLabels()).containsExactly("label1", "label2");
        }
    }

}
