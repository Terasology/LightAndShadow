// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;


public class FlagDropRequestEvent implements Event {
    public EntityRef attackingPlayer;
    public String flagTeam;

    public FlagDropRequestEvent(EntityRef attackingPlayer, String flagTeam) {
        this.attackingPlayer = attackingPlayer;
        this.flagTeam = flagTeam;
    }
}
