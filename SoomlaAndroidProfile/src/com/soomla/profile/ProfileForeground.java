package com.soomla.profile;

import com.soomla.BusProvider;
import com.soomla.SoomlaApp;
import com.soomla.profile.domain.IProvider;
import com.soomla.profile.events.auth.LoginCancelledEvent;
import com.soomla.profile.events.auth.LoginFailedEvent;
import com.soomla.profile.events.auth.LoginFinishedEvent;
import com.soomla.profile.events.auth.LoginStartedEvent;
import com.soomla.profile.events.social.SocialActionCancelledEvent;
import com.soomla.profile.events.social.SocialActionFailedEvent;
import com.soomla.profile.events.social.SocialActionFinishedEvent;
import com.soomla.profile.events.social.SocialActionStartedEvent;
import com.squareup.otto.Subscribe;

public class ProfileForeground {

    private ProfileForeground() {
        BusProvider.getInstance().register(this);
    }

    public static synchronized ProfileForeground get() {
        if (sInstance == null) {
            sInstance = new ProfileForeground();
        }
        return sInstance;
    }

    private static ProfileForeground sInstance;


    @Subscribe
    public void onLoginFinishedEvent(LoginFinishedEvent loginFinishedEvent) {
        // Facebook and Google are the only providers that have outside activities for now
        if (loginFinishedEvent.getProvider() != IProvider.Provider.FACEBOOK &&
                loginFinishedEvent.getProvider() != IProvider.Provider.GOOGLE) {
            return;
        }

        if (SoomlaApp.ForegroundService != null) {
            SoomlaApp.ForegroundService.OutsideOperation = false;
        }
    }

    @Subscribe
    public void onLoginFailedEvent(LoginFailedEvent loginFailedEvent) {
        // Facebook and Google are the only providers that have outside activities for now
        if (loginFailedEvent.Provider != IProvider.Provider.FACEBOOK &&
                loginFailedEvent.Provider != IProvider.Provider.GOOGLE) {
            return;
        }

        if (SoomlaApp.ForegroundService != null) {
            SoomlaApp.ForegroundService.OutsideOperation = false;
        }
    }

    @Subscribe
    public void onLoginCancelledEvent(LoginCancelledEvent loginCancelledEvent) {
        // Facebook and Google are the only providers that have outside activities for now
        if (loginCancelledEvent.Provider != IProvider.Provider.FACEBOOK &&
                loginCancelledEvent.Provider != IProvider.Provider.GOOGLE) {
            return;
        }

        if (SoomlaApp.ForegroundService != null) {
            SoomlaApp.ForegroundService.OutsideOperation = false;
        }
    }

    @Subscribe
    public void onLoginStartedEvent(LoginStartedEvent loginStartedEvent) {

        // Facebook and Google are the only providers that have outside activities for now
        if (loginStartedEvent.Provider != IProvider.Provider.FACEBOOK &&
                loginStartedEvent.Provider != IProvider.Provider.GOOGLE) {
            return;
        }

        // ok, it's Facebook or Google. Lets tell ForegroundService that's it's an outside operation
        if (SoomlaApp.ForegroundService != null) {
            SoomlaApp.ForegroundService.OutsideOperation = true;
        }
    }

    @Subscribe
    public void onSocialActionStartedEvent(SocialActionStartedEvent socialActionStartedEvent) {

        // Google is the only providers that have outside activities for now
        if (socialActionStartedEvent.Provider != IProvider.Provider.GOOGLE) {
            return;
        }

        // ok, it's Google. Lets tell ForegroundService that's it's an outside operation
        if (SoomlaApp.ForegroundService != null) {
            SoomlaApp.ForegroundService.OutsideOperation = true;
        }
    }

    @Subscribe
    public void onSocialActionCancelledEvent(SocialActionCancelledEvent socialActionCancelledEvent) {

        // Google is the only providers that have outside activities for now
        if (socialActionCancelledEvent.Provider != IProvider.Provider.GOOGLE) {
            return;
        }

        // ok, it's Google. Lets tell ForegroundService that's it's an outside operation
        if (SoomlaApp.ForegroundService != null) {
            SoomlaApp.ForegroundService.OutsideOperation = false;
        }
    }

    @Subscribe
    public void onSocialActionFailedEvent(SocialActionFailedEvent socialActionFailedEvent) {

        // Google is the only providers that have outside activities for now
        if (socialActionFailedEvent.Provider != IProvider.Provider.GOOGLE) {
            return;
        }

        // ok, it's Google. Lets tell ForegroundService that's it's an outside operation
        if (SoomlaApp.ForegroundService != null) {
            SoomlaApp.ForegroundService.OutsideOperation = false;
        }
    }


    @Subscribe
    public void onSocialActionCancelledEvent(SocialActionFinishedEvent socialActionFinishedEvent) {

        // Google is the only providers that have outside activities for now
        if (socialActionFinishedEvent.Provider != IProvider.Provider.GOOGLE) {
            return;
        }

        // ok, it's Google. Lets tell ForegroundService that's it's an outside operation
        if (SoomlaApp.ForegroundService != null) {
            SoomlaApp.ForegroundService.OutsideOperation = false;
        }
    }
}
