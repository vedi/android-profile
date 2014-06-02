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
import com.soomla.profile.events.social.GetContactsFailedEvent;
import com.soomla.profile.events.social.GetContactsFinishedEvent;
import com.soomla.profile.events.social.GetContactsStartedEvent;
import com.soomla.profile.events.social.SocialActionFailedEvent;
import com.soomla.profile.events.social.SocialActionFinishedEvent;
import com.soomla.profile.events.social.SocialActionStartedEvent;
import com.soomla.profile.exceptions.ProviderNotFoundException;
import com.soomla.profile.social.ISocialProvider;
import com.soomla.profile.social.SocialCallbacks;
import com.soomla.store.BusProvider;
import com.soomla.store.StoreUtils;

import java.util.List;

/**
 * Created by oriargov on 5/28/14.
 */
public class SocialController extends AuthController<ISocialProvider> {

    public SocialController() {
        if (!loadProviders("com.soomla.social.provider", "com.soomla.profile.social.")) {
            String msg = "You don't have a ISocialProvider service attached. " +
                    "Decide which ISocialProvider you want, add it to AndroidManifest.xml " +
                    "and add its jar to the path.";
            StoreUtils.LogDebug(TAG, msg);
        }
    }

    public void updateStatus(IProvider.Provider provider, String status, final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        final ISocialProvider.SocialActionType updateStatusType = ISocialProvider.SocialActionType.UpdateStatus;
        BusProvider.getInstance().post(new SocialActionStartedEvent(updateStatusType));
        socialProvider.updateStatus(status, new SocialCallbacks.SocialActionListener() {
            @Override
            public void success() {
                BusProvider.getInstance().post(new SocialActionFinishedEvent(updateStatusType));

                if (reward != null) {
                    reward.give();
                }
            }

            @Override
            public void fail(String message) {
                BusProvider.getInstance().post(new SocialActionFailedEvent(updateStatusType, message));
            }
        });
    }

    public void updateStory(IProvider.Provider provider, String message, String name, String caption, String description,
                            String link, String picture, final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        final ISocialProvider.SocialActionType updateStoryType = ISocialProvider.SocialActionType.UpdateStory;
        BusProvider.getInstance().post(new SocialActionStartedEvent(updateStoryType));
        socialProvider.updateStory(message, name, caption, description, link, picture,
                new SocialCallbacks.SocialActionListener() {
                    @Override
                    public void success() {
                        BusProvider.getInstance().post(new SocialActionFinishedEvent(updateStoryType));

                        if (reward != null) {
                            reward.give();
                        }
                    }

                    @Override
                    public void fail(String message) {
                        BusProvider.getInstance().post(new SocialActionFailedEvent(updateStoryType, message));
                    }
                }
        );
    }

    public void uploadImage(IProvider.Provider provider,
                            String message, String fileName, Bitmap bitmap, int jpegQuality,
                            final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        final ISocialProvider.SocialActionType uploadImageType = ISocialProvider.SocialActionType.UploadImage;
        BusProvider.getInstance().post(new SocialActionStartedEvent(uploadImageType));
        socialProvider.uploadImage(message, fileName, bitmap, jpegQuality, new SocialCallbacks.SocialActionListener() {
                    @Override
                    public void success() {
                        BusProvider.getInstance().post(new SocialActionFinishedEvent(uploadImageType));

                        if (reward != null) {
                            reward.give();
                        }
                    }

                    @Override
                    public void fail(String message) {
                        BusProvider.getInstance().post(new SocialActionFailedEvent(uploadImageType, message));
                    }
                }
        );
    }

    public void getContacts(IProvider.Provider provider,
                            final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        final ISocialProvider.SocialActionType getContactsType = ISocialProvider.SocialActionType.GetContacts;
        BusProvider.getInstance().post(new GetContactsStartedEvent(getContactsType));
        socialProvider.getContacts(new SocialCallbacks.ContactsListener() {
               @Override
               public void success(List<UserProfile> contacts) {
                   BusProvider.getInstance().post(new GetContactsFinishedEvent(getContactsType, contacts));

                   if (reward != null) {
                       reward.give();
                   }
               }

               @Override
               public void fail(String message) {
                   BusProvider.getInstance().post(new GetContactsFailedEvent(getContactsType, message));
               }
            }
        );
    }

//    public void getFeeds(IProvider.Provider provider,
//                            final Reward reward) throws ProviderNotFoundException {
//        final ISocialProvider socialProvider = getProvider(provider);
//
//        final ISocialProvider.SocialActionType getFeedsType = ISocialProvider.SocialActionType.GetFeeds;
//        BusProvider.getInstance().post(new GetContactsStartedEvent(getFeedsType));
//        socialProvider.getContacts(new SocialCallbacks.ContactsListener() {
//               @Override
//               public void success(List<UserProfile> contacts) {
//                   BusProvider.getInstance().post(new GetFeedsFinishedEvent(getFeedsType, contacts));
//
//                   if (reward != null) {
//                       reward.give();
//                   }
//               }
//
//               @Override
//               public void fail(String message) {
//                   BusProvider.getInstance().post(new GetFeedsFailedEvent(getFeedsType, message));
//               }
//           }
//        );
//    }

    private static final String TAG = "SOOMLA SocialController";
}
