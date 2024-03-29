// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.lightandshadow.systems;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.Priority;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.Assets;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.module.health.events.BeforeDamagedEvent;
import org.terasology.module.inventory.components.StartingInventoryComponent;
import org.terasology.module.lightandshadow.components.InvulnerableComponent;
import org.terasology.module.lightandshadow.events.ActivateBarrierEvent;
import org.terasology.module.lightandshadow.events.PregameEvent;

import java.util.Optional;

@RegisterSystem(RegisterMode.AUTHORITY)
public class PregameSystem extends BaseComponentSystem {
    Optional<Prefab> prefab = Assets.getPrefab("inventory");
    StartingInventoryComponent startingInventory = prefab.get().getComponent(StartingInventoryComponent.class);

    @In
    private EntityManager entityManager;

    @ReceiveEvent
    public void onPregameStart(PregameEvent event, EntityRef entity) {
        entity.send(new ActivateBarrierEvent());
        entity.addComponent(new InvulnerableComponent());
    }

    @Priority(EventPriority.PRIORITY_HIGH)
    @ReceiveEvent(components = InvulnerableComponent.class)
    public void preventFriendlyFire(BeforeDamagedEvent event, EntityRef entity) {
        event.consume();
    }

}
