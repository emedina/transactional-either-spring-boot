package com.emedina.transactional.support;

import io.vavr.control.Either;
import org.springframework.lang.Nullable;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;

/**
 * Extension of {@link RollbackRuleAttribute} that indicates a rollback rule
 * when working with {@link Either} monadic type.
 *
 * @author Enrique Medina Montenegro
 */
public class RollbackRuleAttributeWithEither {

    private final String valuePattern;

    @Nullable
    private final Class<?> valueType;

    public RollbackRuleAttributeWithEither(String valueName) {
        this.valuePattern = valueName;
        this.valueType = null;
    }

    public RollbackRuleAttributeWithEither(Class<?> valueClazz) {
        this.valuePattern = valueClazz.getName();
        this.valueType = valueClazz;
    }

    public String getExceptionName() {
        return this.valuePattern;
    }

    public int getDepth(Class<?> valueType, int depth) {
        if (this.valueType != null) {
            if (this.valueType.equals(valueType)) {
                // Found it!
                return depth;
            }
        } else if (valueType.getName().contains(this.valuePattern)) {
            // Found it!
            return depth;
        }
        // If we've gone as far as we can go and haven't found it...
        if (valueType == Throwable.class) {
            return -1;
        }
        return this.getDepth(valueType.getSuperclass(), depth + 1);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RollbackRuleAttributeWithEither rhs)) {
            return false;
        }
        return this.valuePattern.equals(rhs.valuePattern);
    }

    @Override
    public int hashCode() {
        return this.valuePattern.hashCode();
    }

    @Override
    public String toString() {
        return "RollbackRuleAttributeWithEither with value pattern [" + this.valuePattern + "]";
    }

}
