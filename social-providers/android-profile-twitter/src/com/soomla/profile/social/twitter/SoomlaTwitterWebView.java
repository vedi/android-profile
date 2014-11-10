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
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.soomla.SoomlaApp;

import java.util.LinkedList;
import java.util.Queue;

public class SoomlaTwitterWebView extends WebView {
    public SoomlaTwitterWebView(Activity parentActivity) {
        super(parentActivity);

        this.getSettings().setJavaScriptEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            this.getSettings().setAllowUniversalAccessFromFileURLs(true);
        }

        this.mHandler = new Handler(Looper.getMainLooper());

        this.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d(TAG, cm.message() + " -- From line " + cm.lineNumber() + " of " + cm.sourceId());
                return true;
            }
        });

        this.setBackgroundColor(0x00000000);
        this.mTranslucent = true;
        postInvalidate();
    }

    public void loadUrlOnUiThread(final String url) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                loadUrl(url);
            }
        });
    }

    public void show(final Activity activity) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup vg = (ViewGroup)SoomlaTwitterWebView.this.getParent();
                if (vg != null) {
                    vg.removeView(SoomlaTwitterWebView.this);
                }
                enableDisableViewGroup((ViewGroup)activity.getWindow().getDecorView().findViewById(android.R.id.content), false);
                activity.addContentView(SoomlaTwitterWebView.this, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                setFocusableInTouchMode(true);
                requestFocus();
            }
        });
    }

    /**
     * Hide the window.
     */
    public void hide() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup vg = (ViewGroup)SoomlaTwitterWebView.this.getParent();
                if (vg != null) {
                    vg.removeView(SoomlaTwitterWebView.this);
                    enableDisableViewGroup(vg, true);
                }
            }
        });
    }

    public static void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = viewGroup.getChildAt(i);
            if (view != null) view.setEnabled(enabled);
            if (view instanceof ViewGroup) {
                enableDisableViewGroup((ViewGroup) view, enabled);
            }
        }
    }

    /**
     * There are rendering issues in WebKit when returning from a hook
     * this masks these issues. Looks a bit slow, but it does the job.
     */
    @Override
    protected void onWindowVisibilityChanged (int visibility) {
        if (visibility == VISIBLE) {
            makeTranslucent();
        }
        super.onWindowVisibilityChanged(visibility);
    }

    public void makeTranslucent() {
        if (!mTranslucent) {
            this.setBackgroundColor(0x00000000);
            mTranslucent = true;
        }
    }

    /** Members */

    private static String TAG = "SOOMLA TwitterWebView";

    /** UI members */
    private Handler mHandler;
    private boolean mTranslucent;
}
