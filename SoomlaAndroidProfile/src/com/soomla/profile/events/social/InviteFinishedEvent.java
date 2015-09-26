package com.soomla.profile.events.social;

import com.soomla.profile.domain.IProvider;
import com.soomla.profile.social.ISocialProvider;

import java.util.List;

public class InviteFinishedEvent extends BaseSocialActionEvent {
    /**
     * a Description of the reason for failure
     */
    public final String RequestId;

    public final List<String> InvitedIds;

    /**
     * Constructor
     *  @param provider The provider on which the get feed process has
     * @param getFeedType The social action preformed
     * @param requestId identifier of created app request
     * @param invitedIds List of recipients of this request
     * @param payload an identification String sent from the caller of the action
     */
    public InviteFinishedEvent(IProvider.Provider provider,
                              ISocialProvider.SocialActionType getFeedType, String requestId, List<String> invitedIds,
                              String payload) {
        super(provider, getFeedType, payload);
        RequestId = requestId;
        InvitedIds = invitedIds;
    }
}
