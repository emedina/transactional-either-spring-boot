package com.emedina.transactional.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;

/**
 * Unit tests for {@link RuleBasedTransactionAttributeWithEither}.
 *
 * @author Enrique Medina Montenegro
 */
class RuleBasedTransactionAttributeWithEitherTest {

    @Test
    void testFrom() {
        // given
        RuleBasedTransactionAttribute original = new RuleBasedTransactionAttribute();
        original.setName("testTransaction");

        // when
        RuleBasedTransactionAttributeWithEither attribute = RuleBasedTransactionAttributeWithEither.from(original);

        // then
        assertThat(attribute).isNotNull();
        assertThat(attribute.getName()).isEqualTo("testTransaction");
    }

    @Test
    void testRollbackOn_withNoRules() {
        // given
        RuleBasedTransactionAttributeWithEither attribute = RuleBasedTransactionAttributeWithEither.from(
            new RuleBasedTransactionAttribute());
        RuntimeException ex = new RuntimeException("Test exception");

        // when
        boolean result = attribute.rollbackOn(ex);

        // then
        // Default behavior is to rollback on unchecked exceptions
        assertThat(result).isTrue();
    }

    @Test
    void testRollbackOn_withMatchingRule() {
        // given
        RuleBasedTransactionAttributeWithEither attribute = RuleBasedTransactionAttributeWithEither.from(
            new RuleBasedTransactionAttribute());

        List<RollbackRuleAttributeWithEither> rules = new ArrayList<>();
        rules.add(new RollbackRuleAttributeWithEither(RuntimeException.class));
        attribute.setRollbackRulesWithEither(rules);

        RuntimeException ex = new RuntimeException("Test exception");

        // when
        boolean result = attribute.rollbackOn(ex);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void testRollbackOn_withNoRollbackRule() {
        // given
        RuleBasedTransactionAttributeWithEither attribute = RuleBasedTransactionAttributeWithEither.from(
            new RuleBasedTransactionAttribute());

        List<RollbackRuleAttributeWithEither> rules = new ArrayList<>();
        rules.add(new NoRollbackRuleAttributeWithEither(RuntimeException.class));
        attribute.setRollbackRulesWithEither(rules);

        RuntimeException ex = new RuntimeException("Test exception");

        // when
        boolean result = attribute.rollbackOn(ex);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void testRollbackOnErrorValue_withNoRules() {
        // given
        RuleBasedTransactionAttributeWithEither attribute = RuleBasedTransactionAttributeWithEither.from(
            new RuleBasedTransactionAttribute());
        Class<?> errorClass = IllegalArgumentException.class;

        // when
        boolean result = attribute.rollbackOnErrorValue(errorClass);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void testRollbackOnErrorValue_withMatchingRule() {
        // given
        RuleBasedTransactionAttributeWithEither attribute = RuleBasedTransactionAttributeWithEither.from(
            new RuleBasedTransactionAttribute());

        List<RollbackRuleAttributeWithEither> rules = new ArrayList<>();
        rules.add(new RollbackRuleAttributeWithEither(IllegalArgumentException.class));
        attribute.setRollbackRulesWithEither(rules);

        Class<?> errorClass = IllegalArgumentException.class;

        // when
        boolean result = attribute.rollbackOnErrorValue(errorClass);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void testRollbackOnErrorValue_withNoRollbackRule() {
        // given
        RuleBasedTransactionAttributeWithEither attribute = RuleBasedTransactionAttributeWithEither.from(
            new RuleBasedTransactionAttribute());

        List<RollbackRuleAttributeWithEither> rules = new ArrayList<>();
        rules.add(new NoRollbackRuleAttributeWithEither(IllegalArgumentException.class));
        attribute.setRollbackRulesWithEither(rules);

        Class<?> errorClass = IllegalArgumentException.class;

        // when
        boolean result = attribute.rollbackOnErrorValue(errorClass);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void testRollbackOnErrorValue_withSubclassMatch() {
        // given
        RuleBasedTransactionAttributeWithEither attribute = RuleBasedTransactionAttributeWithEither.from(
            new RuleBasedTransactionAttribute());

        List<RollbackRuleAttributeWithEither> rules = new ArrayList<>();
        rules.add(new RollbackRuleAttributeWithEither(RuntimeException.class));
        attribute.setRollbackRulesWithEither(rules);

        Class<?> errorClass = IllegalArgumentException.class;

        // when
        boolean result = attribute.rollbackOnErrorValue(errorClass);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void testGettersAndSetters() {
        // given
        RuleBasedTransactionAttributeWithEither attribute = RuleBasedTransactionAttributeWithEither.from(
            new RuleBasedTransactionAttribute());
        List<RollbackRuleAttributeWithEither> rules = new ArrayList<>();
        rules.add(new RollbackRuleAttributeWithEither(RuntimeException.class));

        // when
        attribute.setRollbackRulesWithEither(rules);
        List<RollbackRuleAttributeWithEither> retrievedRules = attribute.getRollbackRulesWithEither();

        // then
        assertThat(retrievedRules).isEqualTo(rules);
    }
}
