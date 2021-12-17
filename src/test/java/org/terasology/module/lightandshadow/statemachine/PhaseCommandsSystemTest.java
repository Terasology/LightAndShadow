// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.lightandshadow.statemachine;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.module.lightandshadow.systems.GameEntitySystem;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PhaseCommandsSystemTest {
    PhaseCommandsSystem phaseCommandsSystem;

    @BeforeEach
    void setUp() {
        phaseCommandsSystem = new PhaseCommandsSystem();
        EntityRef gameEntity = new PojoEntityManager().create();
        IdlePhaseComponent idlePhaseComponent = new IdlePhaseComponent();
        gameEntity.saveComponent(idlePhaseComponent);
        createMockPhaseSystem(gameEntity);
    }

    private void createMockPhaseSystem(EntityRef gameEntity) {
        phaseCommandsSystem.phaseSystem = mock(PhaseSystem.class);
        when(phaseCommandsSystem.phaseSystem.gameEntitySystem.getGameEntity()).thenReturn(gameEntity);
    }

    @ParameterizedTest
    @EnumSource(PhaseSystem.Phase.class)
    void testForceIdlePhaseSuccessful(PhaseSystem.Phase currentPhase) {
        phaseCommandsSystem.forcePhase(PhaseSystem.Phase.IDLE);
        verify(phaseCommandsSystem.phaseSystem, times(1)).transitionPhase(currentPhase, PhaseSystem.Phase.IDLE);
    }

}