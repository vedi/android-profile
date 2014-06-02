package com.soomla.profile.events.social;

import com.soomla.profile.social.ISocialProvider;

/**
 * Created by oriargov on 6/2/14.
 */
public class GetContactsStartedEvent extends BaseSocialActionEvent {
    public GetContactsStartedEvent(ISocialProvider.SocialActionType socialActionType) {
        super(socialActionType);
    }
}
