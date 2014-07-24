package com.soomla.profile.events.social;

import com.soomla.profile.domain.IProvider;
import com.soomla.profile.social.ISocialProvider;

/**
 * Created by oriargov on 7/24/14.
 */
public class SocialActionCancelledEvent extends BaseSocialActionEvent {
    public SocialActionCancelledEvent(IProvider.Provider provider, ISocialProvider.SocialActionType socialActionType) {
        super(provider, socialActionType);
    }
}
