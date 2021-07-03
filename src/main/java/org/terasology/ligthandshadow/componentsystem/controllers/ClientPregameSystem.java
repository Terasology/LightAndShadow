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
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.ligthandshadow.componentsystem.events.BarrierDeactivateEvent;
import org.terasology.ligthandshadow.componentsystem.events.GameStartMessageEvent;
import org.terasology.ligthandshadow.componentsystem.events.RemoveInvulnerabilityEvent;
import org.terasology.ligthandshadow.componentsystem.events.TimerEvent;
import org.terasology.notify.ui.DialogNotificationOverlay;

import java.util.Timer;
import java.util.TimerTask;

@RegisterSystem(RegisterMode.CLIENT)
public class ClientPregameSystem extends BaseComponentSystem {

    private static Timer timer;

    public static final ResourceUrn ASSET_URI = new ResourceUrn("LightAndShadow:Timer");

    @In
    private NUIManager nuiManager;

    @In
    private LocalPlayer localPlayer;

    private DialogNotificationOverlay window;

    @Override
    public void initialise() {
        window = nuiManager.addOverlay(ASSET_URI, DialogNotificationOverlay.class);
    }

    @Override
    public void shutdown() {
        nuiManager.closeScreen(window);
    }

    @ReceiveEvent
    public void onPregameStart(GameStartMessageEvent event, EntityRef entity) {
        if (localPlayer.getClientEntity().equals(entity)) {
            window.addNotification("The game start's when there is at least one player in each team.");
        }
    }

    @ReceiveEvent
    public void updateTimer(TimerEvent event, EntityRef entity) {
        if (localPlayer.getClientEntity().equals(entity)) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                int timePeriod = 30;
                boolean addNotification;
                public void run() {
                    if (addNotification) {
                        window.removeNotification("The game start's when there is at least one player in each team.");
                        window.addNotification("The game starts in " + timePeriod-- + " seconds.");
                        addNotification = false;
                    } else {
                        window.removeNotification("The game start's when there is at least one player in each team.");
                        window.removeNotification("The game starts in " + (timePeriod + 1) + " seconds.");
                        addNotification = true;
                    }
                    if (timePeriod < 0) {
                        window.removeNotification("The game starts in " + (timePeriod + 1) + " seconds.");
                        entity.send(new BarrierDeactivateEvent());
                        entity.send(new RemoveInvulnerabilityEvent());
                        timer.cancel();
                    }
                }
            }, 0, 500);
        }
    }
}
