// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.lightandshadow.statemachine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.module.lightandshadow.phases.IdlePhaseComponent;
import org.terasology.module.lightandshadow.phases.PhaseSystem;
import org.terasology.module.lightandshadow.systems.GameEntitySystem;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PhaseSystemTest {
    PhaseSystem phaseSystem;
    private GameEntitySystem gameEntitySystem;

    @BeforeEach
    void setUp() {
        phaseSystem = new PhaseSystem();
        EntityRef gameEntity = new PojoEntityManager().create();
        IdlePhaseComponent idlePhaseComponent = new IdlePhaseComponent();
        gameEntity.saveComponent(idlePhaseComponent);
        createMockGameEntitySystem(gameEntity);
    }

    private void createMockGameEntitySystem(EntityRef gameEntity) {
        phaseSystem.gameEntitySystem = mock(GameEntitySystem.class);
        when(phaseSystem.gameEntitySystem.getGameEntity()).thenReturn(gameEntity);
    }

    @ParameterizedTest
    @EnumSource(PhaseSystem.Phase.class)
    void transitionToIdlePhase(PhaseSystem.Phase from) {

    }
}