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
import android.graphics.Bitmap;

import com.soomla.blueprint.rewards.Reward;
import com.soomla.profile.domain.IProvider;
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


    public void login(Activity activity, final IProvider.Provider provider) throws ProviderNotFoundException {
        login(activity, provider, null);
    }

    // if you want your reward to be given more than once, make it repeatable
    public void login(Activity activity, final IProvider.Provider provider, final Reward reward) throws ProviderNotFoundException {
        try {
            mAuthController.login(activity, provider, reward);
        } catch (ProviderNotFoundException e) {
            mSocialController.login(activity, provider, reward);
        }
    }

    public void logout(final IProvider.Provider provider) throws ProviderNotFoundException {
        try {
            mAuthController.logout(provider);
        } catch (ProviderNotFoundException e) {
            mSocialController.logout(provider);
        }
    }

    public UserProfile getStoredUserProfile(IProvider.Provider provider) throws UserProfileNotFoundException {
        try {
            return mAuthController.getStoredUserProfile(provider);
        } catch (UserProfileNotFoundException e) {
            return mSocialController.getStoredUserProfile(provider);
        }
    }

    public void updateStatus(IProvider.Provider provider, String status, final Reward reward) throws ProviderNotFoundException {
        mSocialController.updateStatus(provider, status, reward);
    }

    public void updateStory(IProvider.Provider provider, String message, String name, String caption,
                            String description, String link, String picture,
                            final Reward reward) throws ProviderNotFoundException {
        mSocialController.updateStory(provider, message, name, caption, description, link, picture, reward);
    }

    public void uploadImage(IProvider.Provider provider,
                            String message, String fileName, Bitmap bitmap, int jpegQuality,
                            final Reward reward) throws ProviderNotFoundException {
        mSocialController.uploadImage(provider, message, fileName, bitmap, jpegQuality, reward);
    }

    public void uploadImage(IProvider.Provider provider,
                            String message, String filePath,
                            final Reward reward) throws ProviderNotFoundException {
        mSocialController.uploadImage(provider, message, filePath, reward);
    }

    public void getContacts(IProvider.Provider provider, final Reward reward) throws ProviderNotFoundException {
        mSocialController.getContacts(provider, reward);
    }

//    public void getFeeds(IProvider.Provider provider, final Reward reward) throws ProviderNotFoundException {
//        mSocialController.getFeeds(provider, reward);
//    }

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
