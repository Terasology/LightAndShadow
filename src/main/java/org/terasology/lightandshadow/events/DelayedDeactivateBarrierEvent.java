// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadow.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.ServerEvent;

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
