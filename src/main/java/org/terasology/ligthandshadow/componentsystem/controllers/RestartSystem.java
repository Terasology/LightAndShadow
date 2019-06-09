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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.LASTeamComponent;
import org.terasology.ligthandshadow.componentsystem.events.ClientRestartEvent;
import org.terasology.ligthandshadow.componentsystem.events.RestartEvent;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.CharacterTeleportEvent;
import org.terasology.logic.health.DoHealEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layers.ingame.DeathScreen;
import org.terasology.rendering.nui.layouts.miglayout.MigLayout;

@RegisterSystem
public class RestartSystem extends BaseComponentSystem {
    Logger logger = LoggerFactory.getLogger(RestartSystem.class);
    @In
    private EntityManager entityManager;
    @In
    private NUIManager nuiManager;

    @ReceiveEvent(netFilter = RegisterMode.AUTHORITY)
    public void onRestart(RestartEvent event, EntityRef entity) {
        logger.info("authority restart");
        Iterable<EntityRef> clients = entityManager.getEntitiesWith(ClientComponent.class);
        for (EntityRef client : clients) {
            EntityRef player = client.getComponent(ClientComponent.class).character;
            String team = player.getComponent(LASTeamComponent.class).team;
            player.send(new DoHealEvent(100000, player));
            player.send(new CharacterTeleportEvent(LASUtils.getTeleportDestination(team)));
            player.send(new ClientRestartEvent());
        }
    }

    @ReceiveEvent(netFilter = RegisterMode.CLIENT)
    public void onClientRestart(ClientRestartEvent event, EntityRef entity) {
        logger.info("client restart");
        DeathScreen deathScreen = nuiManager.pushScreen(LASUtils.DEATH_SCREEN, DeathScreen.class);
        MigLayout migLayout = deathScreen.find("playerStatistics", MigLayout.class);
        if (migLayout != null) {
            migLayout.removeAllWidgets();
        }
        nuiManager.closeScreen(LASUtils.DEATH_SCREEN);
    }

}
