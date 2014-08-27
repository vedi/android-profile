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

package com.soomla.profile.domain;

/**
 * An interface that represents a provider, which will be used later for
 * authentication and social actions.
 */
public interface IProvider {
    Provider getProvider();

    public enum Provider {
        FACEBOOK(0),
        FOURSQUARE(1),
        GOOGLE(2),
        LINKEDIN(3),
        MYSPACE(4),
        TWITTER(5),
        YAHOO(6),
        SALESFORCE(7),
        YAMMER(8),
        RUNKEEPER(9),
        INSTAGRAM(10),
        FLICKR(11);

        Provider(final int value) {
            this.mValue = value;
        }

        private final int mValue;

        public int getValue() {
            return mValue;
        }

        @Override
        public String toString() {
            String result = "";
            switch (this)
            {
                case FACEBOOK: result = "facebook";
                    break;
                case FOURSQUARE: result = "foursquare";
                    break;
                case GOOGLE: result = "google";
                    break;
                case LINKEDIN: result = "linkedin";
                    break;
                case MYSPACE: result = "myspace";
                    break;
                case TWITTER: result = "twitter";
                    break;
                case YAHOO: result = "yahoo";
                    break;
                case SALESFORCE: result = "salesforce";
                    break;
                case YAMMER: result = "yammer";
                    break;
                case RUNKEEPER: result = "runkeeper";
                    break;
                case INSTAGRAM: result = "instagram";
                    break;
                case FLICKR: result = "flickr";
                    break;
                default: throw new IllegalArgumentException();
            }

            return result;
        }

        public static Provider getEnum(String value) {
            for(Provider v : values()) {
                if (v.toString().equalsIgnoreCase(value)) return v;
            }
            throw new IllegalArgumentException();
        }
    }
}
