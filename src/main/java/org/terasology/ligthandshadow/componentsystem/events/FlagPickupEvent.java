// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.BroadcastEvent;

@BroadcastEvent
public class FlagPickupEvent implements Event {
    public String team;
    public EntityRef player;

    public FlagPickupEvent() {
    }

    public FlagPickupEvent(EntityRef player, String team) {
        this.player = player;
        this.team = team;
    }
}
