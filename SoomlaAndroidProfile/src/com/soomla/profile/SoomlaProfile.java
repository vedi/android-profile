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
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import android.net.Uri;
import android.os.Environment;
import android.view.View;
import com.soomla.BusProvider;
import com.soomla.SoomlaApp;
import com.soomla.SoomlaMarketUtils;
import com.soomla.SoomlaUtils;
import com.soomla.profile.domain.IProvider;
import com.soomla.profile.domain.UserProfile;
import com.soomla.profile.domain.gameservices.Leaderboard;
import com.soomla.profile.events.UserRatingEvent;
import com.soomla.profile.events.ProfileInitializedEvent;
import com.soomla.profile.exceptions.ProviderNotFoundException;
import com.soomla.rewards.Reward;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

/**
 * This is the main class for the SOOMLA User Profile module.  This class
 * should be initialized once, after <code>Soomla.initialize()</code> is invoked.
 * Use this class to perform authentication and social actions on behalf of
 * the user that will grant him \ her rewards in your game.
 */
@SuppressWarnings("UnusedDeclaration")
public class SoomlaProfile {

    public static final String VERSION = "1.1.7";

    /**
     * see {@link #initialize(Activity, boolean, Map)}
     */
    public boolean initialize() {
        return initialize(null, null);
    }

    /**
     * see {@link #initialize(Activity, boolean, Map)}
     */
    public boolean initialize(Activity activity) {
        return initialize(activity, null);
    }

    /**
     * see {@link #initialize(Activity, boolean, Map)}
     */
    public boolean initialize(Map<IProvider.Provider, ? extends Map<String, String>> providerParams) {
        return initialize(null, false, providerParams);
    }

    /**
     * see {@link #initialize(Activity, boolean, Map)}
     */
    public boolean initialize(Activity activity, Map<IProvider.Provider, ? extends Map<String, String>> providerParams) {
        return initialize(activity, false, providerParams);
    }

    public boolean initialize(boolean usingExternalProvider, Map<IProvider.Provider, ? extends Map<String, String>> providerParams) {
        return initialize(null, false, providerParams);
    }

    /**
     * Initializes the Profile module.  Call this method after <code>Soomla.initialize()</code>
     * @param activity The parent activity
     * @param usingExternalProvider If using an external SDK (like Unity FB SDK) pass true
     *                              here so we know not to complain about native providers
     *                              not found
     * @param providerParams provides custom values for specific social providers
     */
    public boolean initialize(Activity activity, boolean usingExternalProvider, Map<IProvider.Provider, ? extends Map<String, String>> providerParams) {
        if (mInitialized) {
            String err = "SoomlaStore is already initialized. You can't initialize it twice!";
            SoomlaUtils.LogError(TAG, err);
            return false;
        }

        mAuthController = new AuthController(usingExternalProvider, providerParams);
        mSocialController = new SocialController(usingExternalProvider, providerParams);

        mInitialized = true;

        BusProvider.getInstance().post(new ProfileInitializedEvent());

        if (activity == null) {
            SoomlaUtils.LogWarning(TAG,
                    "You want to use `autoLogin`, but have not provided the `activity`. Skipping auto login");
            return false;
        }
        mAuthController.settleAutoLogin(activity);
        mSocialController.settleAutoLogin(activity);

        return true;
    }

    /**
     * Login to the given provider
     *
     * @param activity The parent activity
     * @param provider The provider to use
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void login(Activity activity, final IProvider.Provider provider) throws ProviderNotFoundException {
        login(activity, provider, "", null);
    }

    /**
     * Login to the given provider and grant the user a reward.
     *
     * @param activity The parent activity
     * @param provider The provider to use
     * @param reward   The reward to give the user for logging in.
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void login(Activity activity, final IProvider.Provider provider, final Reward reward) throws ProviderNotFoundException {
        login(activity, provider, "", reward);
    }

    /**
     * Login to the given provider and grant the user a reward.
     *
     * @param activity The parent activity
     * @param provider The provider to use
     * @param payload  a String to receive when the function returns.
     * @param reward   The reward to give the user for logging in.
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void login(Activity activity, final IProvider.Provider provider,
                      String payload, final Reward reward) throws ProviderNotFoundException {
        try {
            mAuthController.login(activity, provider, false, payload, reward);
        } catch (ProviderNotFoundException e) {
            mSocialController.login(activity, provider, false, payload, reward);
        }
    }

    /**
     * Checks if the user is logged-in to the given provider
     *
     * @param activity The parent activity
     * @param provider The provider to use
     * @return true if the user is logged-in with the authentication provider,
     * false otherwise
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public boolean isLoggedIn(Activity activity, final IProvider.Provider provider) throws ProviderNotFoundException {
        try {
            return mAuthController.isLoggedIn(activity, provider);
        } catch (ProviderNotFoundException e) {
            return mSocialController.isLoggedIn(activity, provider);
        }
    }

    /**
     * Logout of the given provider
     *
     * @param provider The provider to use
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void logout(final IProvider.Provider provider) throws ProviderNotFoundException {
        try {
            mAuthController.logout(provider);
        } catch (ProviderNotFoundException e) {
            mSocialController.logout(provider);
        }
    }

    /**
     * Fetches the user's profile for the given provider from the local device storage
     *
     * @param provider The provider to use
     * @return The user profile
     */
    public UserProfile getStoredUserProfile(IProvider.Provider provider) {
        UserProfile userProfile = mAuthController.getStoredUserProfile(provider);
        if (userProfile != null)
            return userProfile;

        return mSocialController.getStoredUserProfile(provider);
    }

    /**
     * Shares the given status to the user's feed and grants the user a reward.
     *
     * @param provider The provider to use
     * @param status   The text to share
     * @param reward   The reward to give the user
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void updateStatus(IProvider.Provider provider, String status, final Reward reward) throws ProviderNotFoundException {
        updateStatus(provider, status, "", reward);
    }

    /**
     * Shares the given status to the user's feed and grants the user a reward.
     *
     * @param provider The provider to use
     * @param status   The text to share
     * @param payload  a String to receive when the function returns.
     * @param reward   The reward to give the user
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void updateStatus(IProvider.Provider provider, String status, String payload, final Reward reward) throws ProviderNotFoundException {
        mSocialController.updateStatus(provider, status, payload, reward, null, null);
    }

    /**
     * Overloaded version of {@link #updateStatusWithConfirmation(com.soomla.profile.domain.IProvider.Provider, String, String, com.soomla.rewards.Reward, android.app.Activity, String)} without "customMessage"
     */
    public void updateStatusWithConfirmation(IProvider.Provider provider, String status, String payload, final Reward reward, final Activity activity) throws ProviderNotFoundException {
        mSocialController.updateStatus(provider, status, payload, reward, activity, null);
    }

    /**
     * Shares the given status to the user's feed with confirmation dialog and grants the user a reward.
     *
     * @param provider The provider to use
     * @param status   The text to share
     * @param payload  a String to receive when the function returns.
     * @param reward   The reward to give the user
     * @param activity activity to use as context for the dialog
     * @param customMessage the message to show in the dialog
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void updateStatusWithConfirmation(IProvider.Provider provider, String status, String payload, final Reward reward, final Activity activity, final String customMessage) throws ProviderNotFoundException {
        mSocialController.updateStatus(provider, status, payload, reward, activity, customMessage);
    }

    /**
     * Shares the given status to the user's feed and grants the user a reward.
     * Using the provider's native dialog (when available).
     *
     * @param provider The provider to us
     * @param link     The link to share (could be null)
     * @param reward   The reward to give the user
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void updateStatusDialog(IProvider.Provider provider, String link, final Reward reward) throws ProviderNotFoundException {
        updateStatusDialog(provider, link, "", reward);
    }

    /**
     * Shares the given status to the user's feed and grants the user a reward.
     * Using the provider's native dialog (when available).
     *
     * @param provider The provider to us
     * @param link     The link to share (could be null)
     * @param payload  a String to receive when the function returns.
     * @param reward   The reward to give the user
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void updateStatusDialog(IProvider.Provider provider, String link, String payload, final Reward reward) throws ProviderNotFoundException {
        mSocialController.updateStatusDialog(provider, link, payload, reward);
    }

    /**
     * Shares a story to the user's feed and grants the user a reward.
     *
     * @param provider    The provider to use
     * @param message     The main text which will appear in the story
     * @param name        The headline for the link which will be integrated in the
     *                    story
     * @param caption     The sub-headline for the link which will be
     *                    integrated in the story
     * @param description description The description for the link which will be
     *                    integrated in the story
     * @param link        The link which will be integrated into the user's story
     * @param picture     a Link to a picture which will be featured in the link
     * @param reward      The reward which will be granted to the user upon a
     *                    successful update
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void updateStory(IProvider.Provider provider, String message, String name, String caption,
                            String description, String link, String picture,
                            final Reward reward) throws ProviderNotFoundException {

        updateStory(provider, message, name, caption, description, link, picture, "", reward);
    }

    /**
     * Shares a story to the user's feed and grants the user a reward.
     *
     * @param provider    The provider to use
     * @param message     The main text which will appear in the story
     * @param name        The headline for the link which will be integrated in the
     *                    story
     * @param caption     The sub-headline for the link which will be
     *                    integrated in the story
     * @param description description The description for the link which will be
     *                    integrated in the story
     * @param link        The link which will be integrated into the user's story
     * @param picture     a Link to a picture which will be featured in the link
     * @param payload     a String to receive when the function returns.
     * @param reward      The reward which will be granted to the user upon a
     *                    successful update
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void updateStory(IProvider.Provider provider, String message, String name, String caption,
                            String description, String link, String picture, String payload,
                            final Reward reward) throws ProviderNotFoundException {

        mSocialController.updateStory(provider, message, name, caption, description, link, picture, payload, reward, null, null);
    }

    /**
     * Overloaded version of {@link #updateStoryWithConfirmation(com.soomla.profile.domain.IProvider.Provider, String, String, String, String, String, String, String, com.soomla.rewards.Reward, android.app.Activity, String)} without "customMessage"
     */
    public void updateStoryWithConfirmation(IProvider.Provider provider, String message, String name, String caption,
                            String description, String link, String picture, String payload,
                            final Reward reward, final Activity activity) throws ProviderNotFoundException {

        mSocialController.updateStory(provider, message, name, caption, description, link, picture, payload, reward, activity, null);
    }

    /**
     * Shares a story to the user's feed with confirmation dialog and grants the user a reward.
     *
     * @param provider    The provider to use
     * @param message     The main text which will appear in the story
     * @param name        The headline for the link which will be integrated in the
     *                    story
     * @param caption     The sub-headline for the link which will be
     *                    integrated in the story
     * @param description description The description for the link which will be
     *                    integrated in the story
     * @param link        The link which will be integrated into the user's story
     * @param picture     a Link to a picture which will be featured in the link
     * @param payload     a String to receive when the function returns.
     * @param reward      The reward which will be granted to the user upon a
     *                    successful update
     * @param activity activity to use as context for the dialog
     * @param customMessage the message to show in the dialog
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void updateStoryWithConfirmation(IProvider.Provider provider, String message, String name, String caption,
                            String description, String link, String picture, String payload,
                            final Reward reward, final Activity activity, final String customMessage) throws ProviderNotFoundException {

        mSocialController.updateStory(provider, message, name, caption, description, link, picture, payload, reward, activity, customMessage);
    }

    /**
     * Shares a story to the user's feed and grants the user a reward.
     * Using the provider's native dialog (when available).
     *
     * @param provider    The provider to use
     * @param name        The headline for the link which will be integrated in the
     *                    story
     * @param caption     The sub-headline for the link which will be
     *                    integrated in the story
     * @param description description The description for the link which will be
     *                    integrated in the story
     * @param link        The link which will be integrated into the user's story
     * @param picture     a Link to a picture which will be featured in the link
     * @param reward      The reward which will be granted to the user upon a
     *                    successful update
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void updateStoryDialog(IProvider.Provider provider, String name, String caption,
                                  String description, String link, String picture,
                                  final Reward reward) throws ProviderNotFoundException {

        updateStoryDialog(provider, name, caption, description, link, picture, "", reward);
    }

    /**
     * Shares a story to the user's feed and grants the user a reward.
     * Using the provider's native dialog (when available).
     *
     * @param provider    The provider to use
     * @param name        The headline for the link which will be integrated in the
     *                    story
     * @param caption     The sub-headline for the link which will be
     *                    integrated in the story
     * @param description description The description for the link which will be
     *                    integrated in the story
     * @param link        The link which will be integrated into the user's story
     * @param picture     a Link to a picture which will be featured in the link
     * @param payload     a String to receive when the function returns.
     * @param reward      The reward which will be granted to the user upon a
     *                    successful update
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void updateStoryDialog(IProvider.Provider provider, String name, String caption,
                                  String description, String link, String picture, String payload,
                                  final Reward reward) throws ProviderNotFoundException {
        mSocialController.updateStoryDialog(provider, name, caption, description, link, picture, payload, reward);
    }

    /**
     * Shares a photo to the user's feed and grants the user a reward.
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
    public void uploadImage(IProvider.Provider provider,
                            String message, String fileName, Bitmap bitmap, int jpegQuality,
                            String payload, final Reward reward) throws ProviderNotFoundException {

        mSocialController.uploadImage(provider, message, fileName, bitmap, jpegQuality, payload, reward, null, null);
    }

    /**
     * Shares a photo to the user's feed with confirmation dialog and grants the user a reward.
     *
     * @param provider    The provider to use
     * @param message     A text that will accompany the image
     * @param fileName    The desired image's file name
     * @param bitmap      The image to share
     * @param jpegQuality The image's numeric quality
     * @param payload     a String to receive when the function returns.
     * @param reward      The reward to grant for sharing the photo
     * @param activity If defined, confirmation confirmation dialog will be shown before the action
     * @param customMessage the message to show in the dialog
     * @throws ProviderNotFoundException if the supplied provider is not supported by the framework
     */
    public void uploadImageWithConfirmation(IProvider.Provider provider,
                                            String message, String fileName, Bitmap bitmap, int jpegQuality,
                                            String payload, final Reward reward, Activity activity, String customMessage) throws ProviderNotFoundException {

        mSocialController.uploadImage(provider, message, fileName, bitmap, jpegQuality, payload, reward, activity, customMessage);
    }

    /**
     * Overloaded version of {@link #uploadImageWithConfirmation(com.soomla.profile.domain.IProvider.Provider, String, String, android.graphics.Bitmap, int, String, com.soomla.rewards.Reward, android.app.Activity, String)} without "customMessage"
     */
    public void uploadImageWithConfirmation(IProvider.Provider provider,
                            String message, String fileName, Bitmap bitmap, int jpegQuality,
                            String payload, final Reward reward) throws ProviderNotFoundException {
        mSocialController.uploadImage(provider, message, fileName, bitmap, jpegQuality, payload, reward, null, null);
    }

    /**
     * Shares a photo to the user's feed and grants the user a reward.
     *
     * @param provider    The provider to use
     * @param message     A text that will accompany the image
     * @param file        An image file handler
     * @param payload     a String to receive when the function returns.
     * @param reward      The reward to grant for sharing the photo
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void uploadImage(IProvider.Provider provider, String message, File file,  String payload, final Reward reward) throws ProviderNotFoundException{
        mSocialController.uploadImage(provider, message, file, payload, reward);
    }

    /**
     * Shares a photo to the user's feed and grants the user a reward.
     *
     * @param provider The provider to use
     * @param message  A text that will accompany the image
     * @param filePath The desired image's location on the device
     * @param reward   The reward to give the user
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void uploadImage(IProvider.Provider provider,
                            String message, String filePath,
                            final Reward reward) throws ProviderNotFoundException {
        uploadImage(provider, message, filePath, "", reward);
    }


    /**
     * Shares a photo to the user's feed and grants the user a reward.
     *
     * @param provider The provider to use
     * @param message  A text that will accompany the image
     * @param filePath The desired image's location on the device
     * @param payload  a String to receive when the function returns.
     * @param reward   The reward to give the user
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void uploadImage(IProvider.Provider provider,
                            String message, String filePath, String payload,
                            final Reward reward) throws ProviderNotFoundException {
        mSocialController.uploadImage(provider, message, filePath, payload, reward, null, null);
    }

    /**
     * Overloaded version of {@link #uploadImageWithConfirmation(com.soomla.profile.domain.IProvider.Provider, String, String, String, com.soomla.rewards.Reward, android.app.Activity, String)}} without "customMessage"
     */
    public void uploadImageWithConfirmation(IProvider.Provider provider,
                            String message, String filePath, String payload,
                            final Reward reward, Activity activity) throws ProviderNotFoundException {
        mSocialController.uploadImage(provider, message, filePath, payload, reward, activity, null);
    }

    /**
     * Shares a photo to the user's feed with confirmation dialog and grants the user a reward.
     *
     * @param provider The provider to use
     * @param message  A text that will accompany the image
     * @param filePath The desired image's location on the device
     * @param payload  a String to receive when the function returns.
     * @param reward   The reward to give the user
     * @param activity activity to use as context for the dialog
     * @param customMessage the message to show in the dialog
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void uploadImageWithConfirmation(IProvider.Provider provider,
                            String message, String filePath, String payload,
                            final Reward reward, Activity activity, String customMessage) throws ProviderNotFoundException {
        mSocialController.uploadImage(provider, message, filePath, payload, reward, activity, customMessage);
    }

    /**
     * Shares a current screenshot to the user's feed and grants the user a reward.
     * @param activity activity to use as context for the screenshot
     * @param provider The provider to use
     * @param message  A text that will accompany the image
     * @param title  A title of post
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void uploadCurrentScreenshot(final Activity activity, IProvider.Provider provider, String title, String message) throws ProviderNotFoundException {
        uploadCurrentScreenshot(activity, provider, title, message, "", null);
    }

    /**
     * Shares a current screenshot to the user's feed and grants the user a reward.
     * @param activity activity to use as context for the screenshot
     * @param provider The provider to use
     * @param message  A text that will accompany the image
     * @param title  A title of post
     * @param payload  a String to receive when the function returns.
     * @param reward   The reward to give the user
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void uploadCurrentScreenshot(final Activity activity, IProvider.Provider provider, String title, String message, String payload, Reward reward)
            throws ProviderNotFoundException {
        mSocialController.uploadImage(provider, message, takeScreenshot(activity), payload, reward);
    }

    /**
     * Takes curent app screenshot
     *
     * @param activity An activity used as context
     * @return A file points to current screenshot
     */
    private File takeScreenshot(final Activity activity) {
        try {
            String mPath = Environment.getExternalStorageDirectory().toString() + "/current_screenshot.jpg";

            // create bitmap screen capture
            View v1 = activity.getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            return imageFile;
        } catch (Throwable e) {

            // Several error may come out with file handling or OOM
            e.printStackTrace();
            return null;
        }
    }

    /**

    /**
     * Fetches the user's contact list and grants the user a reward.
     *
     * @param provider The provider to use
     * @param reward   The reward to grant
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void getContacts(IProvider.Provider provider, final Reward reward) throws ProviderNotFoundException {
        getContacts(provider, false, "", reward);
    }

    /**
     * Fetches the user's contact list and grants the user a reward.
     *
     * @param provider The provider to use
     * @param fromStart Should we reset pagination or request the next page
     * @param reward   The reward to grant
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void getContacts(IProvider.Provider provider, boolean fromStart, final Reward reward) throws ProviderNotFoundException {
        getContacts(provider, fromStart, "", reward);
    }

    /**
     * Fetches the user's contact list and grants the user a reward.
     *
     * @param provider The provider to use
     * @param fromStart Should we reset pagination or request the next page
     * @param payload  a String to receive when the function returns.
     * @param reward   The reward to grant
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void getContacts(IProvider.Provider provider, boolean fromStart, String payload, final Reward reward) throws ProviderNotFoundException {
        mSocialController.getContacts(provider, fromStart, payload, reward);
    }

    /**
     * Fetches the user's feed and grants the user a reward.
     *
     * @param provider The provider to use
     * @param reward   The reward to grant
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void getFeed(IProvider.Provider provider, final Reward reward) throws ProviderNotFoundException {
        getFeed(provider, false, "", reward);
    }

    /**
     * Fetches the user's feed and grants the user a reward.
     *
     * @param provider The provider to use
     * @param fromStart Should we reset pagination or request the next page
     * @param payload  a String to receive when the function returns.
     * @param reward   The reward to grant
     * @throws ProviderNotFoundException if the supplied provider is not supported by the framework
     */
    public void getFeed(IProvider.Provider provider, Boolean fromStart, String payload, final Reward reward) throws ProviderNotFoundException {
        mSocialController.getFeed(provider, fromStart, payload, reward);
    }

    /**
     * Sends an invite.
     *
     * @param activity The parent activity
     * @param provider The provider to use
     * @param inviteMessage a message will send to recipients.
     * @throws ProviderNotFoundException if the supplied provider is not supported by the framework
     */
    public void invite(final Activity activity, IProvider.Provider provider, String inviteMessage) {
        invite(activity, provider, inviteMessage, null, "", null);
    }

    /**
     * Sends an invite.
     *
     * @param activity The parent activity
     * @param provider The provider to use
     * @param inviteMessage a message will send to recipients.
     * @param dialogTitle a title of app request dialog.
     * @param payload  a String to receive when the function returns.
     * @param reward   The reward to grant
     * @throws ProviderNotFoundException if the supplied provider is not supported by the framework
     */
    public void invite(final Activity activity, IProvider.Provider provider, String inviteMessage, String dialogTitle, String payload, final Reward reward) {
        mSocialController.invite(activity, provider, inviteMessage, dialogTitle, payload, reward);
    }

    /**
     * Opens up a provider page to "like" (external), and grants the user the supplied reward
     *
     * @param activity The parent activity
     * @param provider The provider to use
     * @param pageId The page to open up
     * @param reward   The reward to grant
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void like(final Activity activity, final IProvider.Provider provider,
                     String pageId,
                     final Reward reward) throws ProviderNotFoundException {
        mSocialController.like(activity, provider, pageId, reward);
    }

    /**
     * Utility method to open up the market application rating page
     *
     * @param context The main context of the app
     */
    public void openAppRatingPage(Context context) {
        SoomlaMarketUtils.openMarketAppPage(context);

        BusProvider.getInstance().post(new UserRatingEvent());
    }

    /**
     * Shares text and/or image using native sharing functionality of your target platform.
     * @param text Text to share
     * @param imageFilePath Path to an image file to share
     */
    public void multiShare(String text, String imageFilePath) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        if (imageFilePath != null) {
            sharingIntent.setType("*/*");
            Uri uri = Uri.parse("file://" + imageFilePath);
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
        } else {
            sharingIntent.setType("text/plain");
        }
        sharingIntent.putExtra(Intent.EXTRA_TEXT, text);
        Intent chooser = Intent.createChooser(sharingIntent, "Share");
        chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        SoomlaApp.getAppContext().startActivity(chooser);
    }

    /**
     Fetches the game's leaderboards list

     * @param provider The provider to use
     * @param reward   The reward to grant
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void getLeaderboards(final IProvider.Provider provider, final Reward reward) throws ProviderNotFoundException {
        getLeaderboards(provider, "", reward);
    }

    /**
     Fetches the game's leaderboards list

     * @param provider The provider to use
     * @param payload  a String to receive when the function returns.
     * @param reward   The reward to grant
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void getLeaderboards(final IProvider.Provider provider, final String payload, final Reward reward) throws ProviderNotFoundException {
        mGameServicesController.getLeaderboards(provider, payload, reward);
    }

    /**
     Fetches the game's scores list from specified leaderboard

     * @param provider The provider to use
     * @param leaderboard Leaderboard containing desired scores list
     * @param reward   The reward to grant
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void getScores(final IProvider.Provider provider, final Leaderboard leaderboard, final Reward reward) throws ProviderNotFoundException {
        getScores(provider, leaderboard, false, "", reward);
    }

    /**
     Fetches the game's scores list from specified leaderboard

     * @param provider The provider to use
     * @param leaderboard Leaderboard containing desired scores list
     * @param fromStart Should we reset pagination or request the next page
     * @param reward   The reward to grant
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void getScores(final IProvider.Provider provider, final Leaderboard leaderboard, final boolean fromStart, final Reward reward) throws ProviderNotFoundException {
        getScores(provider, leaderboard, fromStart, "", reward);
    }

    /**
     Fetches the game's scores list from specified leaderboard

     * @param provider The provider to use
     * @param leaderboard Leaderboard containing desired scores list
     * @param fromStart Should we reset pagination or request the next page
     * @param payload  a String to receive when the function returns.
     * @param reward   The reward to grant
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void getScores(final IProvider.Provider provider, final Leaderboard leaderboard, final boolean fromStart, final String payload, final Reward reward) throws ProviderNotFoundException {
        mGameServicesController.getScores(provider, leaderboard, fromStart, payload, reward);
    }

    /**
     Submits scores to specified leaderboard

     * @param provider The provider to use
     * @param leaderboard Leaderboard containing desired scores list
     * @param value Value to report
     * @param payload  a String to receive when the function returns.
     * @param reward   The reward to grant
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void submitScore(final IProvider.Provider provider, final Leaderboard leaderboard, final long value, final String payload, final Reward reward) throws ProviderNotFoundException {
        mGameServicesController.submitScore(provider, leaderboard, value, payload, reward);
    }

    /**
     * Private Members *
     */

    private AuthController mAuthController;
    private SocialController mSocialController;
    private GameServicesController mGameServicesController;

    /**
     * Singleton *
     */

    private SoomlaProfile() {
    }

    private static final SoomlaProfile mInstance = new SoomlaProfile();

    public static SoomlaProfile getInstance() {
        return mInstance;
    }

    private static final String TAG = "SOOMLA SoomlaProfile";

    private boolean mInitialized = false;
}
