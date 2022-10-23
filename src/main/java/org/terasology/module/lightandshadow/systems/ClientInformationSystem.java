// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.lightandshadow.systems;

import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.Priority;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.input.InputSystem;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.logic.players.PlayerCharacterComponent;
import org.terasology.engine.logic.players.PlayerUtil;
import org.terasology.engine.logic.players.event.LocalPlayerInitializedEvent;
import org.terasology.engine.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.layers.ingame.DeathScreen;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.lightandshadowresources.components.LASTeamComponent;
import org.terasology.module.lightandshadow.LASUtils;
import org.terasology.module.lightandshadow.TapButton;
import org.terasology.module.lightandshadow.components.LASConfigComponent;
import org.terasology.module.lightandshadow.components.PlayerStatisticsComponent;
import org.terasology.module.lightandshadow.events.GameOverEvent;
import org.terasology.module.lightandshadow.events.RestartRequestEvent;
import org.terasology.module.lightandshadow.events.ScoreUpdateFromServerEvent;
import org.terasology.module.lightandshadow.events.TimerEvent;
import org.terasology.module.lightandshadow.phases.OnPreGamePhaseStartedEvent;
import org.terasology.module.lightandshadow.phases.OnPreGamePhaseEndedEvent;
import org.terasology.module.lightandshadow.phases.Phase;
import org.terasology.module.lightandshadow.phases.SwitchToPhaseEvent;
import org.terasology.module.lightandshadow.phases.authority.PhaseSystem;
import org.terasology.notifications.events.ExpireNotificationEvent;
import org.terasology.notifications.events.ShowNotificationEvent;
import org.terasology.notifications.model.Notification;
import org.terasology.notify.ui.DialogNotificationOverlay;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.layouts.miglayout.MigLayout;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UILabel;

import java.util.Timer;
import java.util.TimerTask;

@RegisterSystem(RegisterMode.CLIENT)
public class ClientInformationSystem extends BaseComponentSystem {
    private static final String WAIT_NOTIFICATION_ID = "LightAndShadow:waitForPlayers";
    private static final String STATS_NOTIFICATION_ID = "LightAndShadow:firstTimeStatistics";

    public static final ResourceUrn ASSET_URI = new ResourceUrn("LightAndShadow:Timer");

    private static final int COUNTDOWN_IN_SECONDS = 30;

    private static Timer timer;

    @In
    InputSystem inputSystem;

    @In
    private EntityManager entityManager;
    @In
    private NUIManager nuiManager;
    @In
    private GameEntitySystem gameEntitySystem;
    @In
    private PhaseSystem phaseSystem;

    @In
    private LocalPlayer localPlayer;

    private DeathScreen statisticsScreen;
    private DeathScreen deathScreen;
    private int redScore = 0;
    private int blackScore = 0;
    private boolean statsScreenIsOpen;
    private boolean isExpired;

    private DialogNotificationOverlay window;

    @Override
    public void initialise() {
        window = nuiManager.addOverlay(ASSET_URI, DialogNotificationOverlay.class);
        deathScreen = nuiManager.createScreen(LASUtils.DEATH_SCREEN, DeathScreen.class);
        statisticsScreen = nuiManager.createScreen("LightAndShadow:statisticsScreen", DeathScreen.class);
        addGoalScore(statisticsScreen, "spadesGoalScore");
        addGoalScore(statisticsScreen, "heartsGoalScore");
        addTeamScore(statisticsScreen, "heartsTeamScore", redScore);
        addTeamScore(statisticsScreen, "spadesTeamScore", blackScore);
    }

    @Override
    public void shutdown() {
        nuiManager.closeScreen(window);
    }

    @ReceiveEvent
    public void onPregameStart(OnPreGamePhaseStartedEvent event, EntityRef entity) {
        notifyOnTooFewPlayers();
    }

    @ReceiveEvent
    public void onSpawnDuringPregame(OnPlayerSpawnedEvent event, EntityRef entity) {
        if (phaseSystem.getCurrentPhase() == Phase.PRE_GAME) {
            notifyOnTooFewPlayers();
        }
    }

    @ReceiveEvent
    public void onPregameEnd(OnPreGamePhaseEndedEvent event, EntityRef entity) {
        localPlayer.getClientEntity().send(new ExpireNotificationEvent(WAIT_NOTIFICATION_ID));
    }

    @ReceiveEvent
    public void onScoreUpdate(ScoreUpdateFromServerEvent event, EntityRef entity) {
        String team = event.team;
        if (team.equals(LASUtils.RED_TEAM)) {
            redScore = event.score;
        }
        if (team.equals(LASUtils.BLACK_TEAM)) {
            blackScore = event.score;
        }
    }

    @Priority(EventPriority.PRIORITY_CRITICAL)
    @ReceiveEvent
    public void onTab(TapButton event, EntityRef entity) {
        if (event.isDown()) {
            if (localPlayer.getClientEntity().equals(entity) && !statsScreenIsOpen) {
                if (!isExpired) {
                    entity.send(new ExpireNotificationEvent(STATS_NOTIFICATION_ID));
                    isExpired = true;
                }
                addTeamScore(statisticsScreen, "spadesTeamScore", blackScore);
                addTeamScore(statisticsScreen, "heartsTeamScore", redScore);
                addPlayerStatisticsInfo(statisticsScreen);
                nuiManager.pushScreen("LightAndShadow:statisticsScreen");
                statsScreenIsOpen = true;
                event.consume();
            }
        } else {
            statsScreenIsOpen = false;
            nuiManager.closeScreen("LightAndShadow:statisticsScreen");
        }
    }

    @ReceiveEvent
    public void onLocalPlayerInitialized(LocalPlayerInitializedEvent event, EntityRef entity) {
        Notification notification = new Notification(STATS_NOTIFICATION_ID,
                "The Numbers Game",
                "Hold " + LASUtils.getActivationKey(inputSystem, new SimpleUri("LightAndShadow:statistics")) + " to see statistics",
                "engine:items#blueBook");
        localPlayer.getClientEntity().send(new ShowNotificationEvent(notification));
    }

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
            nuiManager.pushScreen(deathScreen);
            addPlayerStatisticsInfo(deathScreen);
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
                            restartButton.setEnabled(true);
                            timer.cancel();
                        }
                    }
                }, 0, 1000);
                restartButton.setVisible(true);
                restartButton.setEnabled(false);
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

    @ReceiveEvent
    public void updateTimer(TimerEvent event, EntityRef entity) {
        if (localPlayer.getClientEntity().equals(entity)) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                int timePeriod = COUNTDOWN_IN_SECONDS;
                boolean addNotification;
                public void run() {
                    if (phaseSystem.getCurrentPhase() != Phase.COUNTDOWN) {
                        if (!addNotification) {
                            window.removeNotification("The game starts in " + (timePeriod + 1) + " seconds.");
                        }
                        timer.cancel();
                    }
                    if (addNotification) {
                        entity.send(new ExpireNotificationEvent(WAIT_NOTIFICATION_ID));
                        window.addNotification("The game starts in " + timePeriod-- + " seconds.");
                        addNotification = false;
                    } else {
                        entity.send(new ExpireNotificationEvent(WAIT_NOTIFICATION_ID));
                        window.removeNotification("The game starts in " + (timePeriod + 1) + " seconds.");
                        addNotification = true;
                    }
                    if (timePeriod < 0) {
                        window.removeNotification("The game starts in " + (timePeriod + 1) + " seconds.");
                        gameEntitySystem.getGameEntity().send(new SwitchToPhaseEvent(Phase.IN_GAME));
                        timer.cancel();
                    }
                }
            }, 0, 500);
        }
    }

    public void notifyOnTooFewPlayers() {
        Notification notification = new Notification(WAIT_NOTIFICATION_ID,
                "Too Few Players",
                "Each team needs at least " +
                        gameEntitySystem.getGameEntity().getComponent(LASConfigComponent.class).minTeamSize + " player(s)",
                "engine:icons#halfGreenHeart");
        localPlayer.getClientEntity().send(new ShowNotificationEvent(notification));
    }

    public void addPlayerStatisticsInfo(DeathScreen deathScreen) {
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
                if (!playerTeam.equals("white")) {
                    addInfoToTeamMigLayout(migLayout, clientComponent, playerStatisticsComponent);
                }
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

    public void addTeamScore(DeathScreen deathScreen, String teamUILabelId, int finalScore) {
        UILabel teamScore = deathScreen.find(teamUILabelId, UILabel.class);
        teamScore.setText(String.valueOf(finalScore));
    }

    public void addGoalScore(DeathScreen deathScreen, String teamUILabelID) {
        UILabel goalScore = deathScreen.find(teamUILabelID, UILabel.class);
        goalScore.setText(Integer.toString(LASUtils.GOAL_SCORE));
    }

    private void triggerRestart() {
        localPlayer.getClientEntity().send(new RestartRequestEvent());
    }
}
