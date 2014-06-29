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

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.soomla.SoomlaApp;
import com.soomla.SoomlaUtils;
import com.soomla.profile.domain.IProvider;
import com.soomla.profile.exceptions.ProviderNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A parent class that provides functionality for dynamic loading of providers.
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
                SoomlaUtils.LogError(TAG, err);
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
                SoomlaUtils.LogDebug(TAG, "Failed to load provider from AndroidManifest.xml. manifest key: " + manifestKey);
                return null;
            }

            providerArray = SoomlaApp.getAppContext().getResources().getStringArray(ai.metaData.getInt(manifestKey));

        } catch (Exception e) {
            SoomlaUtils.LogDebug(TAG, "Failed to load provider from AndroidManifest.xml, NullPointer: " + e.getMessage());
            return null;
        }

        if (providerArray == null || providerArray.length == 0) {
            SoomlaUtils.LogDebug(TAG, "Failed to load provider from AndroidManifest.xml. manifest key: " + manifestKey);
            return null;
        }

        List<Class<? extends T>> providers = new ArrayList<Class<? extends T>>();
        for(String providerItem : providerArray) {
            Class<? extends T> aClass = null;
            try {
                SoomlaUtils.LogDebug(TAG, "Trying to load class " + providerItem);
                aClass = (Class<? extends T>) Class.forName(providerPkgPrefix + providerItem);
                providers.add(aClass);
            } catch (ClassNotFoundException e) {
                SoomlaUtils.LogDebug(TAG, "Failed loading class " + providerItem + " Exception: " + e.getLocalizedMessage());
            }
        }

        return providers;
    }

    protected void handleErrorResult(String message) {
//        BusProvider.getInstance().post(new UnexpectedStoreErrorEvent(message));
        SoomlaUtils.LogError(TAG, "ERROR: " + message);
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
