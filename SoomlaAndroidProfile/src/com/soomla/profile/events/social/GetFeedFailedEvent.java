package com.soomla.profile.events.social;

import com.soomla.profile.social.ISocialProvider;

/**
 * Created by oriargov on 7/14/14.
 */
public class GetFeedFailedEvent extends BaseSocialActionEvent {
    public final String Message;
    public GetFeedFailedEvent(ISocialProvider.SocialActionType getFeedType, String message) {
        super(getFeedType);
        Message = message;
    }
}
