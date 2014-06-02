package com.soomla.profile.exceptions;

import com.soomla.profile.domain.IProvider;

/**
 * Created by refaelos on 29/05/14.
 */
public class ProviderNotFoundException extends Exception {
    public final IProvider.Provider Provider;

    public ProviderNotFoundException(IProvider.Provider provider) {
        Provider = provider;
    }
}
