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

package com.soomla.profile.events.auth;


import com.soomla.profile.domain.IProvider;
import com.soomla.profile.domain.UserProfile;

/**
 * This event is fired when the login process finishes successfully
 */
public class LoginFinishedEvent {

    /**
     * Constructor
     *
     * @param userProfile The user's profile from the logged in provider
     */
    public LoginFinishedEvent(UserProfile userProfile) {
        UserProfile = userProfile;
    }

    /**
     * Retrieves the provider to which the user has logged-in
     *
     * @return The provider to which the user has logged-in
     */
    public IProvider.Provider getProvider() {
        return UserProfile.getProvider();
    }

    /**
     * The user's profile from the logged in provider
     */
    public final UserProfile UserProfile;
}
