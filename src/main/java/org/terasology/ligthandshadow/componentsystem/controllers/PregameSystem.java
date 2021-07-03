// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.Assets;
import org.terasology.ligthandshadow.componentsystem.components.InvulnerableComponent;
import org.terasology.ligthandshadow.componentsystem.events.ActivateBarrierEvent;
import org.terasology.ligthandshadow.componentsystem.events.PregameEvent;
import org.terasology.module.health.events.BeforeDamagedEvent;
import org.terasology.module.inventory.components.StartingInventoryComponent;

import java.util.Optional;

@RegisterSystem(RegisterMode.AUTHORITY)
public class PregameSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(PregameSystem.class);

    @In
    private EntityManager entityManager;

    Optional<Prefab> prefab = Assets.getPrefab("inventory");
    StartingInventoryComponent startingInventory = prefab.get().getComponent(StartingInventoryComponent.class);

    @ReceiveEvent
    public void onPregameStart(PregameEvent event, EntityRef entity) {
        entity.send(new ActivateBarrierEvent());
        entity.addComponent(new InvulnerableComponent());
    }

    @ReceiveEvent(components = InvulnerableComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void preventFriendlyFire(BeforeDamagedEvent event, EntityRef entity) {
        event.consume();
    }

}
