// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.lightandshadow.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.BroadcastEvent;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * Notification event to indicate that the flag has been picked up.
 */
@BroadcastEvent
public class OnFlagPickupEvent implements Event {
    private String team;
    private EntityRef player;

    public OnFlagPickupEvent() {
    }

    public OnFlagPickupEvent(EntityRef player, String team) {
        this.player = player;
        this.team = team;
    }

    public String getTeam() {
        return team;
    }

    public EntityRef getPlayer() {
        return player;
    }
}
