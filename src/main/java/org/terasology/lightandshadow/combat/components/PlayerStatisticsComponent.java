// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadow.combat.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;

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
}
