package com.soomla.profile.social.google;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.plus.model.moments.ItemScope;
import com.google.android.gms.plus.model.moments.Moment;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.soomla.SoomlaUtils;
import com.soomla.profile.auth.AuthCallbacks;
import com.soomla.profile.domain.UserProfile;
import com.soomla.profile.social.ISocialProvider;
import com.soomla.profile.social.SocialCallbacks;
import com.google.android.gms.common.ConnectionResult;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Soomla wrapper for GooglePlusAPIClient.
 * This class works by creating a transparent activity (SoomlaGooglePlusActivity) and working through it.
 * This is required to correctly integrate with GooglePlus activity lifecycle events
 */
public class SoomlaGooglePlus implements ISocialProvider{

    private static final String TAG = "SOOMLA SoomlaGoogle";

    private static GoogleApiClient GooglePlusAPIClient;
    private static WeakReference<Activity> WeakRefParentActivity;
    private static Provider RefProvider;
    private static AuthCallbacks.LoginListener RefLoginListener;
    private static SocialCallbacks.SocialActionListener RefSocialActionListener;

    public static final int ACTION_LOGIN = 0;
    public static final int ACTION_PUBLISH_STATUS = 1;
    public static final int ACTION_UPLOAD_IMAGE = 2;
    public static final int ACTION_PUBLISH_STORY = 3;
    public static final int ACTION_PUBLISH_STATUS_DIALOG = 4;

    /**
     * The main Soomla Google Plus activity
     * Handles GooglePlusAPIClient build and connection processes, as well as
     * it's API calls such as updating status, uploading image etc...
     */
    public static class SoomlaGooglePlusActivity extends Activity implements
            GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
        private static final String TAG = "SOOMLA SoomlaGoogle$SoomlaGoogleActivity";

        //request codes
        private static final int REQ_SIGN_IN = 0;
        private static final int REQ_SHARE = 1;

        //google plus connectivity monitors
        private static boolean mSignInRequested;
        private static boolean mConnectionInProgress;
        private static ConnectionResult mConnectionResult;

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            SoomlaUtils.LogDebug(TAG, "onCreate");

            Intent intent = getIntent();
            int userAction = intent.getIntExtra("action", -1);

            switch (userAction) {
                case ACTION_LOGIN: {
                    login();
                    break;
                }
                case ACTION_PUBLISH_STATUS: {
                    String status = intent.getStringExtra("status");
                    updateStatus(status);
                    finish();
                    break;
                }
                case ACTION_UPLOAD_IMAGE: {
                    String message = intent.getStringExtra("message");
                    String filePath = intent.getStringExtra("filepath");
                    uploadImage(message, filePath);
                    finish();
                    break;
                }
                case ACTION_PUBLISH_STORY: {
                    String message = intent.getStringExtra("message");
                    String name = intent.getStringExtra("name");
                    String caption = intent.getStringExtra("caption");
                    String description = intent.getStringExtra("description");
                    String link = intent.getStringExtra("link");
                    String picture = intent.getStringExtra("picture");
                    updateStory(message, name, caption, description, link, picture);
                    finish();
                    break;
                }
                case ACTION_PUBLISH_STATUS_DIALOG: {
                    String link = intent.getStringExtra("link");
                    updateStatusDialog(link);
                    finish();
                    break;
                }
            }
        }

        private void login() {
            GooglePlusAPIClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Plus.API, Plus.PlusOptions.builder().build())
                    .addScope(Plus.SCOPE_PLUS_LOGIN)
                    .build();

            if (!GooglePlusAPIClient.isConnecting()){
                mSignInRequested = true;
                GooglePlusAPIClient.connect();
            }
        }

        private void updateStatus(String status) {
            Intent shareIntent = new PlusShare.Builder(this)
                    .setType("text/plain")
                    .setText(status)
                    .getIntent();

            startActivityForResult(shareIntent, REQ_SHARE);
        }

        private void updateStatusDialog(String link) {
            Intent shareIntent = new PlusShare.Builder(this)
                    .setType("text/plain")
                    .setContentUrl(Uri.parse(link))
                    .getIntent();

            startActivityForResult(shareIntent, REQ_SHARE);
        }

        private void uploadImage(String message, String filePath) {
            try{
                Uri imageContentURI = getContentUri(filePath);
                ContentResolver cr = this.getContentResolver();
                String mime = cr.getType(imageContentURI);

                Intent shareIntent = new PlusShare.Builder(this)
                        .setText(message)
                        .addStream(imageContentURI)
                        .setType(mime)
                        .getIntent();

                startActivityForResult(shareIntent, REQ_SHARE);
            }catch (Exception e){
                RefSocialActionListener.fail("Failed uploading image with exception: " + e.getMessage());
            }
        }

        private void updateStory(String message, String name, String caption, String description, String link, String picture) {
            try{
                Intent shareIntent = new PlusShare.Builder(this)
                        .setText(message)
                        .setContentDeepLinkId("id", name, description, Uri.parse(picture))
                        .setText(message)
                        .setContentUrl(Uri.parse(link))
                        .addCallToAction(caption, Uri.parse(link), "id")
                        .getIntent();

                startActivityForResult(shareIntent, REQ_SHARE);
            }catch (Exception e){
                RefSocialActionListener.fail("Failed sharing story with exception: " + e.getMessage());
            }
        }

        //helper function to get content://uri from absolute path
        public Uri getContentUri(String absolutePath) {
            Cursor cursor = getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[] { MediaStore.Images.Media._ID },
                    MediaStore.Images.Media.DATA + "=? ",
                    new String[] { absolutePath }, null);
            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor
                        .getColumnIndex(MediaStore.MediaColumns._ID));
                Uri baseUri = Uri.parse("content://media/external/images/media");
                return Uri.withAppendedPath(baseUri, "" + id);
            } else {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, absolutePath);
                return getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }
        }

        @Override
        public void onConnected(Bundle bundle) {
            SoomlaUtils.LogDebug(TAG, "onConnected " + " [" + RefLoginListener + "]");
            mSignInRequested = false;
            RefLoginListener.success(RefProvider);
            finish();
        }

        @Override
        public void onConnectionSuspended(int i) {
            SoomlaUtils.LogDebug(TAG, "onConnectionSuspended");
            GooglePlusAPIClient.connect();
        }

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            SoomlaUtils.LogDebug(TAG, "onConnectionFailed");

            if (result.hasResolution()) {
                if (!mConnectionInProgress) {
                    mConnectionResult = result;

                    if (mSignInRequested)
                        resolveSignInError();
                }
            } else {
                RefLoginListener.fail("onConnectionFailed:" + result.getErrorCode() + " [" + RefLoginListener + "]");
                finish();
            }
        }

        private void resolveSignInError(){
            try {
                mConnectionInProgress = true;
                mConnectionResult.startResolutionForResult(this, REQ_SIGN_IN);

            }catch (IntentSender.SendIntentException e){
                mConnectionInProgress = false;
                GooglePlusAPIClient.connect();
            }
        }

        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case REQ_SIGN_IN: {
                    if (resultCode != RESULT_OK)
                        mSignInRequested = false;

                    mConnectionInProgress = false;

                    if (!GooglePlusAPIClient.isConnecting())
                        GooglePlusAPIClient.connect();
                    break;
                }

                case REQ_SHARE: {
                    if (resultCode == RESULT_OK)
                        RefSocialActionListener.success();
                    else
                        RefSocialActionListener.fail("Failed sharing with error code: " + resultCode);
                    break;
                }
            }
        }
    }

    @Override
    public void updateStatus(String status, SocialCallbacks.SocialActionListener socialActionListener) {
        RefProvider = getProvider();
        RefSocialActionListener = socialActionListener;
        Intent intent = new Intent(WeakRefParentActivity.get(), SoomlaGooglePlusActivity.class);
        intent.putExtra("action", ACTION_PUBLISH_STATUS);
        intent.putExtra("status", status);
        WeakRefParentActivity.get().startActivity(intent);
    }

    @Override
    public void updateStatusDialog(String link, SocialCallbacks.SocialActionListener socialActionListener) {
        RefProvider = getProvider();
        RefSocialActionListener = socialActionListener;
        Intent intent = new Intent(WeakRefParentActivity.get(), SoomlaGooglePlusActivity.class);
        intent.putExtra("action", ACTION_PUBLISH_STATUS_DIALOG);
        intent.putExtra("link", link);
        WeakRefParentActivity.get().startActivity(intent);
    }

    @Override
    public void updateStory(String message, String name, String caption, String description, String link, String picture, SocialCallbacks.SocialActionListener socialActionListener) {
        RefProvider = getProvider();
        RefSocialActionListener = socialActionListener;
        Intent intent = new Intent(WeakRefParentActivity.get(), SoomlaGooglePlusActivity.class);
        intent.putExtra("action", ACTION_PUBLISH_STORY);
        intent.putExtra("message", message);
        intent.putExtra("name", name);
        intent.putExtra("caption", caption);
        intent.putExtra("description", description);
        intent.putExtra("link", link);
        intent.putExtra("picture", picture);
        WeakRefParentActivity.get().startActivity(intent);
    }

    @Override
    public void updateStoryDialog(String name, String caption, String description, String link, String picture, SocialCallbacks.SocialActionListener socialActionListener) {
        //TODO:
        socialActionListener.fail("updateStoryDialog is not implemented");
    }

    @Override
    public void getContacts(final SocialCallbacks.ContactsListener contactsListener) {
        if (GooglePlusAPIClient != null && GooglePlusAPIClient.isConnected()){
            Plus.PeopleApi.loadVisible(GooglePlusAPIClient, null)
                    .setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
                        @Override
                        public void onResult(People.LoadPeopleResult peopleData) {
                            if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
                                List<UserProfile> userProfiles = new ArrayList<UserProfile>();
                                PersonBuffer personBuffer = peopleData.getPersonBuffer();
                                try {
                                    int count = personBuffer.getCount();
                                    for (int i = 0; i < count; i++) {
                                        Person profile = personBuffer.get(i);

                                        userProfiles.add(new UserProfile(RefProvider, profile.getId(), profile.getDisplayName(), "",
                                                profile.getName().getGivenName(), profile.getName().getFamilyName())); //TODO: emails (add scope: https://developers.google.com/+/mobile/android/people)

                                    }
                                    contactsListener.success(userProfiles);
                                } catch (Exception e){
                                    contactsListener.fail("Failed getting contacts with exception: " + e.getMessage());
                                }finally {
                                    personBuffer.close();
                                }
                            } else {
                                contactsListener.fail("Contact information is not available.");
                            }
                        }
                    });
        }else{
            contactsListener.fail("Failed getting contacts because because not connected to Google Plus.");
        }
    }


    @Override
    public void getFeed(SocialCallbacks.FeedListener feedsListener) {
        //TODO
        feedsListener.fail("getFeed is not implemented");
    }

    @Override
    public void uploadImage(String message, String filePath, SocialCallbacks.SocialActionListener socialActionListener) {
        SoomlaUtils.LogDebug(TAG, "uploadImage");
        RefProvider = getProvider();
        RefSocialActionListener = socialActionListener;
        Intent intent = new Intent(WeakRefParentActivity.get(), SoomlaGooglePlusActivity.class);
        intent.putExtra("action", ACTION_UPLOAD_IMAGE);
        intent.putExtra("message", message);
        intent.putExtra("filepath", filePath);
        WeakRefParentActivity.get().startActivity(intent);
    }

    @Override
    public void uploadImage(String message, String fileName, Bitmap bitmap, int jpegQuality, SocialCallbacks.SocialActionListener socialActionListener) {
        socialActionListener.fail("Not implemented");
    }

    @Override
    public void like(Activity parentActivity, String pageName) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/+" + pageName));
        parentActivity.startActivity(browserIntent);
    }

    @Override
    public void login(Activity parentActivity, AuthCallbacks.LoginListener loginListener) {
        SoomlaUtils.LogDebug(TAG, "login");
        WeakRefParentActivity = new WeakReference<Activity>(parentActivity);
        RefProvider = getProvider();
        RefLoginListener = loginListener;
        Intent intent = new Intent(parentActivity, SoomlaGooglePlusActivity.class);
        intent.putExtra("action", ACTION_LOGIN);
        parentActivity.startActivity(intent);
    }

    @Override
    public void getUserProfile(AuthCallbacks.UserProfileListener userProfileListener) {
        SoomlaUtils.LogDebug(TAG, "getUserProfile");
        try{
            Person profile = Plus.PeopleApi.getCurrentPerson(GooglePlusAPIClient);
            String email = Plus.AccountApi.getAccountName(GooglePlusAPIClient);
            final UserProfile userProfile = new UserProfile(getProvider(), profile.getId(),
                    profile.getDisplayName(), email,
                    profile.getName().getGivenName(), profile.getName().getFamilyName());
            userProfile.setAvatarLink(profile.getImage().getUrl());
            userProfileListener.success(userProfile);

        }catch (Exception e){
            userProfileListener.fail("Unable to get user profile with exception: " + e.getMessage());
        }
    }

    @Override
    public void logout(AuthCallbacks.LogoutListener logoutListener) {
        try {
            Plus.AccountApi.clearDefaultAccount(GooglePlusAPIClient);
            GooglePlusAPIClient.disconnect();
            logoutListener.success();
        } catch (Exception e) {
            logoutListener.fail("Failed to logout with exception: " + e.getMessage());
        }
    }

    @Override
    public boolean isLoggedIn(Activity activity) {
        return (GooglePlusAPIClient != null && GooglePlusAPIClient.isConnected());
    }

    @Override
    public Provider getProvider() {
        return Provider.GOOGLE;
    }
}
