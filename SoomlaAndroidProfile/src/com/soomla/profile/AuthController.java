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
import com.soomla.data.KeyValueStorage;
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
import com.soomla.rewards.Reward;

import java.util.Map;

/**
 * A class that loads all authentication providers and performs authentication
 * actions on with them.  This class wraps the provider's authentication
 * actions in order to connect them to user profile data and rewards.
 */
public class AuthController<T extends IAuthProvider> extends ProviderLoader<T> {

    private static final String DB_KEY_PREFIX = "soomla.profile";

    /**
     * Constructor
     *
     * Loads all authentication providers
     * @param usingExternalProvider {@link SoomlaProfile#initialize}
     */
    public AuthController(boolean usingExternalProvider, Map<IProvider.Provider, ? extends Map<String, String>> profileParams) {
        if(usingExternalProvider) {
            SoomlaUtils.LogDebug(TAG, "usingExternalProvider");
        }
        else if (!loadProviders(profileParams)) {
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
     * @param activity The parent activity
     * @param provider The provider to login with
     * @param autoLogin comes "true" if user login automatically
     * @param payload  a String to receive when the function returns.
     * @param reward The reward to grant the user for logging in   @throws ProviderNotFoundException
     */
    public void login(final Activity activity, final IProvider.Provider provider, final boolean autoLogin, final String payload, final Reward reward) throws ProviderNotFoundException {
        final IAuthProvider authProvider = getProvider(provider);

        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                setLoggedInForProvider(provider, false);
                BusProvider.getInstance().post(new LoginStartedEvent(provider, autoLogin, payload));
                authProvider.login(activity, new AuthCallbacks.LoginListener() {
                    @Override
                    public void success(final IProvider.Provider provider) {
                        afterLogin(provider, authProvider, autoLogin, payload, reward);
                    }

                    @Override
                    public void fail(String message) {
                        BusProvider.getInstance().post(new LoginFailedEvent(provider, message, autoLogin, payload));
                    }

                    @Override
                    public void cancel() {
                        BusProvider.getInstance().post(new LoginCancelledEvent(provider, autoLogin, payload));
                    }
                });
            }
        });

    }

    private void afterLogin(final IProvider.Provider provider,
                            IAuthProvider authProvider, final boolean autoLogin, final String payload, final Reward reward) {
        authProvider.getUserProfile(new AuthCallbacks.UserProfileListener() {
            @Override
            public void success(UserProfile userProfile) {
                UserProfileStorage.setUserProfile(userProfile);
                setLoggedInForProvider(provider, true);
                BusProvider.getInstance().post(new LoginFinishedEvent(userProfile, autoLogin, payload));

                if (reward != null) {
                    reward.give();
                }
            }

            @Override
            public void fail(String message) {
                BusProvider.getInstance().post(new LoginFailedEvent(provider, message, autoLogin, payload));
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

        if (!isLoggedIn(provider) && userProfile == null) {
            return;
        }

        BusProvider.getInstance().post(new LogoutStartedEvent(provider));
        setLoggedInForProvider(provider, false);

        if (!isLoggedIn(provider)) {
            UserProfileStorage.removeUserProfile(userProfile);
            BusProvider.getInstance().post(new LogoutFinishedEvent(provider));
            return;
        }

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
     * @param provider The provider to get the stored user profile for
     * @return The user profile for the given provider
     */
    public UserProfile getStoredUserProfile(IProvider.Provider provider) {
        UserProfile userProfile = UserProfileStorage.getUserProfile(provider);
        if (userProfile == null) {
            return null;
        }
        return UserProfileStorage.getUserProfile(provider);
    }

    /**
     * Checks if the user is logged in the given provider
     *
     * @deprecated Use isLoggedIn(IProvider.Provider provider) instead
     * @param activity The parent activity
     * @param provider The provider to check
     * @return true if the user is logged in, false otherwise
     * @throws ProviderNotFoundException if the given provider is not loaded
     */
    @Deprecated
    public boolean isLoggedIn(final Activity activity, IProvider.Provider provider) throws ProviderNotFoundException {
        return this.isLoggedIn(provider);
    }

    /**
     * Checks if the user is logged in the given provider
     *
     * @param provider The provider to check
     * @return true if the user is logged in, false otherwise
     * @throws ProviderNotFoundException if the given provider is not loaded
     */
    public boolean isLoggedIn(IProvider.Provider provider) throws ProviderNotFoundException {
        final IAuthProvider authProvider = getProvider(provider);
        return authProvider.isLoggedIn();
    }

    /**
     * Perform login to providers, if (where) it's needed.
     */
    @SuppressWarnings("ConstantConditions")
    public void settleAutoLogin(Activity activity) {
        for (Map.Entry<IProvider.Provider, T> entry : this.mProviders.entrySet()) {
            T authProvider = entry.getValue();
            if (authProvider.isAutoLogin()) {
                IProvider.Provider provider = entry.getKey();
                if (this.wasLoggedInWithProvider(provider)) {
                    String payload = "";
                    Reward reward = null;
                    if (authProvider.isLoggedIn()) {
                        setLoggedInForProvider(provider, false);
                        BusProvider.getInstance().post(new LoginStartedEvent(provider, true, payload));
                        afterLogin(provider, authProvider, true, payload, reward);
                    } else {
                        login(activity, provider, true, payload, reward);
                    }
                }
            }
        }
    }

    private void setLoggedInForProvider(IProvider.Provider provider, boolean value) {
        String key = getLoggedInStorageKeyForProvider(provider);
        if (value) {
            KeyValueStorage.setValue(key, "true");
        } else {
            KeyValueStorage.deleteKeyValue(key);
        }
    }

    private boolean wasLoggedInWithProvider(IProvider.Provider provider) {
        return "true".equals(KeyValueStorage.getValue(getLoggedInStorageKeyForProvider(provider)));
    }

    private String getLoggedInStorageKeyForProvider(IProvider.Provider provider) {
        return String.format("%s.%s.%s", DB_KEY_PREFIX, provider.toString(), "loggedIn");
    }

    private static final String TAG = "SOOMLA AuthController";
}
