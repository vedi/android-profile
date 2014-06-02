package com.soomla.profile.social.socialauth;


/**
 * Created by refaelos on 01/06/14.
 */
public class SoomlaSAGoogle extends SoomlaSocialAuth {

    @Override
    public Provider getProvider() {
        return Provider.GOOGLE;
    }
}
