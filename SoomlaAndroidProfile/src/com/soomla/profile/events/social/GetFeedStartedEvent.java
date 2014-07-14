package com.soomla.profile.events.social;

import com.soomla.profile.social.ISocialProvider;

/**
 * Created by oriargov on 7/14/14.
 */
public class GetFeedStartedEvent extends BaseSocialActionEvent {
    public GetFeedStartedEvent(ISocialProvider.SocialActionType getFeedType) {
        super(getFeedType);
    }
}
