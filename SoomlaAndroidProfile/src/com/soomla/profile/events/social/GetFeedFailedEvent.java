package com.soomla.profile.events.social;

import com.soomla.profile.domain.IProvider;
import com.soomla.profile.social.ISocialProvider;

/**
 * Created by oriargov on 7/14/14.
 */
public class GetFeedFailedEvent extends BaseSocialActionEvent {
    public final String ErrorDescription;
    public GetFeedFailedEvent(IProvider.Provider provider,
                              ISocialProvider.SocialActionType getFeedType,
                              String errorDescription) {
        super(provider, getFeedType);
        ErrorDescription = errorDescription;
    }
}
