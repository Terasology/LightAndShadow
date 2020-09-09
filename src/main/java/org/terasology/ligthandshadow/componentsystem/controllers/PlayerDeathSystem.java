// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.controllers;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.AliveCharacterComponent;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.characters.CharacterTeleportEvent;
import org.terasology.engine.logic.destruction.BeforeDestroyEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.players.PlayerCharacterComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.health.logic.event.RestoreFullHealthEvent;
import org.terasology.inventory.logic.InventoryManager;
import org.terasology.inventory.logic.events.DropItemRequest;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.LASTeamComponent;
import org.terasology.ligthandshadow.componentsystem.components.PlayerStatisticsComponent;
import org.terasology.math.geom.Vector3f;


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

    /**
     * Empty the inventory and send player player back to its base with refilled health. This is a high priority method,
     * hence it receives the event first and consumes it. This prevents the destruction of player entity and prevents
     * the deathScreen from showing up.
     *
     * @param event
     * @param player
     * @param characterComponent
     * @param aliveCharacterComponent
     */
    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH)
    public void beforeDestroy(BeforeDestroyEvent event, EntityRef player, CharacterComponent characterComponent,
                              AliveCharacterComponent aliveCharacterComponent) {
        if (player.hasComponent(PlayerCharacterComponent.class)) {
            event.consume();
            String team = player.getComponent(LASTeamComponent.class).team;
            updateStatistics(event.getInstigator(), "kills");
            updateStatistics(player, "deaths");
            dropItemsFromInventory(player);
            player.send(new RestoreFullHealthEvent(player));
            player.send(new CharacterTeleportEvent(LASUtils.getTeleportDestination(team)));
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
