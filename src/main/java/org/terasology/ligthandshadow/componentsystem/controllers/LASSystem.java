/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.ligthandshadow.componentsystem.controllers;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.action.GiveItemAction;
import org.terasology.logic.inventory.action.RemoveItemAction;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.BlockItemFactory;

/**
 * Created by synopia on 24.01.14.
 */
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

    @ReceiveEvent
    public void onPlayerSpawn(OnPlayerSpawnedEvent event, EntityRef player, InventoryComponent inventory) {
        BlockItemFactory blockFactory = new BlockItemFactory(entityManager);
        for (int i = 0; i < inventoryManager.getNumSlots(player); i++) {
            EntityRef itemInSlot = inventoryManager.getItemInSlot(player, i);
            player.send(new RemoveItemAction(player, itemInSlot, true));
        }
        player.send(new GiveItemAction(player, entityManager.create("Behaviors:jobWalkToBlock"), 1));
        player.send(new GiveItemAction(player, entityManager.create("Behaviors:jobBuildBlock"), 2));
        player.send(new GiveItemAction(player, entityManager.create("Behaviors:jobRemoveBlock"), 3));

        player.send(new GiveItemAction(player, blockFactory.newInstance(blockManager.getBlockFamily("redSpawn")), 4));
        player.send(new GiveItemAction(player, blockFactory.newInstance(blockManager.getBlockFamily("blackSpawn")), 5));

        giveItem(player, 6, "clubsAce");
        giveItem(player, 7, "diamondsAce");
        giveItem(player, 8, "heartsAce");
        giveItem(player, 9, "spadesAce");
    }

    private void giveItem(EntityRef player, int slot, String name) {
        int stackSize = 64;
        for (int i = 0; i < stackSize; i++) {
            EntityRef item = entityManager.create(name);
            player.send(new GiveItemAction(player, item, slot));
        }
    }

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

}
