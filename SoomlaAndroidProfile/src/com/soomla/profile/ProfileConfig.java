package com.soomla.profile;

import java.util.HashMap;

/**
 * Created by refaelos on 29/05/14.
 */
public class ProfileConfig {
    public final HashMap<String, ProfileConfig> ProfileConfigs;

    public ProfileConfig(HashMap<String, ProfileConfig> profileConfigs) {
        ProfileConfigs = profileConfigs;
    }


    public class ProviderConfig {
        private String mAppId;
        private String mSecret;
    }
}
