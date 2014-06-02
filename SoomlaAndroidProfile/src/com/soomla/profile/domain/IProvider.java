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
