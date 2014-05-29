package com.soomla.profile;

/**
 * Created by refaelos on 29/05/14.
 */
public interface IProvider {
    String getProviderId();

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

        private Provider(final String text) {
            this.text = text;
        }

        private final String text;

        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return text;
        }

        public String getText() {
            return text;
        }

        public static Provider getEnum(String value) {
            for(Provider v : values()) {
                if (v.getText().equalsIgnoreCase(value)) return v;
            }
            throw new IllegalArgumentException();
        }
    }
}
