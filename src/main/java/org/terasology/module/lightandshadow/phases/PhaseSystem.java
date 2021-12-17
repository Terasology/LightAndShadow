// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.lightandshadow.phases;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.module.lightandshadow.systems.GameEntitySystem;

/**
 *  Provides an entity that keeps track of game state information.
 */
@RegisterSystem
@Share(value = PhaseSystem.class)
public class PhaseSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(PhaseSystem.class);

    @In
    GameEntitySystem gameEntitySystem;

    @Override
    public void initialise() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        gameEntity.addOrSaveComponent(new PhaseComponent());
    }

    // TODO: What happens here if there's multiple components present that implement PhaseComponent?
    public Phase getCurrentPhase() {
        return gameEntitySystem.getGameEntity().getComponent(PhaseComponent.class).getCurrentPhase();
    }

    void transitionPhase(Phase from, Phase to) {
        logger.debug("Transitioning phases from " + from.toString() + " to " + to.toString());
        switch (from) {
            case IDLE:
                endIdlePhase();
                break;
            case PRE_GAME:
                endPreGamePhase();
                break;
            case COUNTDOWN:
                endCountdownPhase();
                break;
            case IN_GAME:
                endInGamePhase();
                break;
            case POST_GAME:
                endPostGamePhase();
                break;
            default:
                logger.debug("Attempted to transition from invalid phase " + to.toString());
        }

        switch (to) {
            case IDLE:
                startIdlePhase();
                break;
            case PRE_GAME:
                startPreGamePhase();
                break;
            case COUNTDOWN:
                startCountdownPhase();
                break;
            case IN_GAME:
                startInGamePhase();
                break;
            case POST_GAME:
                startPostGamePhase();
                break;
            default:
                logger.debug("Attempted to transition into invalid phase " + to.toString());
        }
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

    private void endIdlePhase() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        assertInPhase(Phase.IDLE, gameEntity);
        gameEntity.send(new OnIdlePhaseEndedEvent());
    };

    private void endPreGamePhase() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        assertInPhase(Phase.PRE_GAME, gameEntity);
        gameEntity.send(new OnPreGamePhaseEndedEvent());
    };

    private void endCountdownPhase() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        assertInPhase(Phase.COUNTDOWN, gameEntity);
        gameEntity.send(new OnCountdownPhaseEndedEvent());
    };

    private void endInGamePhase() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        assertInPhase(Phase.IN_GAME, gameEntity);
        gameEntity.send(new OnInGamePhaseEndedEvent());
    };

    private void endPostGamePhase() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        assertInPhase(Phase.POST_GAME, gameEntity);
        gameEntity.send(new OnPostGamePhaseEndedEvent());
    };

    private void startIdlePhase() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        gameEntity.updateComponent(PhaseComponent.class, phaseComponent -> {
            phaseComponent.setCurrentPhase(Phase.IDLE);
            return phaseComponent;
        });
        gameEntity.send(new OnIdlePhaseStartedEvent());
    };

    private void startPreGamePhase() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        gameEntity.updateComponent(PhaseComponent.class, phaseComponent -> {
            phaseComponent.setCurrentPhase(Phase.PRE_GAME);
            return phaseComponent;
        });
        gameEntity.send(new OnPreGamePhaseStartedEvent());
    };

    private void startCountdownPhase() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        gameEntity.updateComponent(PhaseComponent.class, phaseComponent -> {
            phaseComponent.setCurrentPhase(Phase.COUNTDOWN);
            return phaseComponent;
        });
        gameEntity.send(new OnCountdownPhaseStartedEvent());
    };

    private void startInGamePhase() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        gameEntity.updateComponent(PhaseComponent.class, phaseComponent -> {
            phaseComponent.setCurrentPhase(Phase.IN_GAME);
            return phaseComponent;
        });
        gameEntity.send(new OnInGamePhaseStartedEvent());
    };

    private void startPostGamePhase() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        gameEntity.updateComponent(PhaseComponent.class, phaseComponent -> {
            phaseComponent.setCurrentPhase(Phase.POST_GAME);
            return phaseComponent;
        });
        gameEntity.send(new OnPostGamePhaseStartedEvent());
    };
}
