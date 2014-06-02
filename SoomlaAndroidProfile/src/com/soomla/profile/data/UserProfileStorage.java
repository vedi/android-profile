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

package com.soomla.profile.data;

import android.text.TextUtils;

import com.soomla.profile.domain.IProvider;
import com.soomla.profile.domain.UserProfile;
//import com.soomla.profile.events.DefaultUserProfileChangedEvent;
import com.soomla.profile.events.UserProfileUpdatedEvent;
import com.soomla.store.BusProvider;
import com.soomla.store.StoreUtils;
import com.soomla.store.data.StorageManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by oriargov on 5/27/14.
 */
public class UserProfileStorage {

    public static void setUserProfile(UserProfile userProfile) {
        setUserProfile(userProfile, true);
    }

    public static void setUserProfile(UserProfile userProfile, boolean notify) {
        String userProfileJSON = userProfile.toJSONObject().toString();
        String key = keyUserProfile(userProfile.getProvider());

        StorageManager.getKeyValueStorage().setValue(key, userProfileJSON);

        if (notify) {
            BusProvider.getInstance().post(new UserProfileUpdatedEvent(userProfile));
        }
    }

    public static void removeUserProfile(UserProfile userProfile) {
        String key = keyUserProfile(userProfile.getProvider());

        StorageManager.getKeyValueStorage().deleteKeyValue(key);
    }

    public static UserProfile getUserProfile(IProvider.Provider provider) {
        String userProfileJSON = StorageManager.getKeyValueStorage().getValue(keyUserProfile(provider));
        if (TextUtils.isEmpty(userProfileJSON)) {
            return null;
        }

        try {
            JSONObject upJSON = new JSONObject(userProfileJSON);
            return new UserProfile(upJSON);
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "Couldn't create UserProfile from json: " + userProfileJSON);
        }

        return null;
    }

//    public static void setDefaultProvider(IProvider.Provider provider) {
//        setDefaultProvider(provider, true);
//    }

//    public static void setDefaultProvider(IProvider.Provider provider, boolean notify) {
//        String key = keyDefaultProvider();
//
//        StorageManager.getKeyValueStorage().setValue(key, provider.toString());
//
//        if (notify) {
//            BusProvider.getInstance().post(new DefaultUserProfileChangedEvent(getUserProfile(provider)));
//        }
//    }

//    private static String keyDefaultProvider() {
//        return DB_KEY_PREFIX + "userprofile.defaultProvider";
//    }

    private static String keyUserProfile(IProvider.Provider provider) {
        return DB_KEY_PREFIX + "userprofile." + provider.toString();
    }

    private static final String DB_KEY_PREFIX = "soomla.profile.";
    private static final String TAG = "SOOMLA UserProfileStorage";
}
