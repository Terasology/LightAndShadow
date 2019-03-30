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
import org.terasology.las.UI.GameoverScreen;
import org.terasology.ligthandshadow.componentsystem.components.LASTeamComponent;
import org.terasology.ligthandshadow.componentsystem.events.GameoverEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;

/**
 * Displays game over screen for all clients.
 *
 * @author darshan3
 */
@RegisterSystem(RegisterMode.CLIENT)
public class ClientGameoverSystem extends BaseComponentSystem {

    @In
    private NUIManager nuiManager;
    @In
    private LocalPlayer localPlayer;

    @ReceiveEvent
    public void onGameover(GameoverEvent event, EntityRef entity) {
        nuiManager.removeOverlay("engine:onlinePlayersOverlay");
        nuiManager.pushScreen("lightAndShadow:gameOverScreen");
        if (event.winningTeam.equals(localPlayer.getCharacterEntity().getComponent(LASTeamComponent.class).team)) {
            ((GameoverScreen) nuiManager.getScreen("lightAndShadow:gameOverScreen")).setGameoverDetails("Won");
        } else {
            ((GameoverScreen) nuiManager.getScreen("lightAndShadow:gameOverScreen")).setGameoverDetails("Lost");
        }
    }
}
