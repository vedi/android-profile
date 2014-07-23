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
import android.content.Intent;
import android.graphics.Bitmap;

import com.soomla.BusProvider;
import com.soomla.SoomlaUtils;
import com.soomla.profile.domain.IProvider;
import com.soomla.profile.domain.UserProfile;
import com.soomla.profile.events.social.GetContactsFailedEvent;
import com.soomla.profile.events.social.GetContactsFinishedEvent;
import com.soomla.profile.events.social.GetContactsStartedEvent;
import com.soomla.profile.events.social.GetFeedFailedEvent;
import com.soomla.profile.events.social.GetFeedFinishedEvent;
import com.soomla.profile.events.social.GetFeedStartedEvent;
import com.soomla.profile.events.social.SocialActionFailedEvent;
import com.soomla.profile.events.social.SocialActionFinishedEvent;
import com.soomla.profile.events.social.SocialActionStartedEvent;
import com.soomla.profile.exceptions.ProviderNotFoundException;
import com.soomla.profile.social.ISocialProvider;
import com.soomla.profile.social.SocialCallbacks;
import com.soomla.rewards.Reward;

import java.util.List;

/**
 * A class that loads all social providers and performs social
 * actions on with them.  This class wraps the provider's social
 * actions in order to connect them to user profile data and rewards.
 *
 * Inheritance: SocialController > AuthController > ProviderLoader
 */
public class SocialController extends AuthController<ISocialProvider> {

    /**
     * Constructor
     *
     * Loads all social providers
     */
    public SocialController() {
        if (!loadProviders("com.soomla.social.provider", "com.soomla.profile.social.")) {
            String msg = "You don't have a ISocialProvider service attached. " +
                    "Decide which ISocialProvider you want, add it to AndroidManifest.xml " +
                    "and add its jar to the path.";
            SoomlaUtils.LogDebug(TAG, msg);
        }
    }

    /**
     * Shares the given status to the user's feed
     *
     * @param provider the provider to use
     * @param status the text to share
     * @param reward the reward to grant for sharing
     * @throws ProviderNotFoundException
     */
    public void updateStatus(final IProvider.Provider provider, String status, final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        final ISocialProvider.SocialActionType updateStatusType = ISocialProvider.SocialActionType.UPDATE_STATUS;
        BusProvider.getInstance().post(new SocialActionStartedEvent(provider, updateStatusType));
        socialProvider.updateStatus(status, new SocialCallbacks.SocialActionListener() {
            @Override
            public void success() {
                BusProvider.getInstance().post(new SocialActionFinishedEvent(provider, updateStatusType));

                if (reward != null) {
                    reward.give();
                }
            }

            @Override
            public void fail(String message) {
                BusProvider.getInstance().post(new SocialActionFailedEvent(provider, updateStatusType, message));
            }
        });
    }

    /**
     * Shares a story to the user's feed.  This is very oriented for Facebook.
     *
     * @param provider
     * @param message
     * @param name
     * @param caption
     * @param description
     * @param link
     * @param picture
     * @param reward
     * @throws ProviderNotFoundException
     */
    public void updateStory(final IProvider.Provider provider, String message, String name, String caption, String description,
                            String link, String picture, final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        final ISocialProvider.SocialActionType updateStoryType = ISocialProvider.SocialActionType.UPDATE_STORY;
        BusProvider.getInstance().post(new SocialActionStartedEvent(provider, updateStoryType));
        socialProvider.updateStory(message, name, caption, description, link, picture,
                new SocialCallbacks.SocialActionListener() {
                    @Override
                    public void success() {
                        BusProvider.getInstance().post(new SocialActionFinishedEvent(provider, updateStoryType));

                        if (reward != null) {
                            reward.give();
                        }
                    }

                    @Override
                    public void fail(String message) {
                        BusProvider.getInstance().post(new SocialActionFailedEvent(provider, updateStoryType, message));
                    }
                }
        );
    }

    /**
     * Shares a photo to the user's feed.  This is very oriented for Facebook.
     *
     * @param provider The provider to use
     * @param message A text that will accompany the image
     * @param fileName The desired image's file name
     * @param bitmap The image to share
     * @param jpegQuality The image's numeric quality
     * @param reward The reward to grant for sharing the photo
     * @throws ProviderNotFoundException
     */
    public void uploadImage(final IProvider.Provider provider,
                            String message, String fileName, Bitmap bitmap, int jpegQuality,
                            final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        final ISocialProvider.SocialActionType uploadImageType = ISocialProvider.SocialActionType.UPLOAD_IMAGE;
        BusProvider.getInstance().post(new SocialActionStartedEvent(provider, uploadImageType));
        socialProvider.uploadImage(message, fileName, bitmap, jpegQuality, new SocialCallbacks.SocialActionListener() {
                    @Override
                    public void success() {
                        BusProvider.getInstance().post(new SocialActionFinishedEvent(provider, uploadImageType));

                        if (reward != null) {
                            reward.give();
                        }
                    }

                    @Override
                    public void fail(String message) {
                        BusProvider.getInstance().post(new SocialActionFailedEvent(provider, uploadImageType, message));
                    }
                }
        );
    }

    /**
     * Shares a photo to the user's feed.  This is very oriented for Facebook.
     *
     * @param provider The provider to use
     * @param message A text that will accompany the image
     * @param filePath The desired image's location on the device
     * @param reward The reward to grant for sharing the photo
     * @throws ProviderNotFoundException
     */
    public void uploadImage(final IProvider.Provider provider,
                            String message, String filePath,
                            final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        final ISocialProvider.SocialActionType uploadImageType = ISocialProvider.SocialActionType.UPLOAD_IMAGE;
        BusProvider.getInstance().post(new SocialActionStartedEvent(provider, uploadImageType));
        socialProvider.uploadImage(message, filePath, new SocialCallbacks.SocialActionListener() {
                    @Override
                    public void success() {
                        BusProvider.getInstance().post(new SocialActionFinishedEvent(provider, uploadImageType));

                        if (reward != null) {
                            reward.give();
                        }
                    }

                    @Override
                    public void fail(String message) {
                        BusProvider.getInstance().post(new SocialActionFailedEvent(provider, uploadImageType, message));
                    }
                }
        );
    }

    /**
     * Fetches the user's contact list
     *
     * @param provider The provider to use
     * @param reward The reward to grant
     * @throws ProviderNotFoundException
     */
    public void getContacts(final IProvider.Provider provider,
                            final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        final ISocialProvider.SocialActionType getContactsType = ISocialProvider.SocialActionType.GET_CONTACTS;
        BusProvider.getInstance().post(new GetContactsStartedEvent(provider, getContactsType));
        socialProvider.getContacts(new SocialCallbacks.ContactsListener() {
               @Override
               public void success(List<UserProfile> contacts) {
                   BusProvider.getInstance().post(new GetContactsFinishedEvent(provider, getContactsType, contacts));

                   if (reward != null) {
                       reward.give();
                   }
               }

               @Override
               public void fail(String message) {
                   BusProvider.getInstance().post(new GetContactsFailedEvent(provider, getContactsType, message));
               }
            }
        );
    }

    /**
     * Fetches the user's feed.
     *
     * @param provider The provider to use
     * @param reward The reward to grant
     * @throws ProviderNotFoundException
     */
    public void getFeed(final IProvider.Provider provider,
                            final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        final ISocialProvider.SocialActionType getFeedType = ISocialProvider.SocialActionType.GET_FEED;
        BusProvider.getInstance().post(new GetFeedStartedEvent(provider, getFeedType));
        socialProvider.getFeed(new SocialCallbacks.FeedListener() {
               @Override
               public void success(List<String> feedPosts) {
                   BusProvider.getInstance().post(new GetFeedFinishedEvent(provider, getFeedType, feedPosts));

                   if (reward != null) {
                       reward.give();
                   }
               }

               @Override
               public void fail(String message) {
                   BusProvider.getInstance().post(new GetFeedFailedEvent(provider, getFeedType, message));
               }
           }
        );
    }

    private static final String TAG = "SOOMLA SocialController";
}
