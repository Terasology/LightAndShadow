// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.BroadcastEvent;

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
