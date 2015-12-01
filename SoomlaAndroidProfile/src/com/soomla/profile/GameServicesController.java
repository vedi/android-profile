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

package com.soomla.profile;

import com.soomla.BusProvider;
import com.soomla.SoomlaUtils;
import com.soomla.profile.domain.IProvider;
import com.soomla.profile.domain.UserProfile;
import com.soomla.profile.domain.gameservices.Leaderboard;
import com.soomla.profile.domain.gameservices.Score;
import com.soomla.profile.events.gameservices.*;
import com.soomla.profile.events.social.GetContactsFailedEvent;
import com.soomla.profile.events.social.GetContactsFinishedEvent;
import com.soomla.profile.events.social.GetContactsStartedEvent;
import com.soomla.profile.exceptions.ProviderNotFoundException;
import com.soomla.profile.gameservices.GameServicesCallbacks;
import com.soomla.profile.gameservices.IGameServicesProvider;
import com.soomla.profile.social.ISocialProvider;
import com.soomla.rewards.Reward;

import java.util.List;
import java.util.Map;

/**
 * A class that loads all game services providers and performs game services
 * actions on with them.  This class wraps the provider's game services
 * actions in order to connect them to user profile data and rewards.
 * <p/>
 * Inheritance: {@link com.soomla.profile.GameServicesController} >
 * {@link com.soomla.profile.AuthController} >
 * {@link com.soomla.profile.ProviderLoader}
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class GameServicesController extends AuthController<IGameServicesProvider> {

    /**
     * Constructor
     * <p/>
     * Loads all game services providers
     * * @param usingExternalProvider {@link SoomlaProfile#initialize}
     */
    public GameServicesController(boolean usingExternalProvider, Map<IProvider.Provider, ? extends Map<String, String>> profileParams) {
        super(usingExternalProvider, profileParams);

        if (!usingExternalProvider && !loadProviders(
                profileParams
                //TODO: add concrete providers
                )) {

            String msg = "You don't have a IGameServicesProvider service attached. " +
                    "Decide which IGameServicesProvider you want, add it to AndroidManifest.xml " +
                    "and add its jar to the path.";
            SoomlaUtils.LogDebug(TAG, msg);
        }
    }

    /**
     * Fetches the user's contact list
     *
     * @param provider The provider to use
     * @param fromStart Should we reset pagination or request the next page
     * @param payload  a String to receive when the function returns.
     * @param reward   The reward to grant
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void getContacts(final IProvider.Provider provider,
                            final boolean fromStart, final String payload, final Reward reward) throws ProviderNotFoundException {

        final IGameServicesProvider gsProvider = getProvider(provider);

        final ISocialProvider.SocialActionType getContactsType = ISocialProvider.SocialActionType.GET_CONTACTS;
        BusProvider.getInstance().post(new GetContactsStartedEvent(provider, getContactsType, fromStart, payload));
        gsProvider.getContacts(fromStart, new GameServicesCallbacks.SuccessWithListListener<UserProfile>() {
            @Override
            public void success(List<UserProfile> result, boolean hasMore) {
                BusProvider.getInstance().post(new GetContactsFinishedEvent(provider, getContactsType, result, payload, hasMore));

                if (reward != null) {
                    reward.give();
                }
            }

            @Override
            public void fail(String message) {
                BusProvider.getInstance().post(new GetContactsFailedEvent(provider, getContactsType, message, fromStart, payload));
            }
        });
    }

    /**
     Fetches the game's leaderboards list

     * @param provider The provider to use
     * @param payload  a String to receive when the function returns.
     * @param reward   The reward to grant
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void getLeaderboards(final IProvider.Provider provider, final String payload, final Reward reward) throws ProviderNotFoundException {
        final IGameServicesProvider gsProvider = getProvider(provider);

        BusProvider.getInstance().post(new GetLeaderboardsStartedEvent(provider, payload));
        gsProvider.getLeaderboards(new GameServicesCallbacks.SuccessWithListListener<Leaderboard>() {
            @Override
            public void success(List<Leaderboard> result, boolean hasMore) {
                BusProvider.getInstance().post(new GetLeaderboardsFinishedEvent(provider, result, hasMore, payload));

                if (reward != null) {
                    reward.give();
                }
            }

            @Override
            public void fail(String message) {
                BusProvider.getInstance().post(new GetLeaderboardsFailedEvent(provider, message, payload));
            }
        });
    }

    /**
     Fetches the game's scores list from specified leaderboard

     * @param provider The provider to use
     * @param leaderboard Leaderboard containing desired scores list
     * @param fromStart Should we reset pagination or request the next page
     * @param payload  a String to receive when the function returns.
     * @param reward   The reward to grant
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void getScores(final IProvider.Provider provider, final Leaderboard leaderboard, final boolean fromStart, final String payload, final Reward reward) throws ProviderNotFoundException {
        final IGameServicesProvider gsProvider = getProvider(provider);

        BusProvider.getInstance().post(new GetScoresStartedEvent(provider, leaderboard, payload));
        gsProvider.getScores(leaderboard.getID(), fromStart, new GameServicesCallbacks.SuccessWithListListener<Score>() {
            @Override
            public void success(List<Score> result, boolean hasMore) {
                BusProvider.getInstance().post(new GetScoresFinishedEvent(provider, leaderboard, result, hasMore, payload));

                if (reward != null) {
                    reward.give();
                }
            }

            @Override
            public void fail(String message) {
                BusProvider.getInstance().post(new GetScoresFailedEvent(provider, leaderboard, message, payload));
            }
        });
    }

    /**
     Submits scores to specified leaderboard

     * @param provider The provider to use
     * @param leaderboard Leaderboard containing desired scores list
     * @param value Value to report
     * @param payload  a String to receive when the function returns.
     * @param reward   The reward to grant
     * @throws ProviderNotFoundException if the supplied provider is not
     *                                   supported by the framework
     */
    public void submitScore(final IProvider.Provider provider, final Leaderboard leaderboard, final long value, final String payload, final Reward reward) throws ProviderNotFoundException {
        final IGameServicesProvider gsProvider = getProvider(provider);

        BusProvider.getInstance().post(new SubmitScoreStartedEvent(provider, leaderboard, payload));
        gsProvider.submitScore(leaderboard.getID(), value, new GameServicesCallbacks.SuccessWithScoreListener() {
            @Override
            public void success(Score score) {
                BusProvider.getInstance().post(new SubmitScoreFinishedEvent(provider, leaderboard, score, payload));

                if (reward != null) {
                    reward.give();
                }
            }

            @Override
            public void fail(String message) {
                BusProvider.getInstance().post(new SubmitScoreFailedEvent(provider, leaderboard, message, payload));
            }
        });
    }

    private static final String TAG = "SOOMLA GameServicesController";
}