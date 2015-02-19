/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.ligthandshadow.logic;

import com.google.common.collect.Maps;
import org.terasology.entitySystem.Component;
import org.terasology.network.Replicate;

import java.util.Map;

@Replicate
public class TeamPointsComponent implements Component {
    @Replicate
    Map<String, Integer> teamPoints = Maps.newHashMap();

    public void addPoints(String team, int points) {
        String normalizedTeam = team.toLowerCase();
        int currentPoints = getPoints(normalizedTeam);
        teamPoints.put(normalizedTeam, currentPoints + points);
    }

    public Integer getPoints(String team) {
        String normalizedTeam = team.toLowerCase();
        int currentPoints = 0;
        if (teamPoints.containsKey(normalizedTeam)) {
            currentPoints = teamPoints.get(normalizedTeam);
        }
        return currentPoints;
    }
}
