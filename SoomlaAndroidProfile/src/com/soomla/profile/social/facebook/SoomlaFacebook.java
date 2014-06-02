package com.soomla.profile.social.facebook;

import android.app.Activity;
import android.graphics.Bitmap;

import com.soomla.profile.auth.AuthCallbacks;
import com.soomla.profile.social.ISocialProvider;
import com.soomla.profile.social.SocialCallbacks;

/**
 * Created by refaelos on 01/06/14.
 */
public class SoomlaFacebook implements ISocialProvider {

    private static String TAG = "SOOMLA SoomlaFacebook";

    @Override
    public void updateStatus(String status, SocialCallbacks.SocialActionListener socialActionListener) {
        throw new UnsupportedOperationException("SoomlaFacebook unimplemented");
    }

    @Override
    public void updateStory(String message, String name, String caption, String description, String link, String picture, SocialCallbacks.SocialActionListener socialActionListener) {
        throw new UnsupportedOperationException("SoomlaFacebook unimplemented");
    }

    @Override
    public void uploadImage(String message, String fileName, Bitmap bitmap, int jpegQuality, SocialCallbacks.SocialActionListener socialActionListener) {
        throw new UnsupportedOperationException("SoomlaFacebook unimplemented");
    }

    @Override
    public void getContacts(SocialCallbacks.ContactsListener contactsListener) {
        throw new UnsupportedOperationException("SoomlaFacebook unimplemented");
    }

    @Override
    public void getFeeds(SocialCallbacks.FeedsListener feedsListener) {
        throw new UnsupportedOperationException("SoomlaFacebook unimplemented");
    }

    @Override
    public void login(Activity activity, AuthCallbacks.LoginListener loginListener) {
        throw new UnsupportedOperationException("SoomlaFacebook unimplemented");
//        final Intent intent = new Intent(SoomlaApp.getAppContext(), FacebookActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//        mLoginListener = loginListener;
//        SoomlaApp.getAppContext().startActivity(intent);
    }

    @Override
    public void getUserProfile(AuthCallbacks.UserProfileListener userProfileListener) {
        throw new UnsupportedOperationException("SoomlaFacebook unimplemented");
    }

    @Override
    public void logout(AuthCallbacks.LogoutListener logoutListener) {
        throw new UnsupportedOperationException("SoomlaFacebook unimplemented");
    }

    @Override
    public Provider getProvider() {
        return Provider.FACEBOOK;
    }

    private AuthCallbacks.LoginListener mLoginListener;

//    public static class FacebookActivity extends Activity {
//
//        @Override
//        protected void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//
//            Session.openActiveSession(this, true, new Session.StatusCallback() {
//
//                // callback when session changes state
//                @Override
//                public void call(Session session, SessionState state, Exception exception) {
//                    if (session.isOpened()) {
//
//                        // make request to the /me API
//                        Request.newMeRequest(session, new Request.GraphUserCallback() {
//
//                            // callback after Graph API response with user object
//                            @Override
//                            public void onCompleted(GraphUser user, Response response) {
//                                if (user != null) {
//                                    StoreUtils.LogDebug(TAG, "Login successful! user name: " + user.getName());
//                                }
//                            }
//                        }).executeAsync();
//                    }
//                }
//            });
//        }
//
//        @Override
//        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//            super.onActivityResult(requestCode, resultCode, data);
//            Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
//        }
//    }
}
