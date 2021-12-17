// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.lightandshadow.phases;

public class GamePhaseComponent implements PhaseComponent {

    public final PhaseSystem.Phase toPhase() {
        return PhaseSystem.Phase.GAME;
    }

    @Override
    public void copyFrom(PhaseComponent other) {

    }
}
