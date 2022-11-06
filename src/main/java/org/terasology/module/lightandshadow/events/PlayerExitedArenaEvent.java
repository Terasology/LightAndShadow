// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.lightandshadow.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.event.Event;

public class PlayerExitedArenaEvent implements Event {
    private final EntityRef player;

    public PlayerExitedArenaEvent(EntityRef player) {
        this.player = player;
    }

    public EntityRef getPlayer() {
        return player;
    }
}
