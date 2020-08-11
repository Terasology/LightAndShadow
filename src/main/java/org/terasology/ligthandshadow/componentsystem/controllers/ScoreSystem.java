/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.ligthandshadow.componentsystem.controllers;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.BlackFlagComponent;
import org.terasology.ligthandshadow.componentsystem.components.HasFlagComponent;
import org.terasology.ligthandshadow.componentsystem.components.LASTeamComponent;
import org.terasology.ligthandshadow.componentsystem.components.RedFlagComponent;
import org.terasology.ligthandshadow.componentsystem.components.WinConditionCheckOnActivateComponent;
import org.terasology.ligthandshadow.componentsystem.events.GameOverEvent;
import org.terasology.ligthandshadow.componentsystem.events.RestartRequestEvent;
import org.terasology.ligthandshadow.componentsystem.events.ScoreUpdateFromServerEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.nui.ControlWidget;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.BlockItemComponent;

@RegisterSystem(RegisterMode.AUTHORITY)
@Share(ScoreSystem.class)
public class ScoreSystem extends BaseComponentSystem {

    @In
    private InventoryManager inventoryManager;
    @In
    private NUIManager nuiManager;
    @In
    private EntityManager entityManager;
    @In
    private BlockManager blockManager;
    @In
    private WorldProvider worldProvider;
    @In
    private LocalPlayer localPlayer;
    @In
    private PermissionManager permissionManager;

    private int redScore;
    private int blackScore;

    @Override
    public void postBegin() {
        // Sets score screen bindings
        ControlWidget scoreScreen = nuiManager.getHUD().getHUDElement("LightAndShadow:ScoreHud");
        UILabel blackScoreArea = scoreScreen.find("blackScoreArea", UILabel.class);
        blackScoreArea.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                return String.valueOf(blackScore);
            }
        });
        UILabel redScoreArea = scoreScreen.find("redScoreArea", UILabel.class);
        redScoreArea.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                return String.valueOf(redScore);
            }
        });
    }

    @Override
    public void initialise() {
        // Displays score UI on game start
        nuiManager.getHUD().addHUDElement("ScoreHud");
    }

    @ReceiveEvent(components = {WinConditionCheckOnActivateComponent.class, LASTeamComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        checkAndResetGameOnScore(event, entity);
    }

    @ReceiveEvent
    public void onRestartRequest(RestartRequestEvent event, EntityRef clientEntity, ClientComponent clientComponent) {
        if (permissionManager.hasPermission(clientComponent.clientInfo, LASUtils.RESTART_PERMISSION)) {
            redScore = 0;
            blackScore = 0;
            sendEventToClients(new ScoreUpdateFromServerEvent(LASUtils.RED_TEAM, redScore));
            sendEventToClients(new ScoreUpdateFromServerEvent(LASUtils.BLACK_TEAM, blackScore));
        }
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
                movePlayerFlagToBase(player, oppositionTeam, heldFlag);
                if (redScore >= LASUtils.GOAL_SCORE) {
                    sendGameOverEventToClients(LASUtils.RED_TEAM);
                    resetLevel();
                }
                if (blackScore >= LASUtils.GOAL_SCORE) {
                    sendGameOverEventToClients(LASUtils.BLACK_TEAM);
                    resetLevel();
                }
            }
        }
    }

    private void sendGameOverEventToClients(String winningTeam) {
        if (entityManager.getCountOfEntitiesWith(ClientComponent.class) != 0) {
            Iterable<EntityRef> clients = entityManager.getEntitiesWith(ClientComponent.class);
            for (EntityRef client : clients) {
                EntityRef clientInfo = client.getComponent(ClientComponent.class).clientInfo;
                Boolean hasRestartPermission = permissionManager.hasPermission(clientInfo, LASUtils.RESTART_PERMISSION);
                client.send(new GameOverEvent(winningTeam, hasRestartPermission, blackScore, redScore));
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
        if (baseTeamComponent.team.equals(LASUtils.RED_TEAM) && heldItem.hasComponent(BlackFlagComponent.class)) {
            return true;
        }
        if (baseTeamComponent.team.equals(LASUtils.BLACK_TEAM) && heldItem.hasComponent(RedFlagComponent.class)) {
            return true;
        }
        return false;
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

            movePlayerFlagToBase(playerWithFlag, oppositionTeam, heldFlag);
        }
    }

    private void movePlayerFlagToBase(EntityRef player, String oppositionTeam, EntityRef heldFlag) {
        Vector3i basePosition = LASUtils.getFlagLocation(oppositionTeam);
        String flag = LASUtils.getFlagURI(oppositionTeam);
        inventoryManager.removeItem(player, player, heldFlag, true);
        worldProvider.setBlock(basePosition, blockManager.getBlock(flag));
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
