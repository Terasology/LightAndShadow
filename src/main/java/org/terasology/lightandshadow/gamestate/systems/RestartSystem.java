// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadow.gamestate.systems;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterTeleportEvent;
import org.terasology.lightandshadow.LASUtils;
import org.terasology.module.health.events.RestoreFullHealthEvent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.layers.ingame.DeathScreen;
import org.terasology.lightandshadow.gamestate.components.LASTeamComponent;
import org.terasology.lightandshadow.gamestate.events.ClientRestartEvent;
import org.terasology.lightandshadow.gamestate.events.RestartRequestEvent;
import org.terasology.nui.layouts.miglayout.MigLayout;

@RegisterSystem
public class RestartSystem extends BaseComponentSystem {
    @In
    LocalPlayer localPlayer;
    @In
    EntityManager entityManager;
    @In
    NUIManager nuiManager;

    /**
     * System to invoke restart of a game round.
     * All players' health are restored and they are transported back to their bases.
     *
     * @param event
     * @param clientEntity
     */
    @ReceiveEvent(netFilter = RegisterMode.AUTHORITY)
    public void onRestartRequest(RestartRequestEvent event, EntityRef clientEntity, ClientComponent clientComponent) {
            Iterable<EntityRef> clients = entityManager.getEntitiesWith(ClientComponent.class);
            for (EntityRef client: clients) {
                EntityRef player = client.getComponent(ClientComponent.class).character;
                String team = player.getComponent(LASTeamComponent.class).team;
                player.send(new RestoreFullHealthEvent(player));
                player.send(new CharacterTeleportEvent(LASUtils.getTeleportDestination(team)));
                client.send(new ClientRestartEvent());
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
                DeathScreen deathScreen = (DeathScreen) nuiManager.getScreen(LASUtils.DEATH_SCREEN);
                MigLayout migLayout = deathScreen.find("playerStatistics", MigLayout.class);
                if (migLayout != null) {
                    migLayout.removeAllWidgets();
                }
                nuiManager.closeScreen(LASUtils.DEATH_SCREEN);
            }
        }
    }
}
