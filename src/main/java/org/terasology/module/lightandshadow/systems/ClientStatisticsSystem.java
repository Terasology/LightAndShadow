// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.lightandshadow.systems;

import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.Priority;
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
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.input.Input;
import org.terasology.module.lightandshadow.LASUtils;
import org.terasology.module.lightandshadow.events.ScoreUpdateFromServerEvent;
import org.terasology.module.lightandshadow.TapButton;
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
    private static final String NOTIFICATION_ID = "LightAndShadow:firstTimeStatistics";

    @In
    InputSystem inputSystem;
    @In
    private NUIManager nuiManager;
    @In
    private LocalPlayer localPlayer;
    @In
    private ClientGameOverSystem clientGameOverSystem;
    private DeathScreen statisticsScreen;
    private int redScore = 0;
    private int blackScore = 0;
    private boolean isOpen;
    private boolean isExpired;

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

    @Priority(EventPriority.PRIORITY_CRITICAL)
    @ReceiveEvent
    public void onTab(TapButton event, EntityRef entity) {
        if (event.isDown()) {
            if (localPlayer.getClientEntity().equals(entity) && !isOpen) {
                if (!isExpired) {
                    entity.send(new ExpireNotificationEvent(NOTIFICATION_ID));
                    isExpired = true;
                }
                clientGameOverSystem.addTeamScore(statisticsScreen, "spadesTeamScore", blackScore);
                clientGameOverSystem.addTeamScore(statisticsScreen, "heartsTeamScore", redScore);
                clientGameOverSystem.addPlayerStatisticsInfo(statisticsScreen);
                nuiManager.pushScreen("LightAndShadow:statisticsScreen");
                isOpen = true;
                event.consume();
            }
        } else {
            isOpen = false;
            nuiManager.closeScreen("LightAndShadow:statisticsScreen");
        }
    }

    /**
     * Get a formatted representation of the primary {@link Input} associated with the given button binding.
     *
     * If the display name of the primary bound key is a single character this representation will be the encircled
     * character. Otherwise the full display name is used. The bound key will be printed in yellow.
     *
     * If no key binding was found the text "n/a" in red color is returned.
     *
     * @param button the URI of a bindable button
     * @return a formatted text to be used as representation for the player
     */
    //TODO: put this in a common place? Duplicated in Dialogs, EventualSkills, and InGameHelp
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
                    "The Numbers Game",
                    "Hold " + getActivationKey(new SimpleUri("LightAndShadow:statistics")) + " to see statistics",
                    "engine:items#blueBook");
            localPlayer.getClientEntity().send(new ShowNotificationEvent(notification));
    }
}
