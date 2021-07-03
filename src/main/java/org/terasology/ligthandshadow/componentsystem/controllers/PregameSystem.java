// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.controllers;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.players.PlayerCharacterComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.Assets;
import org.terasology.ligthandshadow.componentsystem.components.PlayerInvulnerableComponent;
import org.terasology.ligthandshadow.componentsystem.events.BarrierActivateEvent;
import org.terasology.ligthandshadow.componentsystem.events.PregameEvent;
import org.terasology.ligthandshadow.componentsystem.events.RemoveInvulnerabilityEvent;
import org.terasology.module.health.events.BeforeDamagedEvent;
import org.terasology.module.inventory.components.StartingInventoryComponent;

import java.util.Optional;

@RegisterSystem(RegisterMode.AUTHORITY)
public class PregameSystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    Optional<Prefab> prefab = Assets.getPrefab("inventory");
    StartingInventoryComponent startingInventory = prefab.get().getComponent(StartingInventoryComponent.class);

    @ReceiveEvent
    public void onPregameStart(PregameEvent event, EntityRef entity) {
        entity.send(new BarrierActivateEvent());
        entity.addComponent(new PlayerInvulnerableComponent());
    }

    @ReceiveEvent(components = PlayerInvulnerableComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void preventFriendlyFire(BeforeDamagedEvent event, EntityRef entity) {
        event.consume();
    }

    @ReceiveEvent
    private void removePlayerInvulnerableComponents(RemoveInvulnerabilityEvent event, EntityRef entity) {
        Iterable<EntityRef> players = entityManager.getEntitiesWith(PlayerCharacterComponent.class,
                PlayerInvulnerableComponent.class);
        for (EntityRef player : players) {
            player.removeComponent(PlayerInvulnerableComponent.class);
        }
    }

}
