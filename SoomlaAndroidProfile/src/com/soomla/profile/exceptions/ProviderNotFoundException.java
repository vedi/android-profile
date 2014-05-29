package com.soomla.profile.exceptions;

/**
 * Created by refaelos on 29/05/14.
 */
public class ProviderNotFoundException extends Exception {
    public final String Provider;

    public ProviderNotFoundException(String provider) {
        Provider = provider;
    }
}
