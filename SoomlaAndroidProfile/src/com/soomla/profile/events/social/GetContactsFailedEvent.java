package com.soomla.profile.events.social;

import com.soomla.profile.social.ISocialProvider;

/**
 * Created by oriargov on 6/2/14.
 */
public class GetContactsFailedEvent extends BaseSocialActionEvent {
    public final String ErrorDescription;
    public GetContactsFailedEvent(ISocialProvider.SocialActionType socialActionType, String errorDescription) {
        super(socialActionType);
        this.ErrorDescription = errorDescription;
    }
}
