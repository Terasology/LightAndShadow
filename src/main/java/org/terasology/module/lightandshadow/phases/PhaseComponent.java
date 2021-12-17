// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.lightandshadow.phases;

import org.terasology.gestalt.entitysystem.component.Component;

/**
 * Read-only component holding the current state of a Light and Shadow game.
 */
public class PhaseComponent implements Component<PhaseComponent> {

    private Phase currentPhase;

    PhaseComponent() {
        this.currentPhase = Phase.IDLE;
    }

    public Phase getCurrentPhase() {
        return currentPhase;
    }

    /**
     * Package-private setter to only allow the authority system to update the game state.
     * @param newPhase the new active game phase
     */
    void setCurrentPhase(Phase newPhase) {
        this.currentPhase = newPhase;
    }

    @Override
    public void copyFrom(PhaseComponent other) {
        this.currentPhase = other.currentPhase;
    }
}
