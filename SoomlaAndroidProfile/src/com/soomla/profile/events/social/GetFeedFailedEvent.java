package com.soomla.profile.events.social;

import com.soomla.profile.social.ISocialProvider;

/**
 * Created by oriargov on 7/14/14.
 */
public class GetFeedFailedEvent extends BaseSocialActionEvent {
    public final String ErrorDescription;
    public GetFeedFailedEvent(ISocialProvider.SocialActionType getFeedType, String errorDescription) {
        super(getFeedType);
        ErrorDescription = errorDescription;
    }
}
