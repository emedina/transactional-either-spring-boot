package com.emedina.transactional.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RollbackRuleAttributeWithEither}.
 *
 * @author Enrique Medina Montenegro
 */
class RollbackRuleAttributeWithEitherTest {

    @Test
    void testConstructorWithString() {
        // given
        String valueName = "java.lang.RuntimeException";

        // when
        RollbackRuleAttributeWithEither attribute = new RollbackRuleAttributeWithEither(valueName);

        // then
        assertThat(attribute.getExceptionName()).isEqualTo(valueName);
    }

    @Test
    void testConstructorWithClass() {
        // given
        Class<?> valueClass = RuntimeException.class;

        // when
        RollbackRuleAttributeWithEither attribute = new RollbackRuleAttributeWithEither(valueClass);

        // then
        assertThat(attribute.getExceptionName()).isEqualTo(valueClass.getName());
    }

    @Test
    void testGetDepth_exactMatch() {
        // given
        Class<?> valueClass = RuntimeException.class;
        RollbackRuleAttributeWithEither attribute = new RollbackRuleAttributeWithEither(valueClass);

        // when
        int depth = attribute.getDepth(RuntimeException.class, 0);

        // then
        assertThat(depth).isEqualTo(0);
    }

    @Test
    void testGetDepth_subclassMatch() {
        // given
        Class<?> valueClass = Exception.class;
        RollbackRuleAttributeWithEither attribute = new RollbackRuleAttributeWithEither(valueClass);

        // when
        int depth = attribute.getDepth(RuntimeException.class, 0);

        // then
        assertThat(depth).isEqualTo(1);
    }

    @Test
    void testGetDepth_noMatch() {
        // given
        Class<?> valueClass = Error.class;
        RollbackRuleAttributeWithEither attribute = new RollbackRuleAttributeWithEither(valueClass);

        // when
        int depth = attribute.getDepth(RuntimeException.class, 0);

        // then
        assertThat(depth).isEqualTo(-1);
    }

    @Test
    void testGetDepth_stringPattern() {
        // given
        String pattern = "RuntimeException";
        RollbackRuleAttributeWithEither attribute = new RollbackRuleAttributeWithEither(pattern);

        // when
        int depth = attribute.getDepth(RuntimeException.class, 0);

        // then
        assertThat(depth).isEqualTo(0);
    }

    @Test
    void testEquals_sameObject() {
        // given
        RollbackRuleAttributeWithEither attribute = new RollbackRuleAttributeWithEither(RuntimeException.class);

        // when
        boolean result = attribute.equals(attribute);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void testEquals_equalObject() {
        // given
        RollbackRuleAttributeWithEither attribute1 = new RollbackRuleAttributeWithEither(RuntimeException.class);
        RollbackRuleAttributeWithEither attribute2 = new RollbackRuleAttributeWithEither(RuntimeException.class);

        // when
        boolean result = attribute1.equals(attribute2);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void testEquals_differentObject() {
        // given
        RollbackRuleAttributeWithEither attribute1 = new RollbackRuleAttributeWithEither(RuntimeException.class);
        RollbackRuleAttributeWithEither attribute2 = new RollbackRuleAttributeWithEither(Exception.class);

        // when
        boolean result = attribute1.equals(attribute2);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void testEquals_nullObject() {
        // given
        RollbackRuleAttributeWithEither attribute = new RollbackRuleAttributeWithEither(RuntimeException.class);

        // when
        boolean result = attribute.equals(null);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void testEquals_differentType() {
        // given
        RollbackRuleAttributeWithEither attribute = new RollbackRuleAttributeWithEither(RuntimeException.class);
        Object other = new Object();

        // when
        boolean result = attribute.equals(other);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void testHashCode() {
        // given
        String valueName = "java.lang.RuntimeException";
        RollbackRuleAttributeWithEither attribute = new RollbackRuleAttributeWithEither(valueName);

        // when
        int hashCode = attribute.hashCode();

        // then
        assertThat(hashCode).isEqualTo(valueName.hashCode());
    }

    @Test
    void testToString() {
        // given
        RollbackRuleAttributeWithEither attribute = new RollbackRuleAttributeWithEither(RuntimeException.class);

        // when
        String result = attribute.toString();

        // then
        assertThat(result).contains("RollbackRuleAttributeWithEither");
        assertThat(result).contains(RuntimeException.class.getName());
    }
}
