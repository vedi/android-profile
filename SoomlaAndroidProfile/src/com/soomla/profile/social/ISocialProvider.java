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

package com.soomla.profile.social;

import android.graphics.Bitmap;

import com.soomla.profile.auth.IAuthProvider;

/**
 * A provider that exposes social capabilities such as sharing, fetching user feeds, uploading images etc.
 */
public interface ISocialProvider extends IAuthProvider {

    /**
     * Shares the given status to the user's feed
     *
     * @param status the text to share
     * @param socialActionListener a set of callbacks for this action
     */
    void updateStatus(String status, SocialCallbacks.SocialActionListener socialActionListener);

    /**
     * Share a story to the user's feed.  This is very oriented for Facebook.
     *
     * @param message
     * @param name
     * @param caption
     * @param description
     * @param link
     * @param picture
     * @param socialActionListener
     */
    void updateStory(String message, String name, String caption, String description,
                     String link, String picture,
                     SocialCallbacks.SocialActionListener socialActionListener);

    /**
     * Fetches the user's contact list
     *
     * @param contactsListener a set of callbacks for this action
     */
    void getContacts(SocialCallbacks.ContactsListener contactsListener);

    /**
     * Fetches the user's feed.
     *
     * @param feedsListener a set of callbacks for this action
     */
    void getFeeds(SocialCallbacks.FeedsListener feedsListener);

    /**
     * Shares a photo to the user's feed
     *
     * @param message A text that will accompany the image
     * @param filePath The desired image's location on the device
     * @param socialActionListener a set of callbacks for this action
     */
    void uploadImage(String message, String filePath,
                     SocialCallbacks.SocialActionListener socialActionListener);

    /**
     * Share's a photo to the user's feed
     *
     * @param message A text that will accompany the image
     * @param fileName Where bitmap will be saved before upload
     * @param bitmap Bitmap to be uploaded
     * @param jpegQuality Hint to the compressor, 0-100. 0 meaning compress for small size,
     *                    100 meaning compress for max quality. Some formats,
     *                    like PNG which is lossless, will ignore the quality setting
     * @param socialActionListener a set of callbacks for this action
     */
    void uploadImage(String message, String fileName, Bitmap bitmap, int jpegQuality,
                     SocialCallbacks.SocialActionListener socialActionListener);

    public enum SocialActionType {
        UPDATE_STATUS
        , UPDATE_STORY
        , UPLOAD_IMAGE
        , GET_CONTACTS
        , GET_FEEDS
    }
}
