package com.emedina.transactional.support;

import io.vavr.control.Either;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;

import java.util.List;

/**
 * Extension of the {@link RuleBasedTransactionAttribute} that allows to use the {@link Either} type.
 *
 * @author Enrique Medina Montenegro
 */
public class RuleBasedTransactionAttributeWithEither extends RuleBasedTransactionAttribute {

    @Getter
    @Setter
    @Nullable
    private List<RollbackRuleAttributeWithEither> rollbackRulesWithEither;


    private RuleBasedTransactionAttributeWithEither(RuleBasedTransactionAttribute ruleBasedTransactionAttribute) {
        super(ruleBasedTransactionAttribute);
    }

    public static RuleBasedTransactionAttributeWithEither from(RuleBasedTransactionAttribute ruleBasedTransactionAttribute) {
        return new RuleBasedTransactionAttributeWithEither(ruleBasedTransactionAttribute);
    }

    /**
     * Winning rule is the shallowest rule (that is, the closest in the
     * inheritance hierarchy to the exception). If no rule applies (-1),
     * return false.
     *
     * @see TransactionAttribute#rollbackOn(java.lang.Throwable)
     */
    @Override
    public boolean rollbackOn(Throwable ex) {
        RollbackRuleAttributeWithEither winner = null;
        int deepest = Integer.MAX_VALUE;

        if (this.rollbackRulesWithEither != null) {
            for (RollbackRuleAttributeWithEither rule : this.rollbackRulesWithEither) {
                int depth = rule.getDepth(ex.getClass(), 0);
                if (depth >= 0 && depth < deepest) {
                    deepest = depth;
                    winner = rule;
                }
            }
        }

        // No rule matches for Either, but need to check in delegated as well.
        if (winner == null) {
            return super.rollbackOn(ex);
        }

        return !(winner instanceof NoRollbackRuleAttributeWithEither);
    }

    /**
     * Winning rule is the shallowest rule (that is, the closest in the
     * inheritance hierarchy to the exception). If no rule applies (-1),
     * return false.
     * <p>When used with the {@link Either} type, allows for error values. </p>
     *
     * @param error the error value
     * @return true if the error value should trigger a rollback
     */
    public boolean rollbackOnErrorValue(Class<?> error) {
        RollbackRuleAttributeWithEither winner = null;
        int deepest = Integer.MAX_VALUE;

        if (this.rollbackRulesWithEither != null) {
            for (RollbackRuleAttributeWithEither rule : this.rollbackRulesWithEither) {
                int depth = rule.getDepth(error, 0);
                if (depth >= 0 && depth < deepest) {
                    deepest = depth;
                    winner = rule;
                }
            }
        }

        // No rule matches for Either and delegate only provides rules for Throwable.
        if (winner == null) {
            return false;
        }

        return !(winner instanceof NoRollbackRuleAttributeWithEither);
    }

}
