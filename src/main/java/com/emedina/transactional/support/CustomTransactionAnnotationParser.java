package com.emedina.transactional.support;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.TransactionAnnotationParser;
import org.springframework.transaction.interceptor.NoRollbackRuleAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;

import com.emedina.sharedkernel.transactional.Transactional;

/**
 * Implementation of the {@link TransactionAnnotationParser} interface that
 * parses the custom {@link com.emedina.transactional.Transactional} annotation.
 * 
 * <p>This parser allows the custom annotation to be used alongside Spring's
 * standard @Transactional annotation.
 *
 * @author Enrique Medina Montenegro
 * @see org.springframework.transaction.annotation.TransactionAnnotationParser
 */
public class CustomTransactionAnnotationParser implements TransactionAnnotationParser {

    /**
     * Parse the transaction attribute for the given method or class,
     * based on the custom {@link com.emedina.transactional.Transactional} annotation.
     * 
     * @param element the annotated method or class
     * @return the configured transaction attribute, or {@code null} if none found
     */
    @Override
    public TransactionAttribute parseTransactionAnnotation(AnnotatedElement element) {
        // Look for the custom annotation
        Transactional ann = AnnotatedElementUtils.findMergedAnnotation(
            element, Transactional.class);

        if (ann != null) {
            return parseCustomTransactionalAnnotation(ann);
        }

        return null;
    }

    /**
     * Parse the custom @Transactional annotation into a TransactionAttribute.
     * 
     * @param ann the annotation to parse
     * @return the transaction attribute
     */
    private TransactionAttribute parseCustomTransactionalAnnotation(Transactional ann) {
        RuleBasedTransactionAttribute attribute = new RuleBasedTransactionAttribute();

        // Map propagation behavior
        switch (ann.propagation()) {
            case REQUIRED:
                attribute.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                break;
            case SUPPORTS:
                attribute.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
                break;
            case MANDATORY:
                attribute.setPropagationBehavior(TransactionDefinition.PROPAGATION_MANDATORY);
                break;
            case REQUIRES_NEW:
                attribute.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                break;
            case NOT_SUPPORTED:
                attribute.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
                break;
            case NEVER:
                attribute.setPropagationBehavior(TransactionDefinition.PROPAGATION_NEVER);
                break;
            case NESTED:
                attribute.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
                break;
            default:
                attribute.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        }

        // Map isolation level
        switch (ann.isolation()) {
            case DEFAULT:
                attribute.setIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT);
                break;
            case READ_UNCOMMITTED:
                attribute.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
                break;
            case READ_COMMITTED:
                attribute.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
                break;
            case REPEATABLE_READ:
                attribute.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
                break;
            case SERIALIZABLE:
                attribute.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
                break;
            default:
                attribute.setIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT);
        }

        // Set timeout and read-only flag
        attribute.setTimeout(ann.timeout());
        attribute.setReadOnly(ann.readOnly());

        // Set qualifier if specified
        if (ann.value().length() > 0) {
            attribute.setQualifier(ann.value());
        }

        // Handle rollback rules
        if (ann.rollbackFor().length > 0) {
            for (Class<?> rbRule : ann.rollbackFor()) {
                attribute.getRollbackRules().add(new RollbackRuleAttribute(rbRule));
            }
        }

        if (ann.rollbackForClassName().length > 0) {
            for (String rbRule : ann.rollbackForClassName()) {
                attribute.getRollbackRules().add(new RollbackRuleAttribute(rbRule));
            }
        }

        if (ann.noRollbackFor().length > 0) {
            for (Class<?> rbRule : ann.noRollbackFor()) {
                attribute.getRollbackRules().add(new NoRollbackRuleAttribute(rbRule));
            }
        }

        if (ann.noRollbackForClassName().length > 0) {
            for (String rbRule : ann.noRollbackForClassName()) {
                attribute.getRollbackRules().add(new NoRollbackRuleAttribute(rbRule));
            }
        }

        // Set labels if available (Spring 5.3+)
        if (ann.label().length > 0) {
            attribute.setLabels(Arrays.asList(ann.label()));
        }

        return attribute;
    }

}
