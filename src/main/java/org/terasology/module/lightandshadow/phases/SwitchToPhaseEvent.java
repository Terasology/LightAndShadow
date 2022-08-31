// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.lightandshadow.phases;

import org.terasology.gestalt.entitysystem.event.Event;

public class SwitchToPhaseEvent implements Event {
    public Phase targetPhase;

    public SwitchToPhaseEvent() { }

    public SwitchToPhaseEvent(Phase targetPhase) {
        this.targetPhase = targetPhase;
    }
}
