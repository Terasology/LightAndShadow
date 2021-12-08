// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.lightandshadow.events;

import org.terasology.engine.network.ServerEvent;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * Trigger event to deactivate the pregame barriers.
 */
@ServerEvent
public class DelayedDeactivateBarrierEvent implements Event {
    private int delay;

    public DelayedDeactivateBarrierEvent(int delay) {
        this.delay = delay;
    }

    public int getDelay() {
        return delay;
    }
}
