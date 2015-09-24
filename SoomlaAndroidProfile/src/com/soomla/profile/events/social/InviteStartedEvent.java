package com.soomla.profile.events.social;

import com.soomla.profile.domain.IProvider;
import com.soomla.profile.social.ISocialProvider;

public class InviteStartedEvent extends BaseSocialActionEvent {
    /**
     * Constructor
     * @param provider The provider on which the get feed process started
     * @param getFeedType The social action preformed
     * @param payload an identification String sent from the caller of the action
     */
    public InviteStartedEvent(IProvider.Provider provider,
                               ISocialProvider.SocialActionType getFeedType, String payload) {
        super(provider, getFeedType, payload);
    }
}
