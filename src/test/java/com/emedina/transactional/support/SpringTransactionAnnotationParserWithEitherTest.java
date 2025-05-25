package com.emedina.transactional.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.interceptor.TransactionAttribute;

import com.emedina.sharedkernel.transactional.Transactional;

/**
 * Unit tests for {@link SpringTransactionAnnotationParserWithEither}.
 *
 * @author Test Author
 */
class SpringTransactionAnnotationParserWithEitherTest {

    @Transactional
    void transactionalMethod() {
    }

    @Transactional(rollbackForWithEither = { IllegalArgumentException.class })
    void transactionalMethodWithRollbackForWithEither() {
    }

    @Transactional(rollbackForClassNameWithEither = { "java.lang.IllegalStateException" })
    void transactionalMethodWithRollbackForClassNameWithEither() {
    }

    @Transactional(noRollbackForWithEither = { IllegalArgumentException.class })
    void transactionalMethodWithNoRollbackForWithEither() {
    }

    @Transactional(noRollbackForClassNameWithEither = { "java.lang.IllegalStateException" })
    void transactionalMethodWithNoRollbackForClassNameWithEither() {
    }

    @Test
    void testIsCandidateClass() {
        // given
        SpringTransactionAnnotationParserWithEither parser = new SpringTransactionAnnotationParserWithEither();

        // when
        boolean isCandidate = parser.isCandidateClass(TestClass.class);

        // then
        assertThat(isCandidate).isTrue();
    }

    @Test
    void testIsCandidateClass_notCandidate() {
        // given
        SpringTransactionAnnotationParserWithEither parser = new SpringTransactionAnnotationParserWithEither();

        // when
        boolean isCandidate = parser.isCandidateClass(String.class);

        // then
        assertThat(isCandidate).isFalse();
    }

    @Test
    void testParseTransactionAnnotation_noAnnotation() throws NoSuchMethodException {
        // given
        SpringTransactionAnnotationParserWithEither parser = new SpringTransactionAnnotationParserWithEither();
        Method method = getClass().getDeclaredMethod("nonTransactionalMethod");

        // when
        TransactionAttribute attribute = parser.parseTransactionAnnotation(method);

        // then
        assertThat(attribute).isNull();
    }

    @Test
    void testParseTransactionAnnotation_basicAnnotation() throws NoSuchMethodException {
        // given
        SpringTransactionAnnotationParserWithEither parser = new SpringTransactionAnnotationParserWithEither();
        Method method = getClass().getDeclaredMethod("transactionalMethod");

        // when
        TransactionAttribute attribute = parser.parseTransactionAnnotation(method);

        // then
        assertThat(attribute).isNotNull();
        assertThat(attribute).isInstanceOf(RuleBasedTransactionAttributeWithEither.class);
    }

    @Test
    void testParseTransactionAnnotation_withRollbackForWithEither() throws NoSuchMethodException {
        // given
        SpringTransactionAnnotationParserWithEither parser = new SpringTransactionAnnotationParserWithEither();
        Method method = getClass().getDeclaredMethod("transactionalMethodWithRollbackForWithEither");

        // when
        TransactionAttribute attribute = parser.parseTransactionAnnotation(method);

        // then
        assertThat(attribute).isNotNull();
        assertThat(attribute).isInstanceOf(RuleBasedTransactionAttributeWithEither.class);

        RuleBasedTransactionAttributeWithEither rbta = (RuleBasedTransactionAttributeWithEither) attribute;
        assertThat(rbta.getRollbackRulesWithEither()).isNotNull();
        assertThat(rbta.getRollbackRulesWithEither()).hasSize(1);
        assertThat(rbta.getRollbackRulesWithEither().get(0).getExceptionName())
            .isEqualTo(IllegalArgumentException.class.getName());
    }

    @Test
    void testParseTransactionAnnotation_withRollbackForClassNameWithEither() throws NoSuchMethodException {
        // given
        SpringTransactionAnnotationParserWithEither parser = new SpringTransactionAnnotationParserWithEither();
        Method method = getClass().getDeclaredMethod("transactionalMethodWithRollbackForClassNameWithEither");

        // when
        TransactionAttribute attribute = parser.parseTransactionAnnotation(method);

        // then
        assertThat(attribute).isNotNull();
        assertThat(attribute).isInstanceOf(RuleBasedTransactionAttributeWithEither.class);

        RuleBasedTransactionAttributeWithEither rbta = (RuleBasedTransactionAttributeWithEither) attribute;
        assertThat(rbta.getRollbackRulesWithEither()).isNotNull();
        assertThat(rbta.getRollbackRulesWithEither()).hasSize(1);
        assertThat(rbta.getRollbackRulesWithEither().get(0).getExceptionName())
            .isEqualTo("java.lang.IllegalStateException");
    }

    @Test
    void testParseTransactionAnnotation_withNoRollbackForWithEither() throws NoSuchMethodException {
        // given
        SpringTransactionAnnotationParserWithEither parser = new SpringTransactionAnnotationParserWithEither();
        Method method = getClass().getDeclaredMethod("transactionalMethodWithNoRollbackForWithEither");

        // when
        TransactionAttribute attribute = parser.parseTransactionAnnotation(method);

        // then
        assertThat(attribute).isNotNull();
        assertThat(attribute).isInstanceOf(RuleBasedTransactionAttributeWithEither.class);

        RuleBasedTransactionAttributeWithEither rbta = (RuleBasedTransactionAttributeWithEither) attribute;
        assertThat(rbta.getRollbackRulesWithEither()).isNotNull();
        assertThat(rbta.getRollbackRulesWithEither()).hasSize(1);
        assertThat(rbta.getRollbackRulesWithEither().get(0)).isInstanceOf(NoRollbackRuleAttributeWithEither.class);
        assertThat(rbta.getRollbackRulesWithEither().get(0).getExceptionName())
            .isEqualTo(IllegalArgumentException.class.getName());
    }

    @Test
    void testParseTransactionAnnotation_withNoRollbackForClassNameWithEither() throws NoSuchMethodException {
        // given
        SpringTransactionAnnotationParserWithEither parser = new SpringTransactionAnnotationParserWithEither();
        Method method = getClass().getDeclaredMethod("transactionalMethodWithNoRollbackForClassNameWithEither");

        // when
        TransactionAttribute attribute = parser.parseTransactionAnnotation(method);

        // then
        assertThat(attribute).isNotNull();
        assertThat(attribute).isInstanceOf(RuleBasedTransactionAttributeWithEither.class);

        RuleBasedTransactionAttributeWithEither rbta = (RuleBasedTransactionAttributeWithEither) attribute;
        assertThat(rbta.getRollbackRulesWithEither()).isNotNull();
        assertThat(rbta.getRollbackRulesWithEither()).hasSize(1);
        assertThat(rbta.getRollbackRulesWithEither().get(0)).isInstanceOf(NoRollbackRuleAttributeWithEither.class);
        assertThat(rbta.getRollbackRulesWithEither().get(0).getExceptionName())
            .isEqualTo("java.lang.IllegalStateException");
    }

    void nonTransactionalMethod() {
    }

    @Transactional
    static class TestClass {
    }
}
