// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.controllers;

import java.util.Optional;
import java.util.Random;

import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.terasology.engine.logic.players.PlayerCharacterComponent;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.utilities.Assets;
import org.terasology.ligthandshadow.componentsystem.components.LASConfigComponent;
import org.terasology.ligthandshadow.componentsystem.events.PregameEvent;
import org.terasology.ligthandshadow.componentsystem.events.TimerEvent;
import org.terasology.module.inventory.components.StartingInventoryComponent;
import org.terasology.module.inventory.events.RequestInventoryEvent;
import org.terasology.module.inventory.systems.InventoryManager;
import org.terasology.engine.registry.In;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.lightandshadowresources.components.LASTeamComponent;
import org.terasology.lightandshadowresources.components.SetTeamOnActivateComponent;

/**
 * Teleports players to play arena once they chose their team.
 * It also sends events to change players skins and hud based on team they have chosen.
 *
 * @see ClientSkinSystem
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class TeleporterSystem extends BaseComponentSystem {
    @In
    EntityManager entityManager;
    @In
    GameEntitySystem gameEntitySystem;

    private boolean gameStart;

    Optional<Prefab> prefab = Assets.getPrefab("inventory");
    StartingInventoryComponent startingInventory = prefab.get().getComponent(StartingInventoryComponent.class);

    private final Random random = new Random();

    @Command(shortDescription = "Set the maximum team size difference", helpText = "Set maxTeamSizeDifference", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String setMaxTeamSizeDifference(@Sender EntityRef client, @CommandParam("difference") int difference) {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        LASConfigComponent lasconfig = gameEntity.getComponent(LASConfigComponent.class);
        lasconfig.maxTeamSizeDifference = difference;
        gameEntity.saveComponent(lasconfig);
        return "The max team size difference is set to " + difference;
    }

    /**
     * Depending on which teleporter the player chooses, they are set to that team
     * and teleported to that base
     *
     * @param event
     * @param entity
     */
    @ReceiveEvent(components = SetTeamOnActivateComponent.class)
    public void onActivate(ActivateEvent event, EntityRef entity) {
        EntityRef player = event.getInstigator();
        if (isProperTeamSize(entity, player)) {
            String team = setPlayerTeamToTeleporterTeam(player, entity);
            handlePlayerTeleport(player, team);
        }
    }

    private boolean isProperTeamSize(EntityRef teleporter, EntityRef player) {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        int oppositeTeamCount = 0;
        int teleporterTeamCount = 0;
        int maxTeamSizeDifference = gameEntity.getComponent(LASConfigComponent.class).maxTeamSizeDifference;
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
            if (teleporterTeamCount >= 0 && oppositeTeamCount >= 1 && !gameStart) {
                sendTimerEventToClients();
                gameStart = true;
            }
            return true;
        } else {
            if (maxTeamSizeDifference == 1) {
                player.getOwner().send(new ChatMessageEvent("The " + teleporterTeam + " team has " + maxTeamSizeDifference + " player more, so please join the "
                        + LASUtils.getOppositionTeam(teleporterTeam) + " team.", EntityRef.NULL));
            } else {
                player.getOwner().send(new ChatMessageEvent("The " + teleporterTeam + " team has " + maxTeamSizeDifference + " players more, so please join the "
                        + LASUtils.getOppositionTeam(teleporterTeam) + " team.", EntityRef.NULL));
            }
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
        player.send(new PregameEvent());
        player.send(new CharacterTeleportEvent(randomVector.add(LASUtils.getTeleportDestination(team))));
        player.addOrSaveComponent(startingInventory);
        player.send(new RequestInventoryEvent(startingInventory.items));
    }

    private void sendTimerEventToClients() {
        if (entityManager.getCountOfEntitiesWith(ClientComponent.class) != 0) {
            Iterable<EntityRef> clients = entityManager.getEntitiesWith(ClientComponent.class);
            for (EntityRef client : clients) {
                client.send(new TimerEvent());
            }
        }
    }
}
