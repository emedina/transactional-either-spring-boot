package com.emedina.transactional.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link NoRollbackRuleAttributeWithEither}.
 *
 * @author Enrique Medina Montenegro
 */
class NoRollbackRuleAttributeWithEitherTest {

    @Test
    void testConstructorWithString() {
        // given
        String valueName = "java.lang.RuntimeException";

        // when
        NoRollbackRuleAttributeWithEither attribute = new NoRollbackRuleAttributeWithEither(valueName);

        // then
        assertThat(attribute.getExceptionName()).isEqualTo(valueName);
        assertThat(attribute.toString()).startsWith("No");
        assertThat(attribute.toString()).contains(valueName);
    }

    @Test
    void testConstructorWithClass() {
        // given
        Class<?> valueClass = RuntimeException.class;

        // when
        NoRollbackRuleAttributeWithEither attribute = new NoRollbackRuleAttributeWithEither(valueClass);

        // then
        assertThat(attribute.getExceptionName()).isEqualTo(valueClass.getName());
        assertThat(attribute.toString()).startsWith("No");
        assertThat(attribute.toString()).contains(valueClass.getName());
    }

    @Test
    void testToString() {
        // given
        NoRollbackRuleAttributeWithEither attribute = new NoRollbackRuleAttributeWithEither(RuntimeException.class);

        // when
        String result = attribute.toString();

        // then
        assertThat(result).startsWith("No");
        assertThat(result).contains("RollbackRuleAttributeWithEither");
        assertThat(result).contains(RuntimeException.class.getName());
    }
}
