// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadow.systems;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.layers.ingame.DeathScreen;
import org.terasology.lightandshadow.LASUtils;
import org.terasology.lightandshadow.events.ClientRestartEvent;
import org.terasology.nui.UILayout;

/**
 * System to close game over screen once restart is complete.
 */

@RegisterSystem(RegisterMode.CLIENT)
public class ClientRestartSystem extends BaseComponentSystem {
    @In
    LocalPlayer localPlayer;
    @In
    NUIManager nuiManager;

    @ReceiveEvent
    public void onClientRestart(ClientRestartEvent event, EntityRef clientEntity) {
        if (localPlayer.getClientEntity().equals(clientEntity)) {
            if (nuiManager.isOpen(LASUtils.DEATH_SCREEN)) {
                DeathScreen deathScreen = (DeathScreen) nuiManager.getScreen(LASUtils.DEATH_SCREEN);
                UILayout migLayout = deathScreen.find("playerStatistics", UILayout.class);
                if (migLayout != null) {
                    migLayout.removeAllWidgets();
                }
                nuiManager.closeScreen(LASUtils.DEATH_SCREEN);
            }
        }
    }
}
