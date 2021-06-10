// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadow.gamestate.systems;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.module.inventory.components.InventoryComponent;
import org.terasology.module.inventory.systems.InventoryManager;
import org.terasology.module.inventory.events.RemoveItemAction;
import org.terasology.engine.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.items.BlockItemFactory;
import org.terasology.engine.world.sun.CelestialSystem;

@RegisterSystem
public class LASSystem extends BaseComponentSystem {
    @In
    private EntityManager entityManager;
    @In
    private InventoryManager inventoryManager;
    @In
    private WorldProvider worldProvider;
    @In
    private BlockManager blockManager;
    @In
    private CelestialSystem celestialSystem;

    /**
     * Gives player inventory items on game start
     */
    @ReceiveEvent
    public void onPlayerSpawn(OnPlayerSpawnedEvent event, EntityRef player, InventoryComponent inventory) {
        BlockItemFactory blockFactory = new BlockItemFactory(entityManager);
        for (int i = 0; i < inventoryManager.getNumSlots(player); i++) {
            EntityRef itemInSlot = inventoryManager.getItemInSlot(player, i);
            player.send(new RemoveItemAction(player, itemInSlot, true));
        }
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
