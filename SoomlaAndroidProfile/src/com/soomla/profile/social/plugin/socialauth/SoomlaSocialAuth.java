package com.soomla.profile.social.plugin.socialauth;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.soomla.profile.SocialProviderAggregator;
import com.soomla.profile.SoomlaProfile;
import com.soomla.profile.auth.AuthCallbacks;
import com.soomla.profile.domain.UserProfile;
import com.soomla.profile.exceptions.ProviderNotSupportedException;
import com.soomla.profile.social.SocialCallbacks;
import com.soomla.store.SoomlaApp;
import com.soomla.store.StoreUtils;

import org.brickred.socialauth.Profile;
import org.brickred.socialauth.android.DialogListener;
import org.brickred.socialauth.android.SocialAuthAdapter;
import org.brickred.socialauth.android.SocialAuthError;
import org.brickred.socialauth.android.SocialAuthListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by refaelos on 29/05/14.
 */
public class SoomlaSocialAuth extends SocialProviderAggregator {

    private SocialAuthAdapter mSocialAuthAdapter;

    private Map<String, SocialAuthAdapter.Provider> mProviderLookup =
            new HashMap<String, SocialAuthAdapter.Provider>();

    public SoomlaSocialAuth() {

        mProviderLookup.put(Provider.FACEBOOK.toString(), SocialAuthAdapter.Provider.FACEBOOK);
        mProviderLookup.put(Provider.FOURSQUARE.toString(), SocialAuthAdapter.Provider.FOURSQUARE);
        mProviderLookup.put(Provider.GOOGLE.toString(), SocialAuthAdapter.Provider.GOOGLE);
        mProviderLookup.put(Provider.LINKEDIN.toString(), SocialAuthAdapter.Provider.LINKEDIN);
        mProviderLookup.put(Provider.MYSPACE.toString(), SocialAuthAdapter.Provider.MYSPACE);
        mProviderLookup.put(Provider.TWITTER.toString(), SocialAuthAdapter.Provider.TWITTER);
        mProviderLookup.put(Provider.YAHOO.toString(), SocialAuthAdapter.Provider.YAHOO);
        mProviderLookup.put(Provider.SALESFORCE.toString(), SocialAuthAdapter.Provider.SALESFORCE);
        mProviderLookup.put(Provider.YAMMER.toString(), SocialAuthAdapter.Provider.YAMMER);
        mProviderLookup.put(Provider.RUNKEEPER.toString(), SocialAuthAdapter.Provider.RUNKEEPER);
        mProviderLookup.put(Provider.INSTAGRAM.toString(), SocialAuthAdapter.Provider.INSTAGRAM);
        mProviderLookup.put(Provider.FLICKR.toString(), SocialAuthAdapter.Provider.FLICKR);

        mSocialAuthAdapter = new SocialAuthAdapter(new DialogListener() {
            @Override
            public void onComplete(Bundle bundle) {
                StoreUtils.LogDebug(TAG, "Login completed");
                mLoginListener.success();
                mLoginListener = null;
            }

            @Override
            public void onError(SocialAuthError socialAuthError) {
                StoreUtils.LogError(TAG, socialAuthError.getMessage());
                mLoginListener.fail(socialAuthError.getMessage());
                mLoginListener = null;
            }

            @Override
            public void onCancel() {
                StoreUtils.LogDebug(TAG, "Login canceled");
                mLoginListener.cancel();
                mLoginListener = null;
            }

            @Override
            public void onBack() {
                StoreUtils.LogDebug(TAG, "Login canceled (back)");
                mLoginListener.cancel();
                mLoginListener = null;
            }
        });
    }

    @Override
    public void setCurrentProvider(Provider currentAuthProvider) throws ProviderNotSupportedException {
        super.setCurrentProvider(currentAuthProvider);

        if (!mProviderLookup.keySet().contains(currentAuthProvider.toString())) {
            throw new ProviderNotSupportedException();
        }

    }

    @Override
    public void updateStatus(String status, final SocialCallbacks.SocialActionListener socialActionListener) {
        mSocialAuthAdapter.updateStatus(status, new SocialAuthListener<Integer>() {
            @Override
            public void onExecute(String provider, Integer status) {
                if (status == 200 || status == 201 || status == 204) {
                    socialActionListener.success();
                }
                else {
                    socialActionListener.fail("Update status operation failed.  (" + status + ")");
                }
            }

            @Override
            public void onError(SocialAuthError socialAuthError) {
                socialActionListener.fail(socialAuthError.getMessage());
            }
        }, false);
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
        mLoginListener = loginListener;
        // Context context = SoomlaApp.getAppContext(); // will crash Dialog
        mSocialAuthAdapter.authorize(activity, mProviderLookup.get(mCurrentProvider.toString()));
    }

    @Override
    public void getUserProfile(final AuthCallbacks.UserProfileListener userProfileListener) {
        mSocialAuthAdapter.getUserProfileAsync(new SocialAuthListener<Profile>() {
            @Override
            public void onExecute(String provider, Profile profile) {
                UserProfile userProfile = new UserProfile(getProviderId() + "." + provider, profile.getValidatedId());
                userProfile.setFirstName(profile.getFirstName());
                userProfile.setLastName(profile.getLastName());
                userProfile.setAvatarLink(profile.getProfileImageURL());

                userProfileListener.success(userProfile);
            }

            @Override
            public void onError(SocialAuthError socialAuthError) {
                userProfileListener.fail(socialAuthError.getMessage());
            }
        });
    }

    @Override
    public void logout(AuthCallbacks.AuthListener authListener) {
        mSocialAuthAdapter.signOut(SoomlaApp.getAppContext(), mCurrentProvider.toString());
        authListener.success();
    }

    @Override
    public String getProviderId() {
        return "SocialAuth";
    }

    private static final String TAG = "SOOMLA SoomlaSocialAuth";

    private AuthCallbacks.LoginListener mLoginListener;
}
