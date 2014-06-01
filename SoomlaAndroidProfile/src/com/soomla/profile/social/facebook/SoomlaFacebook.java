package com.soomla.profile.social.facebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.android.Facebook;
import com.facebook.model.GraphUser;
import com.soomla.profile.auth.AuthCallbacks;
import com.soomla.profile.social.ISocialProvider;
import com.soomla.profile.social.SocialCallbacks;
import com.soomla.store.SoomlaApp;
import com.soomla.store.StoreUtils;

/**
 * Created by refaelos on 01/06/14.
 */
public class SoomlaFacebook implements ISocialProvider {

    private static String TAG = "SOOMLA SoomlaFacebook";

    @Override
    public void updateStatus(Activity activity, String status, SocialCallbacks.SocialActionListener socialActionListener) {

    }

    @Override
    public void updateStory() {

    }

    @Override
    public void getProfile() {

    }

    @Override
    public void getContacts() {

    }

    @Override
    public void login(Activity activity, AuthCallbacks.LoginListener loginListener) {
        final Intent intent = new Intent(SoomlaApp.getAppContext(), FacebookActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        mLoginListener = loginListener;
        SoomlaApp.getAppContext().startActivity(intent);
    }

    @Override
    public void getUserProfile(AuthCallbacks.UserProfileListener userProfileListener) {
        
    }

    @Override
    public void logout(AuthCallbacks.AuthListener authListener) {

    }

    @Override
    public Provider getProvider() {
        return Provider.FACEBOOK;
    }

    private AuthCallbacks.LoginListener mLoginListener;

    public static class FacebookActivity extends Activity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            Session.openActiveSession(this, true, new Session.StatusCallback() {

                // callback when session changes state
                @Override
                public void call(Session session, SessionState state, Exception exception) {
                    if (session.isOpened()) {

                        // make request to the /me API
                        Request.newMeRequest(session, new Request.GraphUserCallback() {

                            // callback after Graph API response with user object
                            @Override
                            public void onCompleted(GraphUser user, Response response) {
                                if (user != null) {
                                    StoreUtils.LogDebug(TAG, "Login successful! user name: " + user.getName());
                                }
                            }
                        }).executeAsync();
                    }
                }
            });
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
        }
    }
}
