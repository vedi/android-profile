/*
 * Copyright (C) 2012-2015 Soomla Inc.
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

package com.soomla.profile.gameservices;

import android.app.Activity;

import com.soomla.profile.auth.IAuthProvider;
import com.soomla.profile.domain.gameservices.Score;

import java.util.List;

/**
 * A utility class that defines interfaces for passing callbacks to game services events.
 */
public class GameServicesCallbacks {

    public interface SuccessWithListListener <T> {
        /**
         * Performs the following function upon success.
         */
        public void success(List<T> result, boolean hasMore);

        /**
         * Performs the following function upon failure and prints the given message.
         *
         * @param message reason for failure
         */
        public void fail(String message);
    }

    public interface SuccessWithScoreListener {
        /**
         * Performs the following function upon success.
         */
        public void success(Score score);

        /**
         * Performs the following function upon failure and prints the given message.
         *
         * @param message reason for failure
         */
        public void fail(String message);
    }
}