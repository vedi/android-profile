package com.soomla.profile.events.social;

import com.soomla.profile.domain.UserProfile;
import com.soomla.profile.social.ISocialProvider;

import java.util.List;

/**
 * Created by oriargov on 7/14/14.
 */
public class GetFeedFinishedEvent extends BaseSocialActionEvent {
    // todo: model posts, this is just the post message
    // todo: or reuse SimpleFacebook Post model
    public final List<String> Posts;

    public GetFeedFinishedEvent(ISocialProvider.SocialActionType getFeedType, List<String> feedPosts) {
        super(getFeedType);
        Posts = feedPosts;
    }
}
