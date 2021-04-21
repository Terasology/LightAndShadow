// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.controllers;

import java.util.Random;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterTeleportEvent;
import org.terasology.engine.logic.chat.ChatMessageEvent;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.engine.logic.console.commandSystem.annotations.Sender;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.utilities.Assets;
import org.terasology.ligthandshadow.componentsystem.components.MaxTeamSizeDifferenceComponent;
import org.terasology.ligthandshadow.componentsystem.components.TeamCountComponent;
import org.terasology.module.inventory.systems.InventoryManager;
import org.terasology.engine.registry.In;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.LASTeamComponent;
import org.terasology.ligthandshadow.componentsystem.components.SetTeamOnActivateComponent;

/**
 * Teleports players to play arena once they chose their team.
 * It also sends events to change players skins and hud based on team they have chosen.
 *
 * @see ClientSkinSystem
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class TeleporterSystem extends BaseComponentSystem {
    @In
    InventoryManager inventoryManager;
    @In
    EntityManager entityManager;

    private final Random random = new Random();

    @Command(shortDescription = "Set the maximum team size difference", helpText = "Set maxTeamSizeDifference", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String setMaxTeamSizeDifference(@Sender EntityRef client, @CommandParam("difference") int difference) {
        Prefab gameplayPrefab = Assets.getPrefab("gameplayConfig").get();
        gameplayPrefab.getComponent(MaxTeamSizeDifferenceComponent.class).maxTeamSizeDifference = difference;
        return "The max team size difference is set to " + difference;
    }

    /**
     * Depending on which teleporter the player chooses, they are set to that team
     * and teleported to that base
     *
     * @param event
     * @param entity
     */
    @ReceiveEvent(components = {SetTeamOnActivateComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        EntityRef player = event.getInstigator();
        if (isProperTeamSize(entity, player)) {
            String team = setPlayerTeamToTeleporterTeam(player, entity);
            handlePlayerTeleport(player, team);
        }
    }

    private boolean isProperTeamSize(EntityRef teleporter, EntityRef player) {
        Prefab gameplayPrefab = Assets.getPrefab("gameplayConfig").get();
        Prefab gameStatePrefab = Assets.getPrefab("gameState").get();
        int maxTeamSizeDifference = gameplayPrefab.getComponent(MaxTeamSizeDifferenceComponent.class).maxTeamSizeDifference;
        int redTeamCount = gameStatePrefab.getComponent(TeamCountComponent.class).redTeamCount;
        int blackTeamCount = gameStatePrefab.getComponent(TeamCountComponent.class).blackTeamCount;
        String teleporterTeam = teleporter.getComponent(LASTeamComponent.class).team;
        int teleporterTeamCount = teleporterTeam.equals(LASUtils.RED_TEAM) ? redTeamCount : blackTeamCount;
        int oppositeTeamCount = teleporterTeam.equals(LASUtils.RED_TEAM) ? blackTeamCount : redTeamCount;
        if (teleporterTeamCount - oppositeTeamCount < maxTeamSizeDifference) {
            if (teleporterTeam.equals(LASUtils.RED_TEAM)) {
                gameStatePrefab.getComponent(TeamCountComponent.class).redTeamCount++;
            } else {
                gameStatePrefab.getComponent(TeamCountComponent.class).blackTeamCount++;
            }
            return true;
        } else {
            player.getOwner().send(new ChatMessageEvent("The " + teleporterTeam + " team has more players so please join the " + LASUtils.getOppositionTeam(teleporterTeam)
                    + " team.", EntityRef.NULL));
            return false;
        }
    }

    private String setPlayerTeamToTeleporterTeam(EntityRef player, EntityRef teleporter) {
        LASTeamComponent teleporterTeamComponent = teleporter.getComponent(LASTeamComponent.class);
        LASTeamComponent playerTeamComponent = player.getComponent(LASTeamComponent.class);
        playerTeamComponent.team = teleporterTeamComponent.team;
        player.saveComponent(playerTeamComponent);
        return playerTeamComponent.team;
    }

    private void handlePlayerTeleport(EntityRef player, String team) {
        Vector3f randomVector = new Vector3f(-1 + random.nextInt(3), 0, -1 + random.nextInt(3));
        player.send(new CharacterTeleportEvent(randomVector.add(LASUtils.getTeleportDestination(team))));
        inventoryManager.giveItem(player, EntityRef.NULL, entityManager.create(LASUtils.MAGIC_STAFF_URI));
    }
}
