package com.emedina.transactional.support;

import io.vavr.control.Either;

/**
 * Extension of {@link RollbackRuleAttributeWithEither} that indicates a rollback rule
 * when working with {@link Either} monadic type.
 *
 * @author Enrique Medina Montenegro
 */
public class NoRollbackRuleAttributeWithEither extends RollbackRuleAttributeWithEither {

    public NoRollbackRuleAttributeWithEither(String valueName) {
        super(valueName);
    }

    public NoRollbackRuleAttributeWithEither(Class<?> valueClazz) {
        super(valueClazz);
    }

    @Override
    public String toString() {
        return "No" + super.toString();
    }

}
