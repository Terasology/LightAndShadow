// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.controllers;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.logic.players.PlayerCharacterComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.ligthandshadow.componentsystem.components.PlayerInvulnerableComponent;
import org.terasology.ligthandshadow.componentsystem.events.BarrierActivateEvent;
import org.terasology.ligthandshadow.componentsystem.events.BarrierDeactivateEvent;
import org.terasology.ligthandshadow.componentsystem.events.PregameEvent;
import org.terasology.ligthandshadow.componentsystem.events.TimerEvent;
import org.terasology.module.health.events.BeforeDamagedEvent;
import org.terasology.nui.ControlWidget;
import org.terasology.nui.widgets.UILabel;

import java.util.Timer;
import java.util.TimerTask;

@RegisterSystem
public class PregameSystem extends BaseComponentSystem {

    private static Timer timer;

    @In
    LocalPlayer localPlayer;
    @In
    private NUIManager nuiManager;
    @In
    private EntityManager entityManager;

    @ReceiveEvent(netFilter = RegisterMode.AUTHORITY)
    public void onPregameStart(PregameEvent event, EntityRef entity) {
        entity.send(new BarrierActivateEvent());
        entity.addComponent(new PlayerInvulnerableComponent());
    }

    @ReceiveEvent(components = PlayerInvulnerableComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void preventFriendlyFire(BeforeDamagedEvent event, EntityRef entity) {
        event.consume();
    }

    @ReceiveEvent(netFilter = RegisterMode.CLIENT)
    public void onTimerStart(TimerEvent event, EntityRef entity) {
        if (localPlayer.getClientEntity().equals(entity)) {
            ControlWidget scoreScreen = nuiManager.getHUD().getHUDElement("LightAndShadow:ScoreHud");
            UILabel countDown = scoreScreen.find("timer", UILabel.class);
            if (countDown != null) {
                timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    int timePeriod = 10;
                    public void run() {
                        countDown.setText(Integer.toString(timePeriod--));
                        if (timePeriod < 0) {
                            countDown.setText(" ");
                            entity.send(new BarrierDeactivateEvent());
                            removePlayerInvulnerableComponents();
                            timer.cancel();
                        }
                    }
                }, 0, 1000);
            }
        }
    }

    private void removePlayerInvulnerableComponents() {
        Iterable<EntityRef> players = entityManager.getEntitiesWith(PlayerCharacterComponent.class,
                PlayerInvulnerableComponent.class);
        for (EntityRef player : players) {
            player.removeComponent(PlayerInvulnerableComponent.class);
        }
    }
}
