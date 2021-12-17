// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.lightandshadow.phases;

import org.terasology.engine.network.BroadcastEvent;
import org.terasology.engine.entitySystem.event.Event;

// TODO: Would it make more sense to have a single "OnPhaseTransition" with old and new phase here?

@BroadcastEvent
public class OnIdlePhaseEndedEvent implements Event {

    public OnIdlePhaseEndedEvent() {
    }
}
