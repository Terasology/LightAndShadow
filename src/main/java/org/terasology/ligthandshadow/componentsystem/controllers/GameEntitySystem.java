// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.ligthandshadow.componentsystem.controllers;

import java.util.ArrayList;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.engine.registry.Share;
import org.terasology.ligthandshadow.componentsystem.components.InvulnerableComponent;
import org.terasology.ligthandshadow.componentsystem.components.LASConfigComponent;
import org.terasology.engine.registry.In;
import org.terasology.ligthandshadow.componentsystem.events.ActivateBarrierEvent;
import org.terasology.ligthandshadow.componentsystem.events.DelayedDeactivateBarrierEvent;
import org.terasology.ligthandshadow.componentsystem.events.PregameEvent;

import static org.terasology.ligthandshadow.componentsystem.LASUtils.DEACTIVATE_BARRIERS_ACTION;

/**
 *  Provides an entity that keeps track of game state information.
 */
@RegisterSystem
@Share(value = GameEntitySystem.class)
public class GameEntitySystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(GameEntitySystem.class);

    @In
    EntityManager entityManager;

    private EntityRef gameEntity = EntityRef.NULL;

    private boolean isPregamePhase = false;

    public EntityRef getGameEntity() {
        if (gameEntity.equals(EntityRef.NULL)) {
            ArrayList<EntityRef> gameEntities = Lists.newArrayList(entityManager.getEntitiesWith(LASConfigComponent.class));
            if (gameEntities.isEmpty()) {
                gameEntity = entityManager.create("LightAndShadow:gameEntity");
            } else if (gameEntities.size() == 1) {
                gameEntity = gameEntities.get(0);
            } else {
                logger.warn("Multiple game state entities available.");
            }
        }
        return gameEntity;
    }

    // can be called to determine whether a game has already started or players are still in the pre-game phase
    public boolean isPregamePhase() {
        return isPregamePhase;
    }

    @ReceiveEvent
    public void delayedDeactivateBarriers(DelayedDeactivateBarrierEvent event, EntityRef entity) {
        isPregamePhase = true;
    }

    @ReceiveEvent
    public void endPregamePhase(DelayedActionTriggeredEvent event, EntityRef entity) {
        if (event.getActionId().equals(DEACTIVATE_BARRIERS_ACTION)) {
            isPregamePhase = false;
        }
    }
}

