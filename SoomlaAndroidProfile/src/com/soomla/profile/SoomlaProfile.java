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
import android.content.Context;

import com.soomla.store.SoomlaApp;

/**
 * Created by oriargov on 5/28/14.
 */
public class SoomlaProfile {

    public void initialize() {
        mAuthController = new AuthController();
        mSocialController = new SocialController();
    }

    public AuthController getAuthController() {
        return mAuthController;
    }

    public SocialController getSocialController() {
        return mSocialController;
    }


    /** Setters and Getters **/



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
