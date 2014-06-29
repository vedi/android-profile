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
 * Created by refaelos on 29/05/14.
 */
public interface IProvider {
    Provider getProvider();

    public enum Provider {
        FACEBOOK("facebook"),
        FOURSQUARE("foursquare"),
        GOOGLE("google"),
        LINKEDIN("linkedin"),
        MYSPACE("myspace"),
        TWITTER("twitter"),
        YAHOO("yahoo"),
        SALESFORCE("salesforce"),
        YAMMER("yammer"),
        RUNKEEPER("runkeeper"),
        INSTAGRAM("instagram"),
        FLICKR("flickr");

        Provider(final String text) {
            this.mValue = text;
        }

        private final String mValue;

        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return mValue;
        }

        public String getValue() {
            return mValue;
        }

        public static Provider getEnum(String value) {
            for(Provider v : values()) {
                if (v.getValue().equalsIgnoreCase(value)) return v;
            }
            throw new IllegalArgumentException();
        }
    }
}
