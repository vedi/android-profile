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
import com.soomla.profile.auth.AuthCallbacks;
import com.soomla.profile.auth.IAuthProvider;
import com.soomla.profile.data.UserProfileStorage;
import com.soomla.profile.domain.IProvider;
import com.soomla.profile.domain.UserProfile;
import com.soomla.profile.events.LoginCancelledEvent;
import com.soomla.profile.events.auth.LoginFailedEvent;
import com.soomla.profile.events.auth.LoginFinishedEvent;
import com.soomla.profile.events.auth.LoginStartedEvent;
import com.soomla.profile.events.auth.LogoutFailedEvent;
import com.soomla.profile.events.auth.LogoutFinishedEvent;
import com.soomla.profile.events.auth.LogoutStartedEvent;
import com.soomla.profile.exceptions.ProviderNotFoundException;
import com.soomla.profile.exceptions.UserProfileNotFoundException;
import com.soomla.store.BusProvider;
import com.soomla.store.StoreUtils;

/**
 * Created by oriargov on 5/28/14.
 */
public class AuthController<T extends IAuthProvider> extends ProviderLoader<T> {

    public AuthController() {
        if (!loadProviders("com.soomla.auth.provider", "com.soomla.profile.auth.")) {
            String msg = "You don't have a IAuthProvider service attached. " +
                    "Decide which IAuthProvider you want, add it to AndroidManifest.xml " +
                    "and add its jar to the path.";
            StoreUtils.LogDebug(TAG, msg);
        }
    }

    public void login(Activity activity, final IProvider.Provider provider, final Reward reward) throws ProviderNotFoundException {
        final IAuthProvider authProvider = getProvider(provider);

        BusProvider.getInstance().post(new LoginStartedEvent(provider));
        authProvider.login(activity, new AuthCallbacks.LoginListener() {
            @Override
            public void success(final IProvider.Provider provider) {
                authProvider.getUserProfile(new AuthCallbacks.UserProfileListener() {
                    @Override
                    public void success(UserProfile userProfile) {
                        UserProfileStorage.setUserProfile(userProfile);
                        BusProvider.getInstance().post(new LoginFinishedEvent(userProfile));

                        if (reward != null) {
                            reward.give();
                        }
                    }

                    @Override
                    public void fail(String message) {
                        BusProvider.getInstance().post(new LoginFailedEvent(message));
                    }
                });
            }

            @Override
            public void fail(String message) {
                BusProvider.getInstance().post(new LoginFailedEvent(message));
            }

            @Override
            public void cancel() {
                BusProvider.getInstance().post(new LoginCancelledEvent());
            }
        });
    }


    public void logout(final IProvider.Provider provider) throws ProviderNotFoundException {
        final IAuthProvider authProvider = getProvider(provider);
        UserProfile userProfile = null;
        try {
            userProfile = getUserProfileLocally(provider);
        } catch (UserProfileNotFoundException e) {
            e.printStackTrace();
        }
        final UserProfile userProfileF = userProfile;

        BusProvider.getInstance().post(new LogoutStartedEvent(provider));
        authProvider.logout(new AuthCallbacks.LogoutListener() {
            @Override
            public void success() {
                if (userProfileF != null) {
                    UserProfileStorage.removeUserProfile(userProfileF);
                }
                BusProvider.getInstance().post(new LogoutFinishedEvent(userProfileF));
            }

            @Override
            public void fail(String message) {
                BusProvider.getInstance().post(new LogoutFailedEvent(message));
            }
        });
    }

    public UserProfile getUserProfileLocally(IProvider.Provider provider) throws UserProfileNotFoundException {
        UserProfile userProfile = UserProfileStorage.getUserProfile(provider);
        if (userProfile == null) {
            throw new UserProfileNotFoundException();
        }
        return UserProfileStorage.getUserProfile(provider);
    }


    private static final String TAG = "SOOMLA AuthController";
}
