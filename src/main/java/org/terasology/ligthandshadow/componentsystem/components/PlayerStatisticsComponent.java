/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.ligthandshadow.componentsystem.components;

import org.terasology.entitySystem.Component;
import org.terasology.network.Replicate;

/**
 * Component to store a players game statistics.
 * It currently stores deaths and kills.
 *
 */
public class PlayerStatisticsComponent implements Component {
    @Replicate
    public int kills;

    @Replicate
    public int deaths;

    public  PlayerStatisticsComponent() {
        this.kills = 0;
        this.deaths = 0;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

}
