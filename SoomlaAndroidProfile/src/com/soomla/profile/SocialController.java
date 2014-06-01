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
import com.soomla.profile.events.social.SocialActionFailedEvent;
import com.soomla.profile.events.social.SocialActionFinishedEvent;
import com.soomla.profile.events.social.SocialActionStartedEvent;
import com.soomla.profile.exceptions.ProviderNotFoundException;
import com.soomla.profile.social.ISocialProvider;
import com.soomla.profile.social.SocialCallbacks;
import com.soomla.store.BusProvider;
import com.soomla.store.StoreUtils;

/**
 * Created by oriargov on 5/28/14.
 */
public class SocialController extends AuthController<ISocialProvider> {

    public SocialController() {
        if (!loadProviders("social.provider", "com.soomla.profile.social.")) {
            String msg = "You don't have a ISocialProvider service attached. " +
                    "Decide which ISocialProvider you want, add it to AndroidManifest.xml " +
                    "and add its jar to the path.";
            StoreUtils.LogDebug(TAG, msg);
        }
    }

    public void updateStatus(Activity activity, IProvider.Provider provider, String status, final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        BusProvider.getInstance().post(new SocialActionStartedEvent(ISocialProvider.SocialActionType.UpdateStatus));
        socialProvider.updateStatus(activity, status, new SocialCallbacks.SocialActionListener() {
            @Override
            public void success() {
                BusProvider.getInstance().post(new SocialActionFinishedEvent(ISocialProvider.SocialActionType.UpdateStatus));

                if (reward != null) {
                    reward.give();
                }
            }

            @Override
            public void fail(String message) {
                BusProvider.getInstance().post(new SocialActionFailedEvent(ISocialProvider.SocialActionType.UpdateStatus, message));
            }
        });
    }

    private static final String TAG = "SOOMLA SocialController";
}
