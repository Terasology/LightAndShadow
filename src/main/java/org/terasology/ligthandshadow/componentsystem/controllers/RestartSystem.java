// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.controllers;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterTeleportEvent;
import org.terasology.engine.logic.players.SetDirectionEvent;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.lightandshadowresources.components.LASTeamComponent;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.events.ClientRestartEvent;
import org.terasology.ligthandshadow.componentsystem.events.RestartRequestEvent;
import org.terasology.module.health.events.RestoreFullHealthEvent;

/**
 * System to invoke restart of a game round.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class RestartSystem extends BaseComponentSystem {
    @In
    EntityManager entityManager;

    /**
     * All players' health are restored and they are transported back to their bases.
     *
     * @param event
     * @param clientEntity
     */
    @ReceiveEvent
    public void onRestartRequest(RestartRequestEvent event, EntityRef clientEntity, ClientComponent clientComponent) {
        Iterable<EntityRef> clients = entityManager.getEntitiesWith(ClientComponent.class);
        for (EntityRef client: clients) {
            EntityRef player = client.getComponent(ClientComponent.class).character;
            String team = player.getComponent(LASTeamComponent.class).team;
            player.send(new RestoreFullHealthEvent(player));
            player.send(new CharacterTeleportEvent(LASUtils.getTeleportDestination(team)));
            player.send(new SetDirectionEvent(LASUtils.getYaw(LASUtils.getTeleportDestination(team).
                    sub(LASUtils.getTeleportDestination(LASUtils.getOppositionTeam(team)), new Vector3f())), 0));
            client.send(new ClientRestartEvent());
        }
    }
}
