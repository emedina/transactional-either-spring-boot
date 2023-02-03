package org.epo.cne.transactional.support;

import org.epo.cne.transactional.config.EnableTransactionManagementWithEither;
import org.epo.cne.transactional.config.ProxyTransactionManagementConfigurationWithEither;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.context.annotation.AutoProxyRegistrar;
import org.springframework.transaction.config.TransactionManagementConfigUtils;
import org.springframework.util.ClassUtils;

public class TransactionManagementConfigurationSelectorWithEither
        extends AdviceModeImportSelector<EnableTransactionManagementWithEither> {

    /**
     * Returns {@link ProxyTransactionManagementConfigurationWithEither} or
     * {@code AspectJ(Jta)TransactionManagementConfiguration} for {@code PROXY}
     * and {@code ASPECTJ} values of {@link EnableTransactionManagementWithEither#mode()},
     * respectively.
     */
    @Override
    protected String[] selectImports(AdviceMode adviceMode) {
        switch (adviceMode) {
            case PROXY:
                return new String[]{AutoProxyRegistrar.class.getName(),
                        ProxyTransactionManagementConfigurationWithEither.class.getName()};
            case ASPECTJ:
                return new String[]{determineTransactionAspectClass()};
            default:
                return null;
        }
    }

    private String determineTransactionAspectClass() {
        return (ClassUtils.isPresent("jakarta.transaction.Transactional", getClass().getClassLoader()) ?
                TransactionManagementConfigUtils.JTA_TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME :
                TransactionManagementConfigUtils.TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME);
    }

}
