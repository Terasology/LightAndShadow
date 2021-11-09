// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadow.systems;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.lightandshadowresources.components.FlagComponent;
import org.terasology.lightandshadow.events.ReturnFlagEvent;
import org.terasology.module.inventory.systems.InventoryManager;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.block.items.BlockItemComponent;
import org.terasology.lightandshadow.LASUtils;
import org.terasology.lightandshadow.components.HasFlagComponent;
import org.terasology.lightandshadow.events.GameOverEvent;
import org.terasology.lightandshadow.events.RestartRequestEvent;
import org.terasology.lightandshadow.events.ScoreUpdateFromServerEvent;
import org.terasology.lightandshadowresources.components.LASTeamComponent;
import org.terasology.lightandshadowresources.components.WinConditionCheckOnActivateComponent;

/**
 * System responsible for calculating and providing score.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class ScoreSystem extends BaseComponentSystem {

    @In
    private InventoryManager inventoryManager;
    @In
    private EntityManager entityManager;

    private int redScore;
    private int blackScore;

    /**
     * Updates score when player tries to place the flag at their home base.
     *
     * @param event
     * @param entity
     */
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

    /**
     * When a player places the opponent's flag at their home base, the player's team score
     * increases and the flag returns back to its team's base.
     * After the winning team reaches the goal score the game restarts.
     *
     * @param event
     * @param entity
     */
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
                    sendEventToClients(new GameOverEvent(LASUtils.RED_TEAM, blackScore, redScore));
                }
                if (blackScore >= LASUtils.GOAL_SCORE) {
                    resetLevel();
                    sendEventToClients(new GameOverEvent(LASUtils.BLACK_TEAM, blackScore, redScore));
                }
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

    /**
     * Retrieves the flags from the players and places them back at the bases.
     * It is used once the game is over.
     */
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
