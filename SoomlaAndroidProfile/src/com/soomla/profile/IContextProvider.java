package com.soomla.profile;

import android.app.Activity;
import android.content.Context;

/**
 * Created by refaelos on 29/05/14.
 */
public interface IContextProvider {
    Activity getActivity();
    Context getContext();
}
