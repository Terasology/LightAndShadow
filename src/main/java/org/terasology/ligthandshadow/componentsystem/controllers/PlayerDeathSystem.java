/*
 * Copyright 2019 MovingBlocks
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

import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.DroppedItemComponent;
import org.terasology.ligthandshadow.componentsystem.components.LASTeamComponent;
import org.terasology.logic.characters.AliveCharacterComponent;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.CharacterTeleportEvent;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.health.BeforeDestroyEvent;
import org.terasology.logic.health.DoHealEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.events.DropItemRequest;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.PlayerCharacterComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.BlockItemComponent;


/**
 * Handles what happens when a player dies.
 */
@RegisterSystem
public class PlayerDeathSystem extends BaseComponentSystem {
    @In
    private AssetManager assetManager;

    @In
    private InventoryManager inventoryManager;

    @In
    private WorldProvider worldProvider;

    @In
    private BlockManager blockManager;

    @In
    private EntityManager entityManager;

    @In
    private DelayManager delayManager;

    /**
     * Empty the inventory and send player player back to its base with refilled health.
     * This is a high priority method, hence it receives the event first and consumes it.
     * This prevents the destruction of player entity and prevents the deathScreen from showing up.
     *
     * @param event
     * @param player
     * @param characterComponent
     * @param aliveCharacterComponent
     */
    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH)
    public void beforeDestroy(BeforeDestroyEvent event, EntityRef player, CharacterComponent characterComponent, AliveCharacterComponent aliveCharacterComponent) {
        if (player.hasComponent(PlayerCharacterComponent.class)) {
            event.consume();
            String team = player.getComponent(LASTeamComponent.class).team;
            dropItemsFromInventory(player);
            player.send(new DoHealEvent(100000, player));
            player.send(new CharacterTeleportEvent(LASUtils.getTeleportDestination(team)));
            addDelayActionToDroppedItems();
        }
    }

    /**
     * Destroy dropped items except flag after the delay set using delay manager.
     * Flag is teleported back to the base.
     *
     * @param event     The event which is triggered once the delay is over
     * @param entity    The item on which the delay was set
     *
     * @see DelayManager
     * @see DelayedActionTriggeredEvent
     */
    @ReceiveEvent
    public void destroyDroppedItems(DelayedActionTriggeredEvent event, EntityRef entity) {
        if (event.getActionId().equals(LASUtils.DROPPED_ITEM_ON_DEATH)) {
            if (entity.hasComponent(BlockItemComponent.class)) {
                String blockFamilyURI = entity.getComponent(BlockItemComponent.class).blockFamily.getURI().toString();
                if (blockFamilyURI.equals(LASUtils.BLACK_FLAG_URI)) {
                    worldProvider.setBlock(LASUtils.getFlagLocation(LASUtils.BLACK_TEAM),
                            blockManager.getBlock(LASUtils.getFlagURI(LASUtils.BLACK_TEAM)));
                }
                if (blockFamilyURI.equals(LASUtils.RED_FLAG_URI)) {
                    worldProvider.setBlock(LASUtils.getFlagLocation(LASUtils.RED_TEAM),
                            blockManager.getBlock(LASUtils.getFlagURI(LASUtils.RED_TEAM)));
                }
            }
            entity.destroy();
        }
    }

    private void dropItemsFromInventory(EntityRef player) {
        Prefab staffPrefab = assetManager.getAsset(LASUtils.MAGIC_STAFF_URI, Prefab.class).orElse(null);
        Vector3f deathPosition = new Vector3f(player.getComponent(LocationComponent.class).getLocalPosition());
        Vector3f impulse = Vector3f.zero();
        int inventorySize = inventoryManager.getNumSlots(player);
        for (int slotNumber = 0; slotNumber <= inventorySize; slotNumber++) {
            EntityRef slot = inventoryManager.getItemInSlot(player, slotNumber);
            Prefab currentPrefab = slot.getParentPrefab();
            if (currentPrefab != null && !currentPrefab.equals(staffPrefab)) {
                slot.addComponent(new DroppedItemComponent());
                int count = inventoryManager.getStackSize(slot);
                player.send(new DropItemRequest(slot, player, impulse, deathPosition, count));
            }
        }
    }

    private void addDelayActionToDroppedItems() {
        for (EntityRef entity: entityManager.getEntitiesWith(DroppedItemComponent.class)) {
            delayManager.addDelayedAction(entity, LASUtils.DROPPED_ITEM_ON_DEATH, LASUtils.DROPPED_ITEM_DELAY);
        }
    }
}
