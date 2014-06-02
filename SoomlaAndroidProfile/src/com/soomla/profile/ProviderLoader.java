package com.soomla.profile;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.soomla.profile.domain.IProvider;
import com.soomla.profile.exceptions.ProviderNotFoundException;
import com.soomla.store.BusProvider;
import com.soomla.store.SoomlaApp;
import com.soomla.store.StoreUtils;
import com.soomla.store.events.UnexpectedStoreErrorEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by refaelos on 29/05/14.
 */
public abstract class ProviderLoader<T extends IProvider> {

    protected boolean loadProviders(String manifestKey, String providerPkgPrefix) {
        List<Class<? extends T>> providerClss = tryFetchProviders(manifestKey, providerPkgPrefix);
        if (providerClss == null || providerClss.size() == 0) {
            return false;
        }

        mProviders = new HashMap<IProvider.Provider, T>();
        for (Class<? extends T> aClass : providerClss) {
            try {
                T provider = aClass.newInstance();
                mProviders.put(provider.getProvider(), provider);
            } catch (Exception e) {
                String err = "Couldn't instantiate provider class. Something's totally wrong here.";
                StoreUtils.LogError(TAG, err);
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private List<Class<? extends T>> tryFetchProviders(String manifestKey, String providerPkgPrefix) {
        final String[] providerArray;
        try {
            ApplicationInfo ai = SoomlaApp.getAppContext().getPackageManager().getApplicationInfo(
                    SoomlaApp.getAppContext().getPackageName(), PackageManager.GET_META_DATA);
            if (ai.metaData == null) {
                StoreUtils.LogDebug(TAG, "Failed to load provider from AndroidManifest.xml. manifest key: " + manifestKey);
                return null;
            }

            providerArray = SoomlaApp.getAppContext().getResources().getStringArray(ai.metaData.getInt(manifestKey));

        } catch (Exception e) {
            StoreUtils.LogDebug(TAG, "Failed to load provider from AndroidManifest.xml, NullPointer: " + e.getMessage());
            return null;
        }

        if (providerArray == null || providerArray.length == 0) {
            StoreUtils.LogDebug(TAG, "Failed to load provider from AndroidManifest.xml. manifest key: " + manifestKey);
            return null;
        }

        List<Class<? extends T>> providers = new ArrayList<Class<? extends T>>();
        for(String providerItem : providerArray) {
            Class<? extends T> aClass = null;
            try {
                StoreUtils.LogDebug(TAG, "Trying to load class " + providerItem);
                aClass = (Class<? extends T>) Class.forName(providerPkgPrefix + providerItem);
                providers.add(aClass);
            } catch (ClassNotFoundException e) {
                StoreUtils.LogDebug(TAG, "Failed loading class " + providerItem + " Exception: " + e.getLocalizedMessage());
            }
        }

        return providers;
    }

    protected void handleErrorResult(String message) {
        BusProvider.getInstance().post(new UnexpectedStoreErrorEvent(message));
        StoreUtils.LogError(TAG, "ERROR: " + message);
    }


    protected T getProvider(IProvider.Provider provider) throws ProviderNotFoundException {
        final T providerObj = mProviders.get(provider);
        if(providerObj == null) {
            throw new ProviderNotFoundException(provider);
        }

        return providerObj;
    }

    /** Private Members **/

    protected Map<IProvider.Provider, T> mProviders = new HashMap<IProvider.Provider, T>();

    private static String TAG = "SOOMLA ProviderLoader";
}
