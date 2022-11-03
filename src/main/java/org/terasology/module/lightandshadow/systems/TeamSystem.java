// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.lightandshadow.systems;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.lightandshadowresources.components.LASTeamComponent;
import org.terasology.module.lightandshadow.LASUtils;
import org.terasology.module.lightandshadow.components.LASConfigComponent;
import org.terasology.module.lightandshadow.components.LASTeamStatsComponent;

@RegisterSystem(RegisterMode.AUTHORITY)
@Share(value = TeamSystem.class)
public class TeamSystem extends BaseComponentSystem {
    @In
    GameEntitySystem gameEntitySystem;

    private static final Logger logger = LoggerFactory.getLogger(TeamSystem.class);

    @ReceiveEvent(components = LASTeamComponent.class)
    public void onTeamChange(OnChangedComponent event, EntityRef entity) {
        logger.debug("Player {} switched to team {}",
                entity.getOwner().getComponent(ClientComponent.class).clientInfo,
                entity.getComponent(LASTeamComponent.class).team);
    }

    public boolean isBalancedTeams(String targetTeam) {
        gameEntitySystem.updateTeamStats();
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        LASTeamStatsComponent teamStats = gameEntity.getComponent(LASTeamStatsComponent.class);
        int maxTeamSizeDifference = gameEntity.getComponent(LASConfigComponent.class).maxTeamSizeDifference;
        int currentTeamSizeDiff = teamStats.blackTeamSize - teamStats.redTeamSize;
        int postTeleportTeamSizeDiff = targetTeam.equals(LASUtils.BLACK_TEAM) ? currentTeamSizeDiff + 1 : currentTeamSizeDiff - 1;

        return Math.abs(postTeleportTeamSizeDiff) <= maxTeamSizeDifference;
    }

    public boolean isMinSizeTeams() {
        gameEntitySystem.updateTeamStats();
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        LASTeamStatsComponent teamStats = gameEntity.getComponent(LASTeamStatsComponent.class);
        int minTeamSize = gameEntity.getComponent(LASConfigComponent.class).minTeamSize;

        return teamStats.redTeamSize >= minTeamSize && teamStats.blackTeamSize >= minTeamSize;
    }

    public boolean setPlayerTeamToTeam(EntityRef player, String team) {
        if (!isBalancedTeams(team)) {
            return false;
        }
        LASTeamComponent playerTeamComponent = new LASTeamComponent(team);
        player.addOrSaveComponent(playerTeamComponent);
        return true;
    }

    public int getTeamSize(String team) {
        gameEntitySystem.updateTeamStats();
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        LASTeamStatsComponent teamStats = gameEntity.getComponent(LASTeamStatsComponent.class);
        switch (team) {
            case LASUtils.BLACK_TEAM:
                return teamStats.blackTeamSize;
            case LASUtils.RED_TEAM:
                return teamStats.redTeamSize;
            case LASUtils.WHITE_TEAM:
                return teamStats.whiteTeamSize;
        }
        logger.error("Was requested to get team size for non-existant team {}", team);
        return 0;
    }

}
