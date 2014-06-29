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

package com.soomla.profile.auth;

import android.app.Activity;

import com.soomla.profile.domain.IProvider;

/**
 * A provider that exposes authentication capabilities.
 */
public interface IAuthProvider extends IProvider {

    /**
     * Logs in with the authentication provider
     *
     * @param activity the parent activity
     * @param loginListener a set of callbacks for the login action
     */
    void login(Activity activity, AuthCallbacks.LoginListener loginListener);

    /**
     * Fetches the user profile from the authentication provider
     *
     * @param userProfileListener a set of callbacks for this fetch action
     */
    void getUserProfile(AuthCallbacks.UserProfileListener userProfileListener);

    /**
     * Logs out of the authentication provider
     *
     * @param logoutListener a set of callbacks for the logout action
     */
    void logout(AuthCallbacks.LogoutListener logoutListener);
}
