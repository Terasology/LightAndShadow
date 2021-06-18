// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;


public class FlagDropEvent implements Event {
    private final EntityRef attackingPlayer;
    private final String flagTeam;

    public FlagDropEvent(EntityRef attackingPlayer, String flagTeam) {
        this.attackingPlayer = attackingPlayer;
        this.flagTeam = flagTeam;
    }

    public EntityRef getAttackingPlayer() {
        return attackingPlayer;
    }

    public String getFlagTeam() {
        return flagTeam;
    }
}
