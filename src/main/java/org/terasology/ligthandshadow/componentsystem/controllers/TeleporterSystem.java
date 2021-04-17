// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.controllers;

import java.util.Random;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterTeleportEvent;
import org.terasology.engine.logic.chat.ChatMessageEvent;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.players.PlayerCharacterComponent;
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

    private Random random = new Random();

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
        if (properTeamSize(entity, player)) {
            String team = setPlayerTeamToTeleporterTeam(player, entity);
            handlePlayerTeleport(player, team);
        }
    }

    private boolean properTeamSize(EntityRef teleporter, EntityRef player) {
        int maxTeamSizeDifference = 3;
        int oppositeTeamCount = 0;
        int teleporterTeamCount = 0;
        String teleporterTeam = teleporter.getComponent(LASTeamComponent.class).team;
        Iterable<EntityRef> characters = entityManager.getEntitiesWith(PlayerCharacterComponent.class,
                LASTeamComponent.class);

        for (EntityRef character : characters) {
            String otherPlayerTeam = character.getComponent(LASTeamComponent.class).team;
            if (teleporterTeam.equals(otherPlayerTeam)) {
                teleporterTeamCount++;
            } else if (!otherPlayerTeam.equals(LASUtils.WHITE_TEAM)) {
                oppositeTeamCount++;
            }
        }
        if (teleporterTeamCount - oppositeTeamCount < maxTeamSizeDifference) {
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
