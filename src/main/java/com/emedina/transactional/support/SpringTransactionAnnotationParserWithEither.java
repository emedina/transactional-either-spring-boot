package com.emedina.transactional.support;

import com.emedina.sharedkernel.transactional.Transactional;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.SpringTransactionAnnotationParser;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a copy of {@link SpringTransactionAnnotationParser} with the only difference that
 * it translates the transactional attributes used in {@link Transactional} to the Spring types.
 *
 * @author Enrique Medina Montenegro
 */
@SuppressWarnings("serial")
public class SpringTransactionAnnotationParserWithEither extends SpringTransactionAnnotationParser {

    @Override
    public boolean isCandidateClass(Class<?> targetClass) {
        return AnnotationUtils.isCandidateClass(targetClass, Transactional.class);
    }

    /**
     * Overrides the default implementation to translate the transactional attributes used in
     * {@link Transactional} to the Spring types and also to add additional attributes specific to
     * the use of Either for rollback rules.
     *
     * @param element the annotated method or class
     * @return the corresponding TransactionAttribute instance, or {@code null} if none found
     */
    @Override
    @Nullable
    public TransactionAttribute parseTransactionAnnotation(AnnotatedElement element) {
        AnnotationAttributes attributes = AnnotatedElementUtils.findMergedAnnotationAttributes(
                element, Transactional.class, false, false);
        if (attributes != null) {
            // Override with "real" Spring types.
            attributes.put("propagation", Propagation.valueOf(attributes.get("propagation").toString()));
            attributes.put("isolation", Isolation.valueOf(attributes.get("isolation").toString()));

            // Add additional attributes specific to the use of Either.
            RuleBasedTransactionAttributeWithEither rbta = RuleBasedTransactionAttributeWithEither.from(
                    (RuleBasedTransactionAttribute) super.parseTransactionAnnotation(attributes));
            List<RollbackRuleAttributeWithEither> rollbackRules = new ArrayList<>();
            for (Class<?> rbRule : attributes.getClassArray("rollbackForWithEither")) {
                rollbackRules.add(new RollbackRuleAttributeWithEither(rbRule));
            }
            for (String rbRule : attributes.getStringArray("rollbackForClassNameWithEither")) {
                rollbackRules.add(new RollbackRuleAttributeWithEither(rbRule));
            }
            for (Class<?> rbRule : attributes.getClassArray("noRollbackForWithEither")) {
                rollbackRules.add(new NoRollbackRuleAttributeWithEither(rbRule));
            }
            for (String rbRule : attributes.getStringArray("noRollbackForClassNameWithEither")) {
                rollbackRules.add(new NoRollbackRuleAttributeWithEither(rbRule));
            }
            rbta.setRollbackRulesWithEither(rollbackRules);

            return rbta;
        } else {
            return null;
        }
    }

}
