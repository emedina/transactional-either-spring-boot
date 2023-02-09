package org.epo.cne.transactional.support;

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
