// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.lightandshadow.systems;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.module.lightandshadow.components.CountdownPhaseComponent;
import org.terasology.module.lightandshadow.components.GamePhaseComponent;
import org.terasology.module.lightandshadow.components.IdlePhaseComponent;
import org.terasology.module.lightandshadow.components.PostgamePhaseComponent;
import org.terasology.module.lightandshadow.components.PregamePhaseComponent;

/**
 *  Provides an entity that keeps track of game state information.
 */
@RegisterSystem
@Share(value = PhaseSystem.class)
public class PhaseSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(PhaseSystem.class);

    @In
    EntityManager entityManager;

    @In
    private GameEntitySystem gameEntitySystem;

    @Override
    public void initialise() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        gameEntity.addOrSaveComponent(new IdlePhaseComponent());
    }

    // TODO: Would it make more sense to ask for the current phase instead or even in addition?

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
}
