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
import org.terasology.engine.logic.players.event.LocalPlayerInitializedEvent;
import org.terasology.engine.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.layers.ingame.DeathScreen;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.module.lightandshadow.LASUtils;
import org.terasology.module.lightandshadow.TapButton;
import org.terasology.module.lightandshadow.components.LASConfigComponent;
import org.terasology.module.lightandshadow.events.ScoreUpdateFromServerEvent;
import org.terasology.module.lightandshadow.events.TimerEvent;
import org.terasology.module.lightandshadow.phases.OnPreGamePhaseStartedEvent;
import org.terasology.module.lightandshadow.phases.OnPreGamePhaseEndedEvent;
import org.terasology.module.lightandshadow.phases.Phase;
import org.terasology.module.lightandshadow.phases.authority.PhaseSystem;
import org.terasology.notifications.events.ExpireNotificationEvent;
import org.terasology.notifications.events.ShowNotificationEvent;
import org.terasology.notifications.model.Notification;
import org.terasology.notify.ui.DialogNotificationOverlay;

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
    @In
    private ClientGameOverSystem clientGameOverSystem;

    private DeathScreen statisticsScreen;
    private int redScore = 0;
    private int blackScore = 0;
    private boolean statsScreenIsOpen;
    private boolean isExpired;

    private DialogNotificationOverlay window;

    @Override
    public void initialise() {
        window = nuiManager.addOverlay(ASSET_URI, DialogNotificationOverlay.class);
        statisticsScreen = nuiManager.createScreen("LightAndShadow:statisticsScreen", DeathScreen.class);
        clientGameOverSystem.addGoalScore(statisticsScreen, "spadesGoalScore");
        clientGameOverSystem.addGoalScore(statisticsScreen, "heartsGoalScore");
        clientGameOverSystem.addTeamScore(statisticsScreen, "heartsTeamScore", redScore);
        clientGameOverSystem.addTeamScore(statisticsScreen, "spadesTeamScore", blackScore);
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
                clientGameOverSystem.addTeamScore(statisticsScreen, "spadesTeamScore", blackScore);
                clientGameOverSystem.addTeamScore(statisticsScreen, "heartsTeamScore", redScore);
                clientGameOverSystem.addPlayerStatisticsInfo(statisticsScreen);
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

    @ReceiveEvent
    public void updateTimer(TimerEvent event, EntityRef entity) {
        if (localPlayer.getClientEntity().equals(entity)) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                int timePeriod = COUNTDOWN_IN_SECONDS;
                boolean addNotification;
                public void run() {
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
}
