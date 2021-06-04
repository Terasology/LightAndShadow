// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.ligthandshadow.componentsystem.controllers;

import java.util.ArrayList;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.Share;
import org.terasology.ligthandshadow.componentsystem.components.LASConfigComponent;
import org.terasology.engine.registry.In;

@RegisterSystem
@Share(value = GameEntitySystem.class)
public class GameEntitySystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(GameEntitySystem.class);

    @In
    EntityManager entityManager;

    private EntityRef gameEntity = EntityRef.NULL;

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
}

