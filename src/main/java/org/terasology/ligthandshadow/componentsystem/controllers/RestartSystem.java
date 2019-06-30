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

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.LASTeamComponent;
import org.terasology.ligthandshadow.componentsystem.events.ClientRestartEvent;
import org.terasology.ligthandshadow.componentsystem.events.RestartRequestEvent;
import org.terasology.logic.characters.CharacterTeleportEvent;
import org.terasology.logic.health.DoHealEvent;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;

@RegisterSystem
public class RestartSystem extends BaseComponentSystem {
    @In
    LocalPlayer localPlayer;
    @In
    EntityManager entityManager;
    @In
    NUIManager nuiManager;
    @In
    PermissionManager permissionManager;

    /**
     * System to invoke restart. Only the host can restart the game.
     * All players' health are restored and they are transported back to their bases.
     *
     * @param event
     * @param clientEntity
     */
    @ReceiveEvent(netFilter = RegisterMode.AUTHORITY)
    public void onRestartRequest(RestartRequestEvent event, EntityRef clientEntity, ClientComponent clientComponent) {
        if (permissionManager.hasPermission(clientComponent.clientInfo, LASUtils.RESTART_PERMISSION)) {
            Iterable<EntityRef> clients = entityManager.getEntitiesWith(ClientComponent.class);
            for (EntityRef client: clients) {
                EntityRef player = client.getComponent(ClientComponent.class).character;
                String team = player.getComponent(LASTeamComponent.class).team;
                player.send(new DoHealEvent(100000, player));
                player.send(new CharacterTeleportEvent(LASUtils.getTeleportDestination(team)));
                client.send(new ClientRestartEvent());
            }
        }
    }

    /**
     * System to close game over screen once restart is complete.
     *
     * @param event
     * @param clientEntity
     */
    @ReceiveEvent(netFilter = RegisterMode.CLIENT)
    public void onClientRestart(ClientRestartEvent event, EntityRef clientEntity) {
        if (localPlayer.getClientEntity().equals(clientEntity)) {
            if (nuiManager.isOpen(LASUtils.DEATH_SCREEN)) {
                nuiManager.closeScreen(LASUtils.DEATH_SCREEN);
            }
        }
    }
}
