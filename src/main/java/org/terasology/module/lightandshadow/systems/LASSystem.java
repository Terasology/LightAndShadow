// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.lightandshadow.systems;

import org.terasology.economy.components.CurrencyStorageComponent;
import org.terasology.economy.events.WalletUpdatedEvent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.Priority;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.sun.CelestialSystem;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.module.inventory.components.InventoryComponent;
import org.terasology.module.inventory.events.RemoveItemAction;
import org.terasology.module.inventory.systems.InventoryManager;

@RegisterSystem
public class LASSystem extends BaseComponentSystem {
    @In
    private InventoryManager inventoryManager;
    @In
    private CelestialSystem celestialSystem;
    @In
    private GameEntitySystem gameEntitySystem;

    /**
     * Gives an empty inventory to a player in the lobby to prevent fight's in the lobby and gives the player some funds.
     */
    @Priority(EventPriority.PRIORITY_LOW)
    @ReceiveEvent
    public void onPlayerSpawn(OnPlayerSpawnedEvent event, EntityRef player, InventoryComponent inventory) {
        for (int i = 0; i < inventoryManager.getNumSlots(player); i++) {
            EntityRef itemInSlot = inventoryManager.getItemInSlot(player, i);
            player.send(new RemoveItemAction(player, itemInSlot, true));
        }
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        player.getComponent(CurrencyStorageComponent.class).amount = gameEntity.getComponent(CurrencyStorageComponent.class).amount;
        player.send(new WalletUpdatedEvent(gameEntity.getComponent(CurrencyStorageComponent.class).amount));
    }

    @Override
    public void initialise() {
        if (!celestialSystem.isSunHalted()) {
            celestialSystem.toggleSunHalting(0.5f);
        }
    }

    @Override
    public void shutdown() {
    }

}
