package com.soomla.profile;

import com.soomla.profile.auth.IAuthProvider;

/**
 * Created by refaelos on 29/05/14.
 */
public abstract class AuthProviderAggregator extends ProviderAggregator implements IAuthProvider {
    public AuthProviderAggregator() {
        super();
    }
}
