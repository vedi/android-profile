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

package com.soomla.profile.events.gameservices;

import com.soomla.profile.domain.IProvider;
import com.soomla.profile.domain.gameservices.Leaderboard;
import com.soomla.profile.domain.gameservices.Score;

import java.util.List;

public class SubmitScoreFinishedEvent extends BaseGameServicesEvent {
    public final com.soomla.profile.domain.gameservices.Leaderboard Leaderboard;
    public final Score Score;

    public SubmitScoreFinishedEvent(IProvider.Provider provider, Leaderboard leaderboard, Score score, String payload) {
        super(provider, payload);
        this.Leaderboard = leaderboard;
        this.Score = score;
    }
}
