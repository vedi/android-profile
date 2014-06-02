/*
 * Copyright (C) 2012 Soomla Inc.
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

public class UserProfile {

    public UserProfile(IProvider.Provider provider, String profileId, String email, String firstName, String lastName) {
        mProvider = provider;
        mProfileId = profileId;
        mEmail = email;
        mFirstName = firstName;
        mLastName = lastName;
    }

    public UserProfile(String userProfileJSON) {
        throw new UnsupportedOperationException();
    }

    public String toJSON() {
        return "";
    }

    /** Setters and Getters **/

    public IProvider.Provider getProvider() {
        return mProvider;
    }

    public String getProfileId() {
        return mProfileId;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        this.mEmail = email;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        this.mFirstName = firstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lastName) {
        this.mLastName = lastName;
    }

    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    public String getAvatarLink() {
        return mAvatarLink;
    }

    public void setAvatarLink(String avatarLink) {
        this.mAvatarLink = avatarLink;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        this.mLocation = location;
    }

    public String getGender() {
        return mGender;
    }

    public void setGender(String gender) {
        this.mGender = gender;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public void setLanguage(String language) {
        this.mLanguage = language;
    }

    public String getBirthday() {
        return mBirthday;
    }

    public void setBirthday(String birthday) {
        this.mBirthday = birthday;
    }

    /** Private Members **/

    private IProvider.Provider mProvider;

    private String mProfileId;
    private String mEmail;
    private String mFirstName;
    private String mLastName;
    private String mAvatarLink;
    private String mLocation;
    private String mGender;
    private String mLanguage;
    private String mBirthday;
}
