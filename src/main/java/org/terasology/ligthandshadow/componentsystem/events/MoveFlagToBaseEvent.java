// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;


public class MoveFlagToBaseEvent implements Event {
    public EntityRef heldFlag;
    public String flagTeam;

    public MoveFlagToBaseEvent(EntityRef heldFlag, String flagTeam) {
        this.heldFlag = heldFlag;
        this.flagTeam = flagTeam;
    }
}
