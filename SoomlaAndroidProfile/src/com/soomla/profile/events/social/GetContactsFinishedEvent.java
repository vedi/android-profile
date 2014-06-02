package com.soomla.profile.events.social;

import com.soomla.profile.domain.UserProfile;
import com.soomla.profile.social.ISocialProvider;

import java.util.List;

/**
 * Created by oriargov on 6/2/14.
 */
public class GetContactsFinishedEvent extends BaseSocialActionEvent {
    public final List<UserProfile> Contacts;
    public GetContactsFinishedEvent(ISocialProvider.SocialActionType socialActionType, List<UserProfile> contacts) {
        super(socialActionType);
        this.Contacts = contacts;
    }
}
