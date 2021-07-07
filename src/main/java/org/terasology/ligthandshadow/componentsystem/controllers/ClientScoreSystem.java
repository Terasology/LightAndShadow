// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.controllers;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.events.ScoreUpdateFromServerEvent;
import org.terasology.nui.ControlWidget;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.UILabel;

@RegisterSystem(RegisterMode.CLIENT)
public class ClientScoreSystem extends BaseComponentSystem {
    @In
    private NUIManager nuiManager;
    @In
    private LocalPlayer localPlayer;

    private int redScore = 0;
    private int blackScore = 0;

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
        UILabel blackGoalScore = scoreScreen.find("blackGoalScore", UILabel.class);
        blackGoalScore.setText(Integer.toString(LASUtils.GOAL_SCORE));
        UILabel redGoalScore = scoreScreen.find("redGoalScore", UILabel.class);
        redGoalScore.setText(Integer.toString(LASUtils.GOAL_SCORE));
    }

    @Override
    public void initialise() {
        // Displays score UI to client on game start
        nuiManager.getHUD().addHUDElement("ScoreHud");
    }

    @ReceiveEvent
    public void onScoreUpdateFromServer(ScoreUpdateFromServerEvent event, EntityRef entity) {
        String team = event.team;
        if (team.equals(LASUtils.RED_TEAM)) {
            redScore = event.score;
            return;
        }
        if (team.equals(LASUtils.BLACK_TEAM)) {
            blackScore = event.score;
            return;
        }
    }
}
