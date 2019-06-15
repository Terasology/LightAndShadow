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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.LASTeamComponent;
import org.terasology.ligthandshadow.componentsystem.events.GameOverEvent;
import org.terasology.ligthandshadow.componentsystem.events.RestartRequestEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.layers.ingame.DeathScreen;
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

    @ReceiveEvent
    public void onGameOver(GameOverEvent event, EntityRef entity) {
        if (localPlayer.getClientEntity().equals(entity)) {
            nuiManager.removeOverlay(LASUtils.ONLINE_PLAYERS_OVERLAY);
            DeathScreen deathScreen = nuiManager.pushScreen(LASUtils.DEATH_SCREEN, DeathScreen.class);
            UILabel gameOverDetails = deathScreen.find("gameOverDetails", UILabel.class);
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

    private void triggerRestart() {
        localPlayer.getClientEntity().send(new RestartRequestEvent());
    }
}
