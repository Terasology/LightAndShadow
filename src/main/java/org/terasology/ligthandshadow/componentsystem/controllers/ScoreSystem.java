// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.controllers;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.lightandshadowresources.components.FlagComponent;
import org.terasology.ligthandshadow.componentsystem.events.ReturnFlagEvent;
import org.terasology.module.inventory.systems.InventoryManager;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.block.items.BlockItemComponent;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.HasFlagComponent;
import org.terasology.ligthandshadow.componentsystem.events.GameOverEvent;
import org.terasology.ligthandshadow.componentsystem.events.RestartRequestEvent;
import org.terasology.ligthandshadow.componentsystem.events.ScoreUpdateFromServerEvent;
import org.terasology.lightandshadowresources.components.LASTeamComponent;
import org.terasology.lightandshadowresources.components.WinConditionCheckOnActivateComponent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class ScoreSystem extends BaseComponentSystem {

    @In
    private InventoryManager inventoryManager;
    @In
    private EntityManager entityManager;

    private int redScore;
    private int blackScore;

    @ReceiveEvent(components = {WinConditionCheckOnActivateComponent.class, LASTeamComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        checkAndResetGameOnScore(event, entity);
    }

    @ReceiveEvent
    public void onRestartRequest(RestartRequestEvent event, EntityRef clientEntity, ClientComponent clientComponent) {
        redScore = 0;
        blackScore = 0;
        sendEventToClients(new ScoreUpdateFromServerEvent(LASUtils.RED_TEAM, redScore));
        sendEventToClients(new ScoreUpdateFromServerEvent(LASUtils.BLACK_TEAM, blackScore));
    }

    private void checkAndResetGameOnScore(ActivateEvent event, EntityRef entity) {
        LASTeamComponent baseTeamComponent = entity.getComponent(LASTeamComponent.class);
        EntityRef player = event.getInstigator();
        if (player.hasComponent(HasFlagComponent.class)) {
            String playerTeam = player.getComponent(LASTeamComponent.class).team;
            String oppositionTeam = LASUtils.getOppositionTeam(playerTeam);
            if (oppositionTeam == null) {
                return;
            }

            String flag = LASUtils.getFlagURI(oppositionTeam);
            EntityRef heldFlag = getHeldFlag(player, flag);
            if (heldFlag.equals(EntityRef.NULL)) {
                return;
            }

            if (checkIfTeamScores(baseTeamComponent, heldFlag)) {
                incrementScore(baseTeamComponent);
                player.send(new ReturnFlagEvent(heldFlag));
                if (redScore >= LASUtils.GOAL_SCORE) {
                    resetLevel();
                    sendGameOverEventToClients(LASUtils.RED_TEAM);
                }
                if (blackScore >= LASUtils.GOAL_SCORE) {
                    resetLevel();
                    sendGameOverEventToClients(LASUtils.BLACK_TEAM);
                }
            }
        }
    }

    private void sendGameOverEventToClients(String winningTeam) {
        if (entityManager.getCountOfEntitiesWith(ClientComponent.class) != 0) {
            Iterable<EntityRef> clients = entityManager.getEntitiesWith(ClientComponent.class);
            for (EntityRef client : clients) {
                client.send(new GameOverEvent(winningTeam, blackScore, redScore));
            }
        }
    }


    private EntityRef getHeldFlag(EntityRef player, String flag) {
        int inventorySize = inventoryManager.getNumSlots(player);
        for (int slotNumber = 0; slotNumber <= inventorySize; slotNumber++) {
            EntityRef inventorySlot = inventoryManager.getItemInSlot(player, slotNumber);
            if (inventorySlot.hasComponent(BlockItemComponent.class)) {
                if (inventorySlot.getComponent(BlockItemComponent.class).blockFamily.getURI().toString().equals(flag)) {
                    return inventorySlot;
                }
            }
        }
        return EntityRef.NULL;
    }

    private boolean checkIfTeamScores(LASTeamComponent baseTeamComponent, EntityRef heldItem) {
        // Check to see if player has other team's flag
        return !baseTeamComponent.team.equals(heldItem.getComponent(FlagComponent.class).team);
    }

    private void incrementScore(LASTeamComponent baseTeamComponent) {
        if (baseTeamComponent.team.equals(LASUtils.RED_TEAM)) {
            redScore++;
            // Send event to clients to update their Score UI
            sendEventToClients(new ScoreUpdateFromServerEvent(LASUtils.RED_TEAM, redScore));
            return;
        }
        if (baseTeamComponent.team.equals(LASUtils.BLACK_TEAM)) {
            blackScore++;
            // Send event to clients to update their Score UI
            sendEventToClients(new ScoreUpdateFromServerEvent(LASUtils.BLACK_TEAM, blackScore));
            return;
        }
    }

    private void resetLevel() {
        Iterable<EntityRef> playersWithFlag = entityManager.getEntitiesWith(HasFlagComponent.class);
        for (EntityRef playerWithFlag : playersWithFlag) {
            String playerTeam = playerWithFlag.getComponent(LASTeamComponent.class).team;
            String oppositionTeam = LASUtils.getOppositionTeam(playerTeam);
            if (oppositionTeam == null) {
                continue;
            }

            String flag = LASUtils.getFlagURI(oppositionTeam);
            EntityRef heldFlag = getHeldFlag(playerWithFlag, flag);
            if (heldFlag.equals(EntityRef.NULL)) {
                continue;
            }

            playerWithFlag.send(new ReturnFlagEvent(heldFlag));
        }
    }

    private void sendEventToClients(Event event) {
        if (entityManager.getCountOfEntitiesWith(ClientComponent.class) != 0) {
            Iterable<EntityRef> clients = entityManager.getEntitiesWith(ClientComponent.class);
            for (EntityRef client : clients) {
                client.send(event);
            }
        }
    }
}
