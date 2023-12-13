// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.lightandshadow.systems;

import java.util.ArrayList;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.engine.logic.console.commandSystem.annotations.Sender;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.logic.players.PlayerCharacterComponent;
import org.terasology.engine.registry.Share;
import org.terasology.lightandshadowresources.components.LASTeamComponent;
import org.terasology.module.lightandshadow.LASUtils;
import org.terasology.module.lightandshadow.components.LASConfigComponent;
import org.terasology.engine.registry.In;
import org.terasology.module.lightandshadow.components.LASTeamStatsComponent;

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

    /**
     * Updates the team statistics stored in the LASTeamStatsComponent of the GameEntity
     */
    public void updateTeamStats() {
        LASTeamStatsComponent teamStats = new LASTeamStatsComponent();
        Iterable<EntityRef> characters = entityManager.getEntitiesWith(PlayerCharacterComponent.class, LASTeamComponent.class);
        for (EntityRef character : characters) {
            if (character.getComponent(LASTeamComponent.class).team.equals(LASUtils.BLACK_TEAM)) {
                teamStats.blackTeamSize++;
            } else if (character.getComponent(LASTeamComponent.class).team.equals(LASUtils.RED_TEAM)) {
                teamStats.redTeamSize++;
            } else {
                logger.debug("Found character {} with white team", character.getId());
                teamStats.whiteTeamSize++;
            }
        }

        gameEntity.addOrSaveComponent(teamStats);
    }

    @Command(shortDescription = "Set the maximum team size difference", helpText = "Set maxTeamSizeDifference", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String setMaxTeamSizeDifference(@Sender EntityRef client, @CommandParam("difference") int difference) {
        LASConfigComponent lasconfig = gameEntity.getComponent(LASConfigComponent.class);
        lasconfig.maxTeamSizeDifference = difference;
        gameEntity.saveComponent(lasconfig);
        return "The max team size difference is set to " + difference;
    }

    @Command(shortDescription = "Set the minimum team size", helpText = "Set minTeamSize", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String setMinTeamSize(@Sender EntityRef client, @CommandParam("size") int size) {
        LASConfigComponent lasconfig = gameEntity.getComponent(LASConfigComponent.class);
        lasconfig.minTeamSize = size;
        gameEntity.saveComponent(lasconfig);
        return "The min team size is set to " + size;
    }
}

