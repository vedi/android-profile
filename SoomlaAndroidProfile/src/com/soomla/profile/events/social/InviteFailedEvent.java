package com.soomla.profile.events.social;


import com.soomla.profile.domain.IProvider;
import com.soomla.profile.social.ISocialProvider;

public class InviteFailedEvent extends BaseSocialActionEvent {
    /**
     * a Description of the reason for failure
     */
    public final String ErrorDescription;

    /**
     * Constructor
     *  @param provider The provider on which the get feed process has
     * @param getFeedType The social action preformed
     * @param errorDescription a Description of the reason for failure
     * @param payload an identification String sent from the caller of the action
     */
    public InviteFailedEvent(IProvider.Provider provider,
                              ISocialProvider.SocialActionType getFeedType,
                              String errorDescription, String payload) {
        super(provider, getFeedType, payload);
        ErrorDescription = errorDescription;
    }
}
