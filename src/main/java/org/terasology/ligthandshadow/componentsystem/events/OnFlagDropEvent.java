// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.BroadcastEvent;

@BroadcastEvent
public class OnFlagDropEvent implements Event {
    private EntityRef player;

    public OnFlagDropEvent() {
    }

    public OnFlagDropEvent(EntityRef player) {
        this.player = player;
    }

    public EntityRef getPlayer() {
        return player;
    }
}
