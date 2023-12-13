// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.lightandshadow.phases.authority;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.module.lightandshadow.phases.Phase;
import org.terasology.module.lightandshadow.systems.GameEntitySystem;

import java.util.stream.Stream;

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
        phaseCommandsSystem.phaseSystem.gameEntitySystem = mock(GameEntitySystem.class);
        when(phaseCommandsSystem.phaseSystem.gameEntitySystem.getGameEntity()).thenReturn(gameEntity);
    }

    static Stream<Arguments> phaseProvider() {
        Stream.Builder<Arguments> argumentsBuilder = Stream.builder();
        for (Phase fromPhase : Phase.values()) {
            for (Phase toPhase : Phase.values()) {
                argumentsBuilder.add(Arguments.of(fromPhase, toPhase));
            }
        }

        return argumentsBuilder.build();
    }

    @ParameterizedTest
    @MethodSource("phaseProvider")
    void testForcePhaseSuccessful(Phase currentPhase, Phase targetPhase) {
        // Arrange
        when(phaseCommandsSystem.phaseSystem.getCurrentPhase()).thenReturn(currentPhase);
        // Act
        phaseCommandsSystem.forcePhase(EntityRef.NULL, targetPhase);
        // Assert
        verify(phaseCommandsSystem.phaseSystem, times(1)).getCurrentPhase();
        verify(phaseCommandsSystem.phaseSystem, times(1)).transitionPhase(currentPhase, targetPhase);
    }
}
