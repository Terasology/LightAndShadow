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
import org.terasology.gestalt.naming.Name;
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

    enum Phase {
        IDLE(new Name("Idle")),
        PREGAME(new Name("Pregame")),
        COUNTDOWN(new Name("Countdown")),
        GAME(new Name("Game")),
        POSTGAME(new Name("Postgame"));

        private final Name phaseName;

        Phase(final Name phaseName) {
            this.phaseName = phaseName;
        }

        @Override
        public final String toString() {
            return phaseName.toString();
        }

        public Phase fromString(String phaseString) {
            Name input = new Name(phaseString);
            switch (input.toString()) {
                case IDLE.phaseName.toString():
            }
        }
    }

    @Override
    public void initialise() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        gameEntity.addOrSaveComponent(new IdlePhaseComponent());
    }

    // TODO: What happens here if there's multiple components present that implement PhaseComponent?
    public Phase getCurrentPhase() {
        return gameEntitySystem.getGameEntity().getComponent(PhaseComponent.class).toPhase();
    }

    public boolean isInIdlePhase() {
        return gameEntitySystem.getGameEntity().hasComponent(IdlePhaseComponent.class);
    }

    public boolean isInPregamePhase() {
        return gameEntitySystem.getGameEntity().hasComponent(PregamePhaseComponent.class);
    }

    public boolean isInCountdownPhase() {
        return gameEntitySystem.getGameEntity().hasComponent(CountdownPhaseComponent.class);
    }

    public boolean isInGamePhase() {
        return gameEntitySystem.getGameEntity().hasComponent(GamePhaseComponent.class);
    }

    public boolean isInPostgamePhase() {
        return gameEntitySystem.getGameEntity().hasComponent(PostgamePhaseComponent.class);
    }

    void transitionPhase(Phase from, Phase to) {
        logger.debug("Transitioning phases from " + from.toString() + " to " + to.toString());
        switch (from) {
            case IDLE:
                endIdlePhase();
                break;
            case PREGAME:
                endPregamePhase();
                break;
            case COUNTDOWN:
                endCountdownPhase();
                break;
            case GAME:
                endGamePhase();
                break;
            case POSTGAME:
                endPostgamePhase();
                break;
            default:
                logger.debug("Attempted to transition from invalid phase " + to.toString());
        }

        switch (to) {
            case IDLE:
                startIdlePhase();
                break;
            case PREGAME:
                startPregamePhase();
                break;
            case COUNTDOWN:
                startCountdownPhase();
                break;
            case GAME:
                startGamePhase();
                break;
            case POSTGAME:
                startPostgamePhase();
                break;
            default:
                logger.debug("Attempted to transition into invalid phase " + to.toString());
        }
    };

    private void endIdlePhase() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        if (gameEntity.hasComponent(IdlePhaseComponent.class)) {
            gameEntity.removeComponent(IdlePhaseComponent.class);
        } else {
            logger.debug("Ending idle phase without `IdlePhaseComponent` being present.");
        }
        gameEntity.send(new OnIdlePhaseEndedEvent());
    };

    private void endPregamePhase() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        if (gameEntity.hasComponent(PregamePhaseComponent.class)) {
            gameEntity.removeComponent(PregamePhaseComponent.class);
        } else {
            logger.debug("Ending pregame phase without `PregamePhaseComponent` being present.");
        }
        gameEntity.send(new OnPregamePhaseEndedEvent());
    };

    private void endCountdownPhase() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        if (gameEntity.hasComponent(CountdownPhaseComponent.class)) {
            gameEntity.removeComponent(CountdownPhaseComponent.class);
        } else {
            logger.debug("Ending countdown phase without `CountdownPhaseComponent` being present.");
        }
        gameEntity.send(new OnCountdownPhaseEndedEvent());
    };

    private void endGamePhase() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        if (gameEntity.hasComponent(GamePhaseComponent.class)) {
            gameEntity.removeComponent(GamePhaseComponent.class);
        } else {
            logger.debug("Ending game phase without `GamePhaseComponent` being present.");
        }
        gameEntity.send(new OnGamePhaseEndedEvent());
    };

    private void endPostgamePhase() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        if (gameEntity.hasComponent(PostgamePhaseComponent.class)) {
            gameEntity.removeComponent(PostgamePhaseComponent.class);
        } else {
            logger.debug("Ending postgame phase without `PostgamePhaseComponent` being present.");
        }
        gameEntity.send(new OnPostgamePhaseEndedEvent());
    };

    private void startIdlePhase() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        gameEntity.addOrSaveComponent(new IdlePhaseComponent());
        gameEntity.send(new OnIdlePhaseStartedEvent());
    };

    private void startPregamePhase() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        gameEntity.addOrSaveComponent(new PregamePhaseComponent());
        gameEntity.send(new OnPregamePhaseStartedEvent());
    };

    private void startCountdownPhase() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        gameEntity.addOrSaveComponent(new CountdownPhaseComponent());
        gameEntity.send(new OnCountdownPhaseStartedEvent());
    };

    private void startGamePhase() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        gameEntity.addOrSaveComponent(new GamePhaseComponent());
        gameEntity.send(new OnGamePhaseStartedEvent());
    };

    private void startPostgamePhase() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        gameEntity.addOrSaveComponent(new PostgamePhaseComponent());
        gameEntity.send(new OnPostgamePhaseStartedEvent());
    };
}
