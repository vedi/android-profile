package com.soomla.profile.social.socialauth;

import android.app.Activity;
import android.os.Bundle;

import com.soomla.profile.IProvider;
import com.soomla.profile.auth.AuthCallbacks;
import com.soomla.profile.domain.UserProfile;
import com.soomla.profile.social.ISocialProvider;
import com.soomla.profile.social.SocialCallbacks;
import com.soomla.store.SoomlaApp;
import com.soomla.store.StoreUtils;

import org.brickred.socialauth.Profile;
import org.brickred.socialauth.android.DialogListener;
import org.brickred.socialauth.android.SocialAuthAdapter;
import org.brickred.socialauth.android.SocialAuthError;
import org.brickred.socialauth.android.SocialAuthListener;

/**
 * Created by refaelos on 29/05/14.
 */
public abstract class SoomlaSocialAuth implements ISocialProvider {

    private static SocialAuthAdapter mSocialAuthAdapter;

    public SoomlaSocialAuth() {

        if (mSocialAuthAdapter == null) {
            mSocialAuthAdapter = new SocialAuthAdapter(new DialogListener() {
                @Override
                public void onComplete(Bundle bundle) {
                    SocialAuthAdapter.Provider saProvider = saProviderFronSAName(bundle.getString(SocialAuthAdapter.PROVIDER));
                    StoreUtils.LogDebug(TAG, "Login completed for SocialAuth provider: " + saProvider.name());
                    if (mLoginListener != null) {
                        mLoginListener.success(providerFromSAProvider(saProvider));
                        mLoginListener = null;
                    }
                }

                @Override
                public void onError(SocialAuthError socialAuthError) {
                    StoreUtils.LogError(TAG, socialAuthError.getMessage());
                    if (mLoginListener != null) {
                        mLoginListener.fail(socialAuthError.getMessage());
                        mLoginListener = null;
                    }
                }

                @Override
                public void onCancel() {
                    StoreUtils.LogDebug(TAG, "Login canceled");
                    if (mLoginListener != null) {
                        mLoginListener.cancel();
                        mLoginListener = null;
                    }
                }

                @Override
                public void onBack() {
                    StoreUtils.LogDebug(TAG, "Login canceled (back)");
                    if (mLoginListener != null) {
                        mLoginListener.cancel();
                        mLoginListener = null;
                    }
                }
            });
        }
    }

    @Override
    public void updateStatus(Activity activity, final String status, final SocialCallbacks.SocialActionListener socialActionListener) {
        login(activity, new AuthCallbacks.LoginListener() {
            @Override
            public void success(Provider provider) {
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
            public void fail(String message) {
                socialActionListener.fail(message);
            }

            @Override
            public void cancel() {
                socialActionListener.fail("Login cancelled by user");
            }
        });
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
        mSocialAuthAdapter.authorize(activity, saProviderFromProvider(getProvider()));
    }

    @Override
    public void getUserProfile(final AuthCallbacks.UserProfileListener userProfileListener) {
        mSocialAuthAdapter.getUserProfileAsync(new SocialAuthListener<Profile>() {
            @Override
            public void onExecute(String providerName, Profile profile) {
                UserProfile userProfile = new UserProfile(getProvider(), profile.getValidatedId());
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
        mSocialAuthAdapter.signOut(SoomlaApp.getAppContext(), saProviderFromProvider(getProvider()).name());
        authListener.success();
    }

    private static final String TAG = "SOOMLA SoomlaSocialAuth";

    private AuthCallbacks.LoginListener mLoginListener;



    private SocialAuthAdapter.Provider saProviderFronSAName(String saProviderName) {
        for (SocialAuthAdapter.Provider saProvider : SocialAuthAdapter.Provider.values()) {
            if (saProvider.name().toLowerCase().equals(saProviderName.toLowerCase())) {
                return saProvider;
            }
        }
        return null;
    }

    private SocialAuthAdapter.Provider saProviderFromProvider(IProvider.Provider provider) {
        if (provider == IProvider.Provider.FACEBOOK) {
            return SocialAuthAdapter.Provider.FACEBOOK;
        } else if (provider == IProvider.Provider.FOURSQUARE) {
            return SocialAuthAdapter.Provider.FOURSQUARE;
        } else if (provider == IProvider.Provider.GOOGLE) {
            return SocialAuthAdapter.Provider.GOOGLE;
        } else if (provider == IProvider.Provider.LINKEDIN) {
            return SocialAuthAdapter.Provider.LINKEDIN;
        } else if (provider == IProvider.Provider.MYSPACE) {
            return SocialAuthAdapter.Provider.MYSPACE;
        } else if (provider == IProvider.Provider.TWITTER) {
            return SocialAuthAdapter.Provider.TWITTER;
        } else if (provider == IProvider.Provider.YAHOO) {
            return SocialAuthAdapter.Provider.YAHOO;
        } else if (provider == IProvider.Provider.SALESFORCE) {
            return SocialAuthAdapter.Provider.SALESFORCE;
        } else if (provider == IProvider.Provider.YAMMER) {
            return SocialAuthAdapter.Provider.YAMMER;
        } else if (provider == IProvider.Provider.RUNKEEPER) {
            return SocialAuthAdapter.Provider.RUNKEEPER;
        } else if (provider == IProvider.Provider.INSTAGRAM) {
            return SocialAuthAdapter.Provider.INSTAGRAM;
        } else if (provider == IProvider.Provider.FLICKR) {
            return SocialAuthAdapter.Provider.FLICKR;
        }
        return null;
    }


    private IProvider.Provider providerFromSAProvider(SocialAuthAdapter.Provider saProvider) {
        if (saProvider == SocialAuthAdapter.Provider.FACEBOOK) {
            return IProvider.Provider.FACEBOOK;
        } else if (saProvider == SocialAuthAdapter.Provider.FOURSQUARE) {
            return IProvider.Provider.FOURSQUARE;
        } else if (saProvider == SocialAuthAdapter.Provider.GOOGLE) {
            return IProvider.Provider.GOOGLE;
        } else if (saProvider == SocialAuthAdapter.Provider.LINKEDIN) {
            return IProvider.Provider.LINKEDIN;
        } else if (saProvider == SocialAuthAdapter.Provider.MYSPACE) {
            return IProvider.Provider.MYSPACE;
        } else if (saProvider == SocialAuthAdapter.Provider.TWITTER) {
            return IProvider.Provider.TWITTER;
        } else if (saProvider == SocialAuthAdapter.Provider.YAHOO) {
            return IProvider.Provider.YAHOO;
        } else if (saProvider == SocialAuthAdapter.Provider.SALESFORCE) {
            return IProvider.Provider.SALESFORCE;
        } else if (saProvider == SocialAuthAdapter.Provider.YAMMER) {
            return IProvider.Provider.YAMMER;
        } else if (saProvider == SocialAuthAdapter.Provider.RUNKEEPER) {
            return IProvider.Provider.RUNKEEPER;
        } else if (saProvider == SocialAuthAdapter.Provider.INSTAGRAM) {
            return IProvider.Provider.INSTAGRAM;
        } else if (saProvider == SocialAuthAdapter.Provider.FLICKR) {
            return IProvider.Provider.FLICKR;
        }
        return null;
    }

}
