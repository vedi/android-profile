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

package com.soomla.profile.social.twitter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.net.Uri;
import android.text.TextUtils;

import com.soomla.Soomla;
import com.soomla.SoomlaApp;
import com.soomla.SoomlaUtils;
import com.soomla.data.KeyValueStorage;
import com.soomla.profile.auth.AuthCallbacks;
import com.soomla.profile.domain.UserProfile;
import com.soomla.profile.social.ISocialProvider;
import com.soomla.profile.social.SocialCallbacks;

import twitter4j.*;
import twitter4j.api.HelpResources;
import twitter4j.auth.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Soomla wrapper for Twitter4j (unofficial SDK for Twitter API).
 * <p/>
 * This class works by creating a transparent activity (SoomlaTwitterActivity) and working through it.
 * This is required to correctly integrate with FB activity lifecycle events
 */
public class SoomlaTwitter implements ISocialProvider {

    private static final String TAG = "SOOMLA SoomlaTwitter";

    private static final String DB_KEY_PREFIX = "soomla.profile.twitter.";
    private static final String TWITTER_OAUTH_TOKEN = "oauth.token";
    private static final String TWITTER_OAUTH_SECRET = "oauth.secret";

    private static final String OAUTH_VERIFIER = "oauth_verifier";

    // some weak refs that are set before launching the wrapper SoomlaTwitterActivity
    // (need to be accessed by static context)
    private static WeakReference<Activity> WeakRefParentActivity;
    private static Provider RefProvider;
    private static AuthCallbacks.LoginListener RefLoginListener;
    private static SocialCallbacks.SocialActionListener RefSocialActionListener;
    private static SocialCallbacks.FeedListener RefFeedListener;
    private static SocialCallbacks.ContactsListener RefContactsListener;

    private String twitterConsumerKey;
    private String twitterConsumerSecret;

    private static RequestToken mainRequestToken;
    private static String oauthCallbackURL;

    public static final int ACTION_LOGIN = 0;

    public static final int ACTION_PUBLISH_STATUS = 10;
    public static final int ACTION_PUBLISH_STORY = 11;
    public static final int ACTION_UPLOAD_IMAGE = 12;
    public static final int ACTION_GET_FEED = 13;
    public static final int ACTION_GET_CONTACTS = 14;
    public static final int ACTION_PUBLISH_STATUS_DIALOG = 15;
    public static final int ACTION_PUBLISH_STORY_DIALOG = 16;

    /**
     * Constructor
     */
    public SoomlaTwitter() {
        twitterConsumerKey = "<twitterConsumerKey>";
        twitterConsumerSecret = "<twitterConsumerSecret>";
        try {
            final Context appContext = SoomlaApp.getAppContext();
            ApplicationInfo ai = appContext.getPackageManager().
                    getApplicationInfo(appContext.getPackageName(),
                            PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            twitterConsumerKey = bundle.getString("com.soomla.twitter.ConsumerKey");
            twitterConsumerSecret = bundle.getString("com.soomla.twitter.ConsumerSecret");
            SoomlaUtils.LogDebug(TAG, String.format(
                    "com.soomla.twitter.ConsumerKey:%s com.soomla.twitter.ConsumerSecret:%s",
                    twitterConsumerKey, twitterConsumerSecret));
        } catch (PackageManager.NameNotFoundException e) {
            SoomlaUtils.LogError(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            SoomlaUtils.LogError(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
        }

        if (TextUtils.isEmpty(twitterConsumerKey) || TextUtils.isEmpty(twitterConsumerSecret)) {
            SoomlaUtils.LogError(TAG, "You must provide the Consumer Key and Secret in the AndroidManifest.xml");
        }

        oauthCallbackURL = "oauth://soomla_twitter" + twitterConsumerKey;
        AsyncTwitterFactory.getSingleton().setOAuthConsumer(twitterConsumerKey, twitterConsumerSecret);
    }

    /**
     * The main SOOMLA Twitter activity
     * <p/>
     * This activity allows the framework to popup a window which in turns
     * communicates with Twitter
     */
    public static class SoomlaTwitterActivity extends Activity {

        private static final String TAG = "SOOMLA SoomlaFacebook$SoomlaTwitterActivity";
        private int preformingAction;
        private TwitterAdapter oauthListener = new TwitterAdapter() {

            @Override
            public void gotOAuthRequestToken(RequestToken requestToken) {
                mainRequestToken = requestToken;
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mainRequestToken.getAuthenticationURL())));
            }

            @Override
            public void gotOAuthAccessToken(AccessToken accessToken) {
                AsyncTwitterFactory.getSingleton().setOAuthAccessToken(accessToken);

                // Keep in storage for later use
                KeyValueStorage.setValue(getTwitterStorageKey(TWITTER_OAUTH_TOKEN), accessToken.getToken());
                KeyValueStorage.setValue(getTwitterStorageKey(TWITTER_OAUTH_SECRET), accessToken.getTokenSecret());

                RefLoginListener.success(RefProvider);

                clearListeners();
                finish();
            }

            @Override
            public void onException(TwitterException e, TwitterMethod twitterMethod) {
                SoomlaUtils.LogDebug(TAG, "login failed while requesting tokens " + e.getMessage());
                RefLoginListener.fail("login failed: " + e.getMessage());
                clearListeners();
                finish();
            }
        };

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            SoomlaUtils.LogDebug(TAG, "onCreate");

            // perform our wrapped action

            Intent intent = getIntent();
            preformingAction = intent.getIntExtra("action", -1);

            if (preformingAction == -1) {
                /**
                 * Handle OAuth Callback
                 */
                Uri uri = getIntent().getData();
                if (uri != null && uri.toString().startsWith(oauthCallbackURL)) {
                    String verifier = uri.getQueryParameter(OAUTH_VERIFIER);
                    AsyncTwitterFactory.getSingleton().getOAuthAccessTokenAsync(mainRequestToken, verifier);
                }
            }
            else {
                switch (preformingAction) {
                    case ACTION_LOGIN: {
                        login(this, RefLoginListener);
                        break;
                    }
                    case ACTION_PUBLISH_STATUS: {
                        String status = intent.getStringExtra("status");
                        updateStatus(status, RefSocialActionListener);
                        break;
                    }
                    case ACTION_PUBLISH_STATUS_DIALOG: {
                        String link = intent.getStringExtra("link");
                        updateStatusDialog(link, RefSocialActionListener);
                        break;
                    }
                    case ACTION_PUBLISH_STORY: {
                        String message = intent.getStringExtra("message");
                        String name = intent.getStringExtra("name");
                        String caption = intent.getStringExtra("caption");
                        String description = intent.getStringExtra("description");
                        String link = intent.getStringExtra("link");
                        String picture = intent.getStringExtra("picture");
                        updateStory(message, name, caption, description, link, picture, RefSocialActionListener);
                        break;
                    }
                    case ACTION_PUBLISH_STORY_DIALOG: {
                        String name = intent.getStringExtra("name");
                        String caption = intent.getStringExtra("caption");
                        String description = intent.getStringExtra("description");
                        String link = intent.getStringExtra("link");
                        String picture = intent.getStringExtra("picture");
                        updateStoryDialog(name, caption, description, link, picture, RefSocialActionListener);
                        break;
                    }
                    case ACTION_UPLOAD_IMAGE: {
                        String message = intent.getStringExtra("message");
                        String filePath = intent.getStringExtra("filePath");
                        uploadImage(message, filePath, RefSocialActionListener);
                        break;
                    }
                    case ACTION_GET_FEED: {
                        getFeed(RefFeedListener);
                        break;
                    }
                    case ACTION_GET_CONTACTS: {
                        getContacts(RefContactsListener);
                        break;
                    }
                    default: {
                        SoomlaUtils.LogWarning(TAG, "action unknown:" + preformingAction);
                        break;
                    }
                }
            }
        }

        private void clearListeners() {
            SoomlaUtils.LogDebug(TAG, "Clearing Listeners");

            switch (preformingAction) {
                case ACTION_LOGIN: {
                    RefLoginListener = null;
                    break;
                }
                case ACTION_PUBLISH_STATUS: {
                    RefSocialActionListener = null;
                    break;
                }
                case ACTION_PUBLISH_STATUS_DIALOG: {
                    RefSocialActionListener = null;
                    break;
                }
                case ACTION_PUBLISH_STORY: {
                    RefSocialActionListener = null;
                    break;
                }
                case ACTION_PUBLISH_STORY_DIALOG: {
                    RefSocialActionListener = null;
                    break;
                }
                case ACTION_UPLOAD_IMAGE: {
                    RefSocialActionListener = null;
                    break;
                }
                case ACTION_GET_FEED: {
                    RefFeedListener = null;
                    break;
                }
                case ACTION_GET_CONTACTS: {
                    RefContactsListener = null;
                    break;
                }
                default: {
                    SoomlaUtils.LogWarning(TAG, "action unknown:" + preformingAction);
                    break;
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onResume() {
            super.onResume();
            SoomlaUtils.LogDebug(TAG, "onResume");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            SoomlaUtils.LogDebug(TAG, "onActivityResult");
        }

        private void login(Activity activity, final AuthCallbacks.LoginListener loginListener) {
            SoomlaUtils.LogDebug(TAG, "login");

            String oauthToken = KeyValueStorage.getValue(getTwitterStorageKey(TWITTER_OAUTH_TOKEN));
            String oauthTokenSecret = KeyValueStorage.getValue(getTwitterStorageKey(TWITTER_OAUTH_SECRET));
            if (!TextUtils.isEmpty(oauthToken) && !TextUtils.isEmpty(oauthTokenSecret)) {
                mainRequestToken = null;
                AsyncTwitterFactory.getSingleton().setOAuthAccessToken(new AccessToken(oauthToken, oauthTokenSecret));

                RefLoginListener.success(RefProvider);

                clearListeners();
                finish();
            }
            else {
                AsyncTwitterFactory.getSingleton().addListener(oauthListener);
                AsyncTwitterFactory.getSingleton().getOAuthRequestTokenAsync(oauthCallbackURL);
            }
        }

        private void updateStatusDialog(String link, final SocialCallbacks.SocialActionListener socialActionListener) {
            SoomlaUtils.LogDebug(TAG, "updateStatus");
        }

        private void updateStatus(String status, final SocialCallbacks.SocialActionListener socialActionListener) {
            SoomlaUtils.LogDebug(TAG, "updateStatus");
        }

        private void updateStory(String message, String name, String caption, String description, String link, String picture,
                                 final SocialCallbacks.SocialActionListener socialActionListener) {
            SoomlaUtils.LogDebug(TAG, "updateStory");
        }

        private void updateStoryDialog(String name, String caption, String description, String link, String picture,
                                       final SocialCallbacks.SocialActionListener socialActionListener) {
            SoomlaUtils.LogDebug(TAG, "updateStoryDialog");
        }

        private void uploadImage(String message, String filePath, final SocialCallbacks.SocialActionListener socialActionListener) {
            SoomlaUtils.LogDebug(TAG, "uploadImage");
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        }

        private void getContacts(final SocialCallbacks.ContactsListener contactsListener) {
            SoomlaUtils.LogDebug(TAG, "getContacts");
        }

        public void getFeed(final SocialCallbacks.FeedListener feedListener) {
            SoomlaUtils.LogDebug(TAG, "getFeed");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void login(final Activity parentActivity, final AuthCallbacks.LoginListener loginListener) {
        SoomlaUtils.LogDebug(TAG, "login");
        WeakRefParentActivity = new WeakReference<Activity>(parentActivity);

        RefProvider = getProvider();
        RefLoginListener = loginListener;
        Intent intent = new Intent(parentActivity, SoomlaTwitterActivity.class);

        intent.putExtra("action", ACTION_LOGIN);
        parentActivity.startActivity(intent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logout(final AuthCallbacks.LogoutListener logoutListener) {
        SoomlaUtils.LogDebug(TAG, "logout");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLoggedIn(final Activity activity) {
        SoomlaUtils.LogDebug(TAG, "isLoggedIn");

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getUserProfile(final AuthCallbacks.UserProfileListener userProfileListener) {
        SoomlaUtils.LogDebug(TAG, "getUserProfile");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateStatus(String status, final SocialCallbacks.SocialActionListener socialActionListener) {
        SoomlaUtils.LogDebug(TAG, "updateStatus");

        RefProvider = getProvider();
        RefSocialActionListener = socialActionListener;
        Intent intent = new Intent(WeakRefParentActivity.get(), SoomlaTwitterActivity.class);
        intent.putExtra("action", ACTION_PUBLISH_STATUS);
        intent.putExtra("status", status);
        WeakRefParentActivity.get().startActivity(intent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateStatusDialog(String link, SocialCallbacks.SocialActionListener socialActionListener) {
        SoomlaUtils.LogDebug(TAG, "updateStatus");

        RefProvider = getProvider();
        RefSocialActionListener = socialActionListener;
        Intent intent = new Intent(WeakRefParentActivity.get(), SoomlaTwitterActivity.class);
        intent.putExtra("action", ACTION_PUBLISH_STATUS_DIALOG);
        intent.putExtra("link", link);
        WeakRefParentActivity.get().startActivity(intent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateStory(String message, String name, String caption, String description, String link, String picture,
                            final SocialCallbacks.SocialActionListener socialActionListener) {
        RefProvider = getProvider();
        RefSocialActionListener = socialActionListener;
        Intent intent = new Intent(WeakRefParentActivity.get(), SoomlaTwitterActivity.class);
        intent.putExtra("action", ACTION_PUBLISH_STORY);
        intent.putExtra("message", message);
        intent.putExtra("name", name);
        intent.putExtra("caption", caption);
        intent.putExtra("description", description);
        intent.putExtra("link", link);
        intent.putExtra("picture", picture);
        WeakRefParentActivity.get().startActivity(intent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateStoryDialog(String name, String caption, String description, String link, String picture,
                                  SocialCallbacks.SocialActionListener socialActionListener) {
        RefProvider = getProvider();
        RefSocialActionListener = socialActionListener;
        Intent intent = new Intent(WeakRefParentActivity.get(), SoomlaTwitterActivity.class);
        intent.putExtra("action", ACTION_PUBLISH_STORY_DIALOG);
        intent.putExtra("name", name);
        intent.putExtra("caption", caption);
        intent.putExtra("description", description);
        intent.putExtra("link", link);
        intent.putExtra("picture", picture);
        WeakRefParentActivity.get().startActivity(intent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getContacts(final SocialCallbacks.ContactsListener contactsListener) {
        RefProvider = getProvider();
        RefContactsListener = contactsListener;
        Intent intent = new Intent(WeakRefParentActivity.get(), SoomlaTwitterActivity.class);
        intent.putExtra("action", ACTION_GET_CONTACTS);
        WeakRefParentActivity.get().startActivity(intent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getFeed(final SocialCallbacks.FeedListener feedListener) {
        RefProvider = getProvider();
        RefFeedListener = feedListener;
        Intent intent = new Intent(WeakRefParentActivity.get(), SoomlaTwitterActivity.class);
        intent.putExtra("action", ACTION_GET_FEED);
        WeakRefParentActivity.get().startActivity(intent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void uploadImage(String message, String filePath, final SocialCallbacks.SocialActionListener socialActionListener) {
        SoomlaUtils.LogDebug(TAG, "uploadImage");
        RefProvider = getProvider();
        RefSocialActionListener = socialActionListener;
        Intent intent = new Intent(WeakRefParentActivity.get(), SoomlaTwitterActivity.class);
        intent.putExtra("action", ACTION_UPLOAD_IMAGE);
        intent.putExtra("message", message);
        intent.putExtra("filePath", filePath);
        WeakRefParentActivity.get().startActivity(intent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void uploadImage(String message, String fileName, Bitmap bitmap, int jpegQuality, final SocialCallbacks.SocialActionListener socialActionListener) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void like(final Activity parentActivity, String pageName) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/" + pageName));
        parentActivity.startActivity(browserIntent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Provider getProvider() {
        return Provider.TWITTER;
    }

    private static String getTwitterStorageKey(String postfix) {
        return DB_KEY_PREFIX + postfix;
    }
}
