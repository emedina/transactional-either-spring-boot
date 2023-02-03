package org.epo.cne.transactional.support;

import org.epo.cne.sharedkernel.transactional.Transactional;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.SpringTransactionAnnotationParser;
import org.springframework.transaction.interceptor.TransactionAttribute;

import java.lang.reflect.AnnotatedElement;

@SuppressWarnings("serial")
public class SpringTransactionAnnotationParserWithEither extends SpringTransactionAnnotationParser {

    @Override
    public boolean isCandidateClass(Class<?> targetClass) {
        return AnnotationUtils.isCandidateClass(targetClass, Transactional.class);
    }

    @Override
    @Nullable
    public TransactionAttribute parseTransactionAnnotation(AnnotatedElement element) {
        AnnotationAttributes attributes = AnnotatedElementUtils.findMergedAnnotationAttributes(
                element, Transactional.class, false, false);
        if (attributes != null) {
            // Override with "real" Spring types.
            attributes.put("propagation", Propagation.valueOf(attributes.get("propagation").toString()));
            attributes.put("isolation", Isolation.valueOf(attributes.get("isolation").toString()));
            return super.parseTransactionAnnotation(attributes);
        } else {
            return null;
        }
    }

}
