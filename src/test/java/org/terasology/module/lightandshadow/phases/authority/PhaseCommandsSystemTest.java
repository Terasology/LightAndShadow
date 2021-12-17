// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.lightandshadow.phases.authority;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.module.lightandshadow.phases.Phase;

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
        gameEntity.saveComponent(new PhaseComponent());
        createMockPhaseSystem(gameEntity);
    }

    private void createMockPhaseSystem(EntityRef gameEntity) {
        phaseCommandsSystem.phaseSystem = mock(PhaseSystem.class);
        when(phaseCommandsSystem.phaseSystem.gameEntitySystem.getGameEntity()).thenReturn(gameEntity);
    }

    @ParameterizedTest
    @EnumSource(Phase.class)
    void testForceIdlePhaseSuccessful(Phase currentPhase) {
        phaseCommandsSystem.forcePhase(EntityRef.NULL, Phase.IDLE); // TODO: use non-NULL entity
        verify(phaseCommandsSystem.phaseSystem, times(1)).transitionPhase(currentPhase, Phase.IDLE);
    }

}