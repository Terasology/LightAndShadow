// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.BroadcastEvent;

@BroadcastEvent
public class FlagDropEvent implements Event {
    public EntityRef player;

    public FlagDropEvent() {
    }

    public FlagDropEvent(EntityRef player) {
        this.player = player;
    }
}
