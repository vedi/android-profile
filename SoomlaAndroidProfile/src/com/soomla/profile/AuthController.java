/*
 * Copyright (C) 2012-2014 Soomla Inc.
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
import android.os.Handler;
import android.os.Looper;

import com.soomla.BusProvider;
import com.soomla.SoomlaUtils;
import com.soomla.profile.auth.AuthCallbacks;
import com.soomla.profile.auth.IAuthProvider;
import com.soomla.profile.data.UserProfileStorage;
import com.soomla.profile.domain.IProvider;
import com.soomla.profile.domain.UserProfile;
import com.soomla.profile.events.auth.LoginCancelledEvent;
import com.soomla.profile.events.auth.LoginFailedEvent;
import com.soomla.profile.events.auth.LoginFinishedEvent;
import com.soomla.profile.events.auth.LoginStartedEvent;
import com.soomla.profile.events.auth.LogoutFailedEvent;
import com.soomla.profile.events.auth.LogoutFinishedEvent;
import com.soomla.profile.events.auth.LogoutStartedEvent;
import com.soomla.profile.exceptions.ProviderNotFoundException;
import com.soomla.profile.exceptions.UserProfileNotFoundException;
import com.soomla.rewards.Reward;

/**
 * A class that loads all authentication providers and performs authentication
 * actions on with them.  This class wraps the provider's authentication
 * actions in order to connect them to user profile data and rewards.
 */
public class AuthController<T extends IAuthProvider> extends ProviderLoader<T> {

    /**
     * Constructor
     *
     * Loads all authentication providers
     * @param usingExternalProvider {@link SoomlaProfile#initialize}
     */
    public AuthController(boolean usingExternalProvider) {
        if(usingExternalProvider) {
            SoomlaUtils.LogDebug(TAG, "usingExternalProvider");
        }
        else if (!loadProviders("com.soomla.auth.provider", "com.soomla.profile.auth.")) {
            String msg = "You don't have a IAuthProvider service attached. " +
                    "Decide which IAuthProvider you want, add it to AndroidManifest.xml " +
                    "and add its jar to the path.";
            SoomlaUtils.LogDebug(TAG, msg);
        }
    }

    private final Handler mainThread = new Handler(Looper.getMainLooper());

    protected void runOnMainThread(Runnable toRun) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            toRun.run();
        } else {
            mainThread.post(toRun);
        }
    }

    /**
     * Logs into the given provider and grants the user a reward.
     *
     * @param activity The parent activiry
     * @param provider The provider to login with
     * @param reward The reward to grant the user for logging in
     * @throws ProviderNotFoundException
     */
    public void login(final Activity activity, final IProvider.Provider provider, final Reward reward) throws ProviderNotFoundException {
        final IAuthProvider authProvider = getProvider(provider);

        runOnMainThread(new Runnable() {
            @Override
            public void run() {
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
                                BusProvider.getInstance().post(new LoginFailedEvent(provider, message));
                            }
                        });
                    }

                    @Override
                    public void fail(String message) {
                        BusProvider.getInstance().post(new LoginFailedEvent(provider, message));
                    }

                    @Override
                    public void cancel() {
                        BusProvider.getInstance().post(new LoginCancelledEvent(provider));
                    }
                });
            }
        });

    }


    /**
     * Logs out of the given provider
     *
     * @param provider The provider to logout from
     * @throws ProviderNotFoundException
     */
    public void logout(final IProvider.Provider provider) throws ProviderNotFoundException {
        final IAuthProvider authProvider = getProvider(provider);
        final UserProfile userProfile = getStoredUserProfile(provider);

        BusProvider.getInstance().post(new LogoutStartedEvent(provider));
        authProvider.logout(new AuthCallbacks.LogoutListener() {
            @Override
            public void success() {
                if (userProfile != null) {
                    UserProfileStorage.removeUserProfile(userProfile);
                }
                // if caller needs stuff from the user, they should get it before logout
                // pass only the provider here
                BusProvider.getInstance().post(new LogoutFinishedEvent(provider));
            }

            @Override
            public void fail(String message) {
                BusProvider.getInstance().post(new LogoutFailedEvent(provider, message));
            }
        });
    }

    /**
     * Fetches the user profile for the given provider from the device's storage.
     *
     * @param provider
     * @return The user profile for the given provider
     */
    public UserProfile getStoredUserProfile(IProvider.Provider provider) {
        UserProfile userProfile = UserProfileStorage.getUserProfile(provider);
        if (userProfile == null) {
            return null;
        }
        return UserProfileStorage.getUserProfile(provider);
    }

    private static final String TAG = "SOOMLA AuthController";
}
