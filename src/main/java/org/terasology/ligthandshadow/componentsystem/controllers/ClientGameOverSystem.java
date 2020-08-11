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
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.LASTeamComponent;
import org.terasology.ligthandshadow.componentsystem.components.PlayerStatisticsComponent;
import org.terasology.ligthandshadow.componentsystem.events.GameOverEvent;
import org.terasology.ligthandshadow.componentsystem.events.RestartRequestEvent;
import org.terasology.logic.characters.AliveCharacterComponent;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.PlayerCharacterComponent;
import org.terasology.logic.players.PlayerUtil;
import org.terasology.network.ClientComponent;
import org.terasology.protobuf.EntityData;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.layers.ingame.DeathScreen;
import org.terasology.rendering.nui.layouts.miglayout.MigLayout;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILabel;

/**
 * Displays game over screen for all clients.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class ClientGameOverSystem extends BaseComponentSystem {
    @In
    private NUIManager nuiManager;
    @In
    private LocalPlayer localPlayer;
    @In
    private EntityManager entityManager;
    @In
    private PermissionManager permissionManager;

    /**
     * System to show game over screen once a team achieves goal score.
     *
     * @param event  the event
     * @param entity the entity
     */
    @ReceiveEvent
    public void onGameOver(GameOverEvent event, EntityRef entity) {
        if (localPlayer.getClientEntity().equals(entity)) {
            nuiManager.removeOverlay(LASUtils.ONLINE_PLAYERS_OVERLAY);
            DeathScreen deathScreen = nuiManager.pushScreen(LASUtils.DEATH_SCREEN, DeathScreen.class);
            addPlayerStatisticsInfo(deathScreen, event);
            UILabel gameOverDetails = deathScreen.find("gameOverDetails", UILabel.class);

            if (event.hasRestartPermission) {
                UIButton restartButton = deathScreen.find("restart", UIButton.class);
                if (restartButton != null) {
                    restartButton.setVisible(true);
                }
            }

            WidgetUtil.trySubscribe(deathScreen, "restart", widget -> triggerRestart());
            if (gameOverDetails != null) {
                if (event.winningTeam.equals(localPlayer.getCharacterEntity().getComponent(LASTeamComponent.class).team)) {
                    gameOverDetails.setText("You Win!");
                } else {
                    gameOverDetails.setText("You Lose!");
                }
            }
        }
    }

    private void addPlayerStatisticsInfo(DeathScreen deathScreen, GameOverEvent event) {
        MigLayout spadesTeamMigLayout = deathScreen.find("spadesTeamPlayerStatistics", MigLayout.class);
        MigLayout heartsTeamMigLayout = deathScreen.find("heartsTeamPlayerStatistics", MigLayout.class);
        if (spadesTeamMigLayout != null && heartsTeamMigLayout != null) {
            Iterable<EntityRef> characters = entityManager.getEntitiesWith(PlayerCharacterComponent.class, LASTeamComponent.class);

            for (EntityRef character : characters) {
                EntityRef client = character.getOwner();
                ClientComponent clientComponent = client.getComponent(ClientComponent.class);
                String playerTeam = character.getComponent(LASTeamComponent.class).team;
                PlayerStatisticsComponent playerStatisticsComponent = character.getComponent(PlayerStatisticsComponent.class);
                MigLayout migLayout = (playerTeam.equals("black") ? spadesTeamMigLayout : heartsTeamMigLayout);
                addInfoToTeamMigLayout(migLayout,clientComponent,playerStatisticsComponent);
            }
        }
        addTeamScore(deathScreen, event);
    }

    private void addInfoToTeamMigLayout(MigLayout migLayout, ClientComponent clientComponent,PlayerStatisticsComponent playerStatisticsComponent){
        migLayout.addWidget(new UILabel(PlayerUtil.getColoredPlayerName(clientComponent.clientInfo)), new MigLayout.CCHint());
        migLayout.addWidget(new UILabel(String.valueOf(playerStatisticsComponent.kills)), new MigLayout.CCHint());
        migLayout.addWidget(new UILabel(String.valueOf(playerStatisticsComponent.deaths)), new MigLayout.CCHint("wrap"));
    }

    private void addTeamScore(DeathScreen deathScreen, GameOverEvent event){
        UILabel spadesTeamScore = deathScreen.find("spadesTeamScore", UILabel.class);
        UILabel heartsTeamScore = deathScreen.find("heartsTeamScore", UILabel.class);
        String blackScore = String.valueOf(event.blackTeamScore);
        String redScore = String.valueOf(event.redTeamScore);
        spadesTeamScore.setText(blackScore);
        heartsTeamScore.setText(redScore);
    }

    private void triggerRestart() {
        localPlayer.getClientEntity().send(new RestartRequestEvent());
    }
}
