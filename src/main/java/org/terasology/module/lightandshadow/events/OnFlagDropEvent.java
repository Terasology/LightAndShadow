// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.lightandshadow.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.BroadcastEvent;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * Notification event to indicate that the flag has been dropped.
 */
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
