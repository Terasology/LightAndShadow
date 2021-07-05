// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.controllers;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.logic.players.PlayerCharacterComponent;
import org.terasology.engine.logic.players.PlayerUtil;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.layers.ingame.DeathScreen;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.PlayerStatisticsComponent;
import org.terasology.ligthandshadow.componentsystem.events.GameOverEvent;
import org.terasology.ligthandshadow.componentsystem.events.RestartRequestEvent;
import org.terasology.lightandshadowresources.components.LASTeamComponent;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.layouts.miglayout.MigLayout;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UILabel;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = Executors.defaultThreadFactory().newThread(runnable);
        thread.setDaemon(true);
        return thread;
    });

    private static Timer timer;

    /**
     * System to show game over screen once a team achieves goal score.
     *
     * @param event the GameOverEvent event which stores the winning team and the final scores of both teams.
     * @param entity the entity about each player connected to the game. TODO: needs more details/clarification
     */
    @ReceiveEvent
    public void onGameOver(GameOverEvent event, EntityRef entity) {
        if (localPlayer.getClientEntity().equals(entity)) {
            nuiManager.removeOverlay(LASUtils.ONLINE_PLAYERS_OVERLAY);
            DeathScreen deathScreen = nuiManager.pushScreen(LASUtils.DEATH_SCREEN, DeathScreen.class);
            addPlayerStatisticsInfo(deathScreen, event);
            addFlagInfo(deathScreen, event);
            UILabel gameOverResult = deathScreen.find("gameOverResult", UILabel.class);

            UIButton restartButton = deathScreen.find("restart", UIButton.class);
            UILabel countDown = deathScreen.find("timer", UILabel.class);
            if (restartButton != null && countDown != null) {
                timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    int timePeriod = 10;
                    public void run() {
                        countDown.setText(Integer.toString(timePeriod--));
                        if (timePeriod < 0) {
                            countDown.setText(" ");
                            timer.cancel();
                        }
                    }
                }, 0, 1000);
                restartButton.setVisible(true);
                restartButton.setEnabled(false);
                executorService.schedule(() -> restartButton.setEnabled(true), 10, TimeUnit.SECONDS);
            }

            WidgetUtil.trySubscribe(deathScreen, "restart", widget -> triggerRestart());
            if (gameOverResult != null) {
                if (event.winningTeam.equals(localPlayer.getCharacterEntity().getComponent(LASTeamComponent.class).team)) {
                    gameOverResult.setText("Victory");
                    gameOverResult.setFamily("win");
                } else {
                    gameOverResult.setText("Defeat");
                    gameOverResult.setFamily("lose");
                }
            }
        }
    }

    private void addPlayerStatisticsInfo(DeathScreen deathScreen, GameOverEvent event) {
        MigLayout spadesTeamMigLayout = deathScreen.find("spadesTeamPlayerStatistics", MigLayout.class);
        MigLayout heartsTeamMigLayout = deathScreen.find("heartsTeamPlayerStatistics", MigLayout.class);
        spadesTeamMigLayout.removeAllWidgets();
        heartsTeamMigLayout.removeAllWidgets();
        if (spadesTeamMigLayout != null && heartsTeamMigLayout != null) {
            Iterable<EntityRef> characters = entityManager.getEntitiesWith(PlayerCharacterComponent.class,
                    LASTeamComponent.class);

            for (EntityRef character : characters) {
                EntityRef client = character.getOwner();
                ClientComponent clientComponent = client.getComponent(ClientComponent.class);
                String playerTeam = character.getComponent(LASTeamComponent.class).team;
                PlayerStatisticsComponent playerStatisticsComponent =
                        character.getComponent(PlayerStatisticsComponent.class);
                MigLayout migLayout = (playerTeam.equals("black") ? spadesTeamMigLayout : heartsTeamMigLayout);
                addInfoToTeamMigLayout(migLayout, clientComponent, playerStatisticsComponent);
            }
        }
    }

    private void addInfoToTeamMigLayout(MigLayout migLayout, ClientComponent clientComponent,
                                        PlayerStatisticsComponent playerStatisticsComponent) {
        migLayout.addWidget(new UILabel(PlayerUtil.getColoredPlayerName(clientComponent.clientInfo)),
                new MigLayout.CCHint());
        migLayout.addWidget(new UILabel(String.valueOf(playerStatisticsComponent.kills)), new MigLayout.CCHint());
        migLayout.addWidget(new UILabel(String.valueOf(playerStatisticsComponent.deaths)), new MigLayout.CCHint("wrap"
        ));
        playerStatisticsComponent.kills = 0;
        playerStatisticsComponent.deaths = 0;
    }

    private void addFlagInfo(DeathScreen deathScreen, GameOverEvent event) {
        addTeamScore(deathScreen, "spadesTeamScore", event.blackTeamScore);
        addTeamScore(deathScreen, "heartsTeamScore", event.redTeamScore);
        addGoalScore(deathScreen, "spadesGoalScore");
        addGoalScore(deathScreen, "heartsGoalScore");
    }

    private void addTeamScore(DeathScreen deathScreen, String teamUILabelId, int finalScore) {
        UILabel teamScore = deathScreen.find(teamUILabelId, UILabel.class);
        teamScore.setText(String.valueOf(finalScore));
    }

    private void addGoalScore(DeathScreen deathScreen, String teamUILabelID) {
        UILabel goalScore = deathScreen.find(teamUILabelID, UILabel.class);
        goalScore.setText(Integer.toString(LASUtils.GOAL_SCORE));
    }

    private void triggerRestart() {
        localPlayer.getClientEntity().send(new RestartRequestEvent());
    }
}
