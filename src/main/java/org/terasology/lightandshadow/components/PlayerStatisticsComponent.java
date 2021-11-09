// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadow.components;

import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.engine.network.Replicate;

/**
 * Component to store a players game statistics.
 * It currently stores deaths and kills.
 *
 */
public class PlayerStatisticsComponent implements Component<PlayerStatisticsComponent> {
    @Replicate
    public int kills;

    @Replicate
    public int deaths;

    public  PlayerStatisticsComponent() {
        this.kills = 0;
        this.deaths = 0;
    }

    @Override
    public void copyFrom(PlayerStatisticsComponent other) {
        this.kills = other.kills;
        this.deaths = other.deaths;
    }
}
