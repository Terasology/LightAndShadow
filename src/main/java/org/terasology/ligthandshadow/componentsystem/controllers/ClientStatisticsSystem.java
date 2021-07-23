// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.controllers;

import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.input.InputSystem;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.logic.players.event.LocalPlayerInitializedEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.layers.ingame.DeathScreen;
import org.terasology.engine.unicode.EnclosedAlphanumerics;
import org.terasology.input.ButtonState;
import org.terasology.input.Input;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.events.ScoreUpdateFromServerEvent;
import org.terasology.TabButton;
import org.terasology.notifications.events.ExpireNotificationEvent;
import org.terasology.notifications.events.ShowNotificationEvent;
import org.terasology.notifications.model.Notification;
import org.terasology.nui.Color;
import org.terasology.nui.FontColor;

/**
 * Displays statistics screen when required.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class ClientStatisticsSystem extends BaseComponentSystem {

    private DeathScreen statisticsScreen;

    @In
    private NUIManager nuiManager;
    @In
    private LocalPlayer localPlayer;
    @In
    private ClientGameOverSystem clientGameOverSystem;
    @In
    InputSystem inputSystem;

    private static final String NOTIFICATION_ID = "LightAndShadow:firstTime";
    private int redScore = 0;
    private int blackScore = 0;

    @Override
    public void initialise() {
        statisticsScreen = nuiManager.createScreen("LightAndShadow:statisticsScreen", DeathScreen.class);
        clientGameOverSystem.addGoalScore(statisticsScreen, "spadesGoalScore");
        clientGameOverSystem.addGoalScore(statisticsScreen, "heartsGoalScore");
        clientGameOverSystem.addTeamScore(statisticsScreen, "heartsTeamScore", redScore);
        clientGameOverSystem.addTeamScore(statisticsScreen, "spadesTeamScore", blackScore);

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

    @ReceiveEvent(priority = EventPriority.PRIORITY_CRITICAL)
    public void onTab(TabButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            if (localPlayer.getClientEntity().equals(entity)) {
                entity.send(new ExpireNotificationEvent(NOTIFICATION_ID));
                clientGameOverSystem.addTeamScore(statisticsScreen, "spadesTeamScore", blackScore);
                clientGameOverSystem.addTeamScore(statisticsScreen, "heartsTeamScore", redScore);
                clientGameOverSystem.addPlayerStatisticsInfo(statisticsScreen);
                nuiManager.toggleScreen("LightAndShadow:statisticsScreen");
                event.consume();
            }
        }
    }

    private String getActivationKey(SimpleUri button) {
        return inputSystem.getInputsForBindButton(button).stream()
                .findFirst()
                .map(Input::getDisplayName)
                .map(key -> {
                    if (key.length() == 1) {
                        // print the key in yellow within a circle
                        int off = key.charAt(0) - 'A';
                        char code = (char) (EnclosedAlphanumerics.CIRCLED_LATIN_CAPITAL_LETTER_A + off);
                        return String.valueOf(code);
                    } else {
                        return key;
                    }
                })
                .map(key -> FontColor.getColored(key, Color.yellow))
                .orElse(FontColor.getColored("n/a", Color.red));
    }

    @ReceiveEvent
    public void onLocalPlayerInitialized(LocalPlayerInitializedEvent event, EntityRef entity) {
            Notification notification = new Notification(NOTIFICATION_ID,
                    "Where's the Manual?",
                    "Press " + getActivationKey(new SimpleUri("LightAndShadow:Tab")) + " for in-game help",
                    "engine:items#blueBook");
            localPlayer.getClientEntity().send(new ShowNotificationEvent(notification));
    }
}
