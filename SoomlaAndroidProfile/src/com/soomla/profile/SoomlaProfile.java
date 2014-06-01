/*
 * Copyright (C) 2012 Soomla Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.soomla.profile;

import android.app.Activity;

import com.soomla.blueprint.rewards.Reward;
import com.soomla.profile.domain.UserProfile;
import com.soomla.profile.exceptions.ProviderNotFoundException;
import com.soomla.profile.exceptions.UserProfileNotFoundException;

/**
 * Created by oriargov on 5/28/14.
 */
public class SoomlaProfile {

    public void initialize() {
        mAuthController = new AuthController();
        mSocialController = new SocialController();
    }


    public void login(Activity activity, final IProvider.Provider provider, final boolean setAsDefault) throws ProviderNotFoundException {
        login(activity, provider, setAsDefault, null);
    }

    // if you want your reward to be given more than once, make it repeatable
    public void login(Activity activity, final IProvider.Provider provider, final boolean setAsDefault, final Reward reward) throws ProviderNotFoundException {
        try {
            mAuthController.login(activity, provider, setAsDefault, reward);
        } catch (ProviderNotFoundException e) {
            mSocialController.login(activity, provider, setAsDefault, reward);
        }
    }

    public void logout(final IProvider.Provider provider) throws ProviderNotFoundException {
        try {
            mAuthController.logout(provider);
        } catch (ProviderNotFoundException e) {
            mSocialController.logout(provider);
        }
    }

    public UserProfile getUserProfileLocally(IProvider.Provider provider) throws UserProfileNotFoundException {
        try {
            return mAuthController.getUserProfileLocally(provider);
        } catch (UserProfileNotFoundException e) {
            return mSocialController.getUserProfileLocally(provider);
        }
    }

    public void updateStatus(Activity activity, IProvider.Provider provider, String status, final Reward reward) throws ProviderNotFoundException {
        mSocialController.updateStatus(activity, provider, status, reward);
    }

    /** Private Members **/

    private AuthController mAuthController;
    private SocialController mSocialController;


    /** singleton **/

    private static final SoomlaProfile mInstance = new SoomlaProfile();
    public static SoomlaProfile getInstance() {
        return mInstance;
    }

    private static final String TAG = "SOOMLA SoomlaProfile";
}
