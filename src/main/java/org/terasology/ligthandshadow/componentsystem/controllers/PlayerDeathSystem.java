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

import java.util.Random;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.AliveCharacterComponent;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.characters.CharacterTeleportEvent;
import org.terasology.engine.logic.health.BeforeDestroyEvent;
import org.terasology.module.health.events.RestoreFullHealthEvent;
import org.terasology.module.inventory.systems.InventoryManager;
import org.terasology.module.inventory.events.DropItemRequest;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.players.PlayerCharacterComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.LASTeamComponent;
import org.terasology.ligthandshadow.componentsystem.components.PlayerStatisticsComponent;


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

    private Random random = new Random();

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
            updateStatistics(event.getInstigator(), "kills");
            updateStatistics(player, "deaths");
            dropItemsFromInventory(player);
            player.send(new RestoreFullHealthEvent(player));
            Vector3f randomVector = new Vector3f(-1 + random.nextInt(3), 0, -1 + random.nextInt(3));
            player.send(new CharacterTeleportEvent(randomVector.add(LASUtils.getTeleportDestination(team))));
        }
    }

    private void dropItemsFromInventory(EntityRef player) {
        Prefab staffPrefab = assetManager.getAsset(LASUtils.MAGIC_STAFF_URI, Prefab.class).orElse(null);
        Vector3fc deathPosition = player.getComponent(LocationComponent.class).getLocalPosition();
        Vector3f impulse = new Vector3f();
        int inventorySize = inventoryManager.getNumSlots(player);
        for (int slotNumber = 0; slotNumber <= inventorySize; slotNumber++) {
            EntityRef slot = inventoryManager.getItemInSlot(player, slotNumber);
            Prefab currentPrefab = slot.getParentPrefab();
            if (currentPrefab != null && !currentPrefab.equals(staffPrefab)) {
                int count = inventoryManager.getStackSize(slot);
                player.send(new DropItemRequest(slot, player, impulse, deathPosition, count));
            }
        }
    }

    private void updateStatistics(EntityRef player, String type) {
        PlayerStatisticsComponent playerStatisticsComponent = player.getComponent(PlayerStatisticsComponent.class);
        if (type.equals("kills")) {
            playerStatisticsComponent.kills += 1;
        }
        if (type.equals("deaths")) {
            playerStatisticsComponent.deaths += 1;
        }
        player.saveComponent(playerStatisticsComponent);
    }
}
