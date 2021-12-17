// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.lightandshadow.phases.authority;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.module.lightandshadow.phases.OnCountdownPhaseEndedEvent;
import org.terasology.module.lightandshadow.phases.OnCountdownPhaseStartedEvent;
import org.terasology.module.lightandshadow.phases.OnIdlePhaseEndedEvent;
import org.terasology.module.lightandshadow.phases.OnIdlePhaseStartedEvent;
import org.terasology.module.lightandshadow.phases.OnInGamePhaseEndedEvent;
import org.terasology.module.lightandshadow.phases.OnInGamePhaseStartedEvent;
import org.terasology.module.lightandshadow.phases.OnPostGamePhaseEndedEvent;
import org.terasology.module.lightandshadow.phases.OnPostGamePhaseStartedEvent;
import org.terasology.module.lightandshadow.phases.OnPreGamePhaseEndedEvent;
import org.terasology.module.lightandshadow.phases.OnPreGamePhaseStartedEvent;
import org.terasology.module.lightandshadow.phases.Phase;
import org.terasology.module.lightandshadow.systems.GameEntitySystem;

/**
 *  Provides an entity that keeps track of game state information.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
@Share(value = PhaseSystem.class)
public class PhaseSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(PhaseSystem.class);

    @In
    GameEntitySystem gameEntitySystem;

    @Override
    public void postBegin() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        gameEntity.addOrSaveComponent(new PhaseComponent());
        logger.info("hello dog");
        logger.debug("Initializing PhaseSystem with game entity:\n{}", gameEntity.toFullDescription());
    }

    // TODO: What happens here if there's multiple components present that implement PhaseComponent?
    public Phase getCurrentPhase() {
        return gameEntitySystem.getGameEntity().getComponent(PhaseComponent.class).getCurrentPhase();
    }

    void transitionPhase(Phase from, Phase to) {
        logger.debug("Transitioning phases from " + from + " to " + to);
        endPhase(from);
        startPhase(to);
    };

    private void assertInPhase(Phase expectedPhase, EntityRef gameEntity) {
        PhaseComponent phase = gameEntity.getComponent(PhaseComponent.class);

        if (phase == null || phase.getCurrentPhase() != expectedPhase) {
            String message = "LightAndShadow/PhaseSystem: Attempting to end idle phase without being in `" + expectedPhase + "` phase.";
            String entityDump = gameEntity.toFullDescription();
            throw new RuntimeException(message + "\n" + entityDump);
        }
        gameEntity.send(new OnIdlePhaseEndedEvent());
    }

    private void endPhase(Phase endingPhase) {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        assertInPhase(endingPhase, gameEntity);
        gameEntity.send(endingPhase.endEvent.get());
    }

    private void startPhase(Phase startingPhase) {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        gameEntity.updateComponent(PhaseComponent.class, phaseComponent -> {
            phaseComponent.setCurrentPhase(startingPhase);
            return phaseComponent;
        });
        gameEntity.send(startingPhase.startEvent.get());
    }
}
