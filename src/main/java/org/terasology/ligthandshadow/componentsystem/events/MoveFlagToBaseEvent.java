// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

/**
 * This trigger event is responsible for sending the flag back to the base.
 */
public class MoveFlagToBaseEvent implements Event {
    private final EntityRef heldFlag;
    private final String flagTeam;

    public MoveFlagToBaseEvent(EntityRef heldFlag, String flagTeam) {
        this.heldFlag = heldFlag;
        this.flagTeam = flagTeam;
    }

    public EntityRef getHeldFlag() {
        return heldFlag;
    }

    public String getFlagTeam() {
        return flagTeam;
    }
}
