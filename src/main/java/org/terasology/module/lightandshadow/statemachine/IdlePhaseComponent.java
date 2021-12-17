// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.lightandshadow.statemachine;

public class IdlePhaseComponent implements PhaseComponent {

    public final PhaseSystem.Phase toPhase() {
        return PhaseSystem.Phase.IDLE;
    }

    @Override
    public void copyFrom(PhaseComponent other) {

    }
}
