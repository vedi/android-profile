package com.soomla.profile.events.social;

import com.soomla.profile.domain.IProvider;
import com.soomla.profile.social.ISocialProvider;

public class InviteCancelledEvent extends BaseSocialActionEvent {
    /**
     * Constructor
     *
     * @param provider The provider on which a social action was cancelled
     * @param socialActionType The social action preformed
     * @param payload an identification String sent from the caller of the action
     */
    public InviteCancelledEvent(IProvider.Provider provider,
                                      ISocialProvider.SocialActionType socialActionType, String payload) {
        super(provider, socialActionType, payload);
    }
}
