// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.lightandshadow.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

/**
 * This trigger event causes the player to drop the flag.
 */
public class DropFlagEvent implements Event {
    private final EntityRef attackingPlayer;

    public DropFlagEvent(EntityRef attackingPlayer) {
        this.attackingPlayer = attackingPlayer;
    }

    public EntityRef getAttackingPlayer() {
        return attackingPlayer;
    }

}
