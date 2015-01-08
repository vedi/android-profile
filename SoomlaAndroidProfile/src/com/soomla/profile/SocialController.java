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
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.os.Environment;

import com.soomla.BusProvider;
import com.soomla.SoomlaApp;
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A class that loads all social providers and performs social
 * actions on with them.  This class wraps the provider's social
 * actions in order to connect them to user profile data and rewards.
 * <p/>
 * Inheritance: {@link com.soomla.profile.SocialController} >
 * {@link com.soomla.profile.AuthController} >
 * {@link com.soomla.profile.ProviderLoader}
 */
public class SocialController extends AuthController<ISocialProvider> {

    /**
     * Constructor
     * <p/>
     * Loads all social providers
     * * @param usingExternalProvider {@link SoomlaProfile#initialize}
     */
    public SocialController(boolean usingExternalProvider, Map<IProvider.Provider, ? extends Map<String, String>> providerParams) {
        super(usingExternalProvider, providerParams);
        if (!usingExternalProvider && !loadProviders(providerParams, "com.soomla.profile.social.facebook.SoomlaFacebook",
                "com.soomla.profile.social.google.SoomlaGooglePlus",
                "com.soomla.profile.social.twitter.SoomlaTwitter")) {
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
     * @param status   the text to share
     * @param payload  a String to receive when the function returns.
     * @param reward   the reward to grant for sharing
     * @throws ProviderNotFoundException
     */
    public void updateStatus(final IProvider.Provider provider, String status, final String payload, final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        final ISocialProvider.SocialActionType updateStatusType = ISocialProvider.SocialActionType.UPDATE_STATUS;
        BusProvider.getInstance().post(new SocialActionStartedEvent(provider, updateStatusType, payload));
        socialProvider.updateStatus(status, new SocialCallbacks.SocialActionListener() {
            @Override
            public void success() {
                BusProvider.getInstance().post(new SocialActionFinishedEvent(provider, updateStatusType, payload));

                if (reward != null) {
                    reward.give();
                }
            }

            @Override
            public void fail(String message) {
                BusProvider.getInstance().post(new SocialActionFailedEvent(provider, updateStatusType, message, payload));
            }
        });
    }

    /**
     * Shares the given status to the user's feed.
     * Using the provider's native dialog (when available).
     *
     * @param provider the provider to use
     * @param link     the text to share
     * @param payload  a String to receive when the function returns.
     * @param reward   the reward to grant for sharing
     * @throws ProviderNotFoundException
     */
    public void updateStatusDialog(final IProvider.Provider provider, String link, final String payload, final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        final ISocialProvider.SocialActionType updateStatusType = ISocialProvider.SocialActionType.UPDATE_STATUS;
        BusProvider.getInstance().post(new SocialActionStartedEvent(provider, updateStatusType, payload));
        socialProvider.updateStatusDialog(link, new SocialCallbacks.SocialActionListener() {
            @Override
            public void success() {
                BusProvider.getInstance().post(new SocialActionFinishedEvent(provider, updateStatusType, payload));

                if (reward != null) {
                    reward.give();
                }
            }

            @Override
            public void fail(String message) {
                BusProvider.getInstance().post(new SocialActionFailedEvent(provider, updateStatusType, message, payload));
            }
        });
    }

    /**
     * Shares a story to the user's feed.  This is very oriented for Facebook.
     *
     * @param provider    The provider on which to update user's story
     * @param message     The main text which will appear in the story
     * @param name        The headline for the link which will be integrated in the
     *                    story
     * @param caption     The sub-headline for the link which will be
     *                    integrated in the story
     * @param description description The description for the link which will be
     *                    integrated in the story
     * @param link        The link which will be integrated into the user's story
     * @param picture     a Link to a picture which will be featured in the link
     * @param payload  a String to receive when the function returns.
     * @param reward      The reward which will be granted to the user upon a
     *                    successful update
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void updateStory(final IProvider.Provider provider, String message, String name, String caption, String description,
                            String link, String picture, final String payload, final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        final ISocialProvider.SocialActionType updateStoryType = ISocialProvider.SocialActionType.UPDATE_STORY;
        BusProvider.getInstance().post(new SocialActionStartedEvent(provider, updateStoryType, payload));
        socialProvider.updateStory(message, name, caption, description, link, picture,
                new SocialCallbacks.SocialActionListener() {
                    @Override
                    public void success() {
                        BusProvider.getInstance().post(new SocialActionFinishedEvent(provider, updateStoryType, payload));

                        if (reward != null) {
                            reward.give();
                        }
                    }

                    @Override
                    public void fail(String message) {
                        BusProvider.getInstance().post(new SocialActionFailedEvent(provider, updateStoryType, message, payload));
                    }
                }
        );
    }

    /**
     * Shares a story to the user's feed.  This is very oriented for Facebook.
     * Using the provider's native dialog (when available).
     *
     * @param provider    The provider on which to update user's story
     * @param name        The headline for the link which will be integrated in the
     *                    story
     * @param caption     The sub-headline for the link which will be
     *                    integrated in the story
     * @param description description The description for the link which will be
     *                    integrated in the story
     * @param link        The link which will be integrated into the user's story
     * @param picture     a Link to a picture which will be featured in the link
     * @param payload  a String to receive when the function returns.
     * @param reward      The reward which will be granted to the user upon a
     *                    successful update
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void updateStoryDialog(final IProvider.Provider provider, String name, String caption, String description,
                                  String link, String picture, final String payload, final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        final ISocialProvider.SocialActionType updateStoryType = ISocialProvider.SocialActionType.UPDATE_STORY;
        BusProvider.getInstance().post(new SocialActionStartedEvent(provider, updateStoryType, payload));
        socialProvider.updateStoryDialog(name, caption, description, link, picture,
                new SocialCallbacks.SocialActionListener() {
                    @Override
                    public void success() {
                        BusProvider.getInstance().post(new SocialActionFinishedEvent(provider, updateStoryType, payload));

                        if (reward != null) {
                            reward.give();
                        }
                    }

                    @Override
                    public void fail(String message) {
                        BusProvider.getInstance().post(new SocialActionFailedEvent(provider, updateStoryType, message, payload));
                    }
                }
        );
    }

    /**
     * Shares a photo to the user's feed.  This is very oriented for Facebook.
     *
     * @param provider The provider to use
     * @param message  A text that will accompany the image
     * @param filePath The desired image's location on the device
     * @param payload  a String to receive when the function returns.
     * @param reward   The reward to grant for sharing the photo
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void uploadImage(final IProvider.Provider provider,
                            String message, String filePath,
                            final String payload, final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        final ISocialProvider.SocialActionType uploadImageType = ISocialProvider.SocialActionType.UPLOAD_IMAGE;
        BusProvider.getInstance().post(new SocialActionStartedEvent(provider, uploadImageType, payload));
        socialProvider.uploadImage(message, filePath, new SocialCallbacks.SocialActionListener() {
                    @Override
                    public void success() {
                        BusProvider.getInstance().post(new SocialActionFinishedEvent(provider, uploadImageType, payload));

                        if (reward != null) {
                            reward.give();
                        }
                    }

                    @Override
                    public void fail(String message) {
                        BusProvider.getInstance().post(new SocialActionFailedEvent(provider, uploadImageType, message, payload));
                    }
                }
        );
    }

    /**
     * Upload image using Bitmap
     *
     * @param provider    The provider to use
     * @param message     A text that will accompany the image
     * @param fileName    The desired image's file name
     * @param bitmap      The image to share
     * @param jpegQuality The image's numeric quality
     * @param payload     a String to receive when the function returns.
     * @param reward      The reward to grant for sharing the photo
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void uploadImage(final IProvider.Provider provider,
                            String message, String fileName, Bitmap bitmap, int jpegQuality,
                            final String payload, final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        final ISocialProvider.SocialActionType uploadImageType = ISocialProvider.SocialActionType.UPLOAD_IMAGE;
        BusProvider.getInstance().post(new SocialActionStartedEvent(provider, uploadImageType, payload));

        try {
            final File tempImageFile = createTempImageFile(fileName, bitmap, jpegQuality);
            socialProvider.uploadImage(message, tempImageFile.getAbsolutePath(), new SocialCallbacks.SocialActionListener() {
                        @Override
                        public void success() {
                            BusProvider.getInstance().post(new SocialActionFinishedEvent(provider, uploadImageType, payload));

                            if (reward != null) {
                                reward.give();
                            }

                            if (tempImageFile != null){
                                tempImageFile.delete();
                            }
                        }

                        @Override
                        public void fail(String message) {
                            BusProvider.getInstance().post(new SocialActionFailedEvent(provider, uploadImageType, message, payload));

                            if (tempImageFile != null){
                                tempImageFile.delete();
                            }
                        }
                    }
            );
        } catch (IOException e) {
            SoomlaUtils.LogError(TAG, "(uploadImage) Failed generating temp image file: " + e.getMessage());
        }
    }

    /**
     * Upload image using a File handler
     *
     * @param provider    The provider to use
     * @param message     A text that will accompany the image
     * @param file        An image file handler
     * @param payload     a String to receive when the function returns.
     * @param reward      The reward to grant for sharing the photo
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void uploadImage(final IProvider.Provider provider,
                            String message, File file,
                            final String payload, final Reward reward) throws ProviderNotFoundException {
        if (file == null){
            SoomlaUtils.LogError(TAG, "(uploadImage) File is null!");
            return;
        }

        uploadImage(provider, message, file.getAbsolutePath(), payload, reward);
    }

    /**
     * Fetches the user's contact list
     *
     * @param provider The provider to use
     * @param payload  a String to receive when the function returns.
     * @param reward   The reward to grant
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void getContacts(final IProvider.Provider provider,
                            final String payload, final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        final ISocialProvider.SocialActionType getContactsType = ISocialProvider.SocialActionType.GET_CONTACTS;
        BusProvider.getInstance().post(new GetContactsStartedEvent(provider, getContactsType, payload));
        socialProvider.getContacts(new SocialCallbacks.ContactsListener() {
                                       @Override
                                       public void success(List<UserProfile> contacts) {
                                           BusProvider.getInstance().post(new GetContactsFinishedEvent(provider, getContactsType, contacts, payload));

                                           if (reward != null) {
                                               reward.give();
                                           }
                                       }

                                       @Override
                                       public void fail(String message) {
                                           BusProvider.getInstance().post(new GetContactsFailedEvent(provider, getContactsType, message, payload));
                                       }
                                   }
        );
    }

    /**
     * Fetches the user's feed.
     *
     * @param provider The provider to use
     * @param payload  a String to receive when the function returns.
     * @param reward   The reward to grant
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void getFeed(final IProvider.Provider provider,
                        final String payload, final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);

        final ISocialProvider.SocialActionType getFeedType = ISocialProvider.SocialActionType.GET_FEED;
        BusProvider.getInstance().post(new GetFeedStartedEvent(provider, getFeedType, payload));
        socialProvider.getFeed(new SocialCallbacks.FeedListener() {
                                   @Override
                                   public void success(List<String> feedPosts) {
                                       BusProvider.getInstance().post(new GetFeedFinishedEvent(provider, getFeedType, feedPosts, payload));

                                       if (reward != null) {
                                           reward.give();
                                       }
                                   }

                                   @Override
                                   public void fail(String message) {
                                       BusProvider.getInstance().post(new GetFeedFailedEvent(provider, getFeedType, message, payload));
                                   }
                               }
        );
    }

    /**
     * Opens up a provider page to "like" (external), and grants the user the supplied reward
     *
     * @param activity The parent activity
     * @param provider The provider to use
     * @param pageName The page to open up
     * @param reward   The reward to grant
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void like(final Activity activity, final IProvider.Provider provider,
                     String pageName,
                     final Reward reward) throws ProviderNotFoundException {
        final ISocialProvider socialProvider = getProvider(provider);
        socialProvider.like(activity, pageName);

        if (reward != null) {
            reward.give();
        }
    }

    /**
     * Creates a temporary image file
     * @param fileName The image file name to create
     * @param bitmap The image bitmap to create
     * @param jpegQuality The image quality
     * @return Image temp file path
     */
    private File createTempImageFile(String fileName, Bitmap bitmap
            , int jpegQuality) throws IOException {
        File tempDir = new File(getTempImageDir());
        tempDir.mkdirs();
        BufferedOutputStream bos = null;

        try{
            File file = new File(tempDir.toString() + fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bos = new BufferedOutputStream(fileOutputStream);

            String extension = fileName.substring((fileName.lastIndexOf(".") + 1), fileName.length());
            Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;

            if (extension == "jpeg"){
                format = Bitmap.CompressFormat.JPEG;
            }else if (extension == "png"){
                format = Bitmap.CompressFormat.PNG;
            } else{
                SoomlaUtils.LogDebug(TAG, "(createTempImageFile) file:" + fileName + " has an unknown extension: " + extension + ". using jpeg.");
            }

            bitmap.compress(format, jpegQuality, bos);
            bos.flush();
            return file;

        } catch (Exception e){
            return null;
        } finally {
            if (bos != null){
                bos.close();
            }
        }
    }

    private static String getTempImageDir(){
        ContextWrapper soomContextWrapper = new ContextWrapper(SoomlaApp.getAppContext());
        return Environment.getExternalStorageDirectory() + soomContextWrapper.getFilesDir().getPath() + "/temp/";
    }

    private static final String TAG = "SOOMLA SocialController";
}
