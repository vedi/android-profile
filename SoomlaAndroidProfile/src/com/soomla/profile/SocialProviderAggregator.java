package com.soomla.profile;

import com.soomla.profile.social.ISocialProvider;

/**
 * Created by refaelos on 29/05/14.
 */
public abstract class SocialProviderAggregator extends ProviderAggregator implements ISocialProvider {
    public SocialProviderAggregator() {
        super();
    }
}
