package org.epo.cne.transactional.support;

import org.springframework.lang.Nullable;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.support.DelegatingTransactionDefinition;

import java.io.Serializable;
import java.util.Collection;

public abstract class DelegatingTransactionAttributeWithEither extends DelegatingTransactionDefinition
        implements TransactionAttribute, Serializable {

    private final TransactionAttribute targetAttribute;


    /**
     * Create a DelegatingTransactionAttribute for the given target attribute.
     *
     * @param targetAttribute the target TransactionAttribute to delegate to
     */
    public DelegatingTransactionAttributeWithEither(TransactionAttribute targetAttribute) {
        super(targetAttribute);
        this.targetAttribute = targetAttribute;
    }


    @Override
    @Nullable
    public String getQualifier() {
        return this.targetAttribute.getQualifier();
    }

    @Override
    public Collection<String> getLabels() {
        return this.targetAttribute.getLabels();
    }

    @Override
    public boolean rollbackOn(Throwable ex) {
        return this.targetAttribute.rollbackOn(ex);
    }

    public boolean rollbackOnErrorValue(Class<?> error) {
        if (this.targetAttribute instanceof RuleBasedTransactionAttributeWithEither) {
            RuleBasedTransactionAttributeWithEither ruleBasedTransactionAttributeWithEither = (RuleBasedTransactionAttributeWithEither) this.targetAttribute;
            return ruleBasedTransactionAttributeWithEither.rollbackOnErrorValue(error);
        }

        return false;
    }

}