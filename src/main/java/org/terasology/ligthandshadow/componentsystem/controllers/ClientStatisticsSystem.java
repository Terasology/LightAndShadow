// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.controllers;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.layers.ingame.DeathScreen;
import org.terasology.input.ButtonState;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.events.ScoreUpdateFromServerEvent;
import org.terasology.ligthandshadow.componentsystem.input.TabButton;
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
                clientGameOverSystem.addTeamScore(statisticsScreen, "spadesTeamScore", blackScore);
                clientGameOverSystem.addTeamScore(statisticsScreen, "heartsTeamScore", redScore);
                clientGameOverSystem.addPlayerStatisticsInfo(statisticsScreen);
                nuiManager.toggleScreen("LightAndShadow:statisticsScreen");
                event.consume();
            }
        }
    }
}
