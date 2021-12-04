// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.lightandshadow.systems;

import java.util.Optional;
import java.util.Random;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.engine.logic.players.SetDirectionEvent;
import org.terasology.engine.utilities.Assets;
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
import org.terasology.module.inventory.components.StartingInventoryComponent;
import org.terasology.module.inventory.events.RequestInventoryEvent;
import org.terasology.module.inventory.systems.InventoryManager;
import org.terasology.module.inventory.events.DropItemRequest;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.players.PlayerCharacterComponent;
import org.terasology.engine.registry.In;
import org.terasology.module.lightandshadow.LASUtils;
import org.terasology.module.lightandshadow.components.PlayerStatisticsComponent;
import org.terasology.lightandshadowresources.components.LASTeamComponent;


/**
 * Handles what happens when a player dies.
 */
@RegisterSystem
public class PlayerDeathSystem extends BaseComponentSystem {
    private static final Random RANDOM = new Random();

    Optional<Prefab> prefab = Assets.getPrefab("inventory");
    StartingInventoryComponent startingInventory = prefab.get().getComponent(StartingInventoryComponent.class);

    @In
    private InventoryManager inventoryManager;

    /**
     * Reset the inventory and send the player back to its base with refilled health.
     * This is a high priority method, hence it receives the event first and consumes it.
     * This prevents the destruction of player entity and prevents the deathScreen from showing up.
     *
     * @param event notification event that a player entity is about to be destroyed (usually sent on death)
     * @param player the player entity about to be destroyed
     * @param characterComponent ensures that the entity has a character (TODO: why do we need this?)
     * @param aliveCharacterComponent ensures that the player is currently alive (TODO: parameter not used, move to annotation?)
     */
    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH)
    public void beforeDestroy(BeforeDestroyEvent event, EntityRef player,
                              CharacterComponent characterComponent, AliveCharacterComponent aliveCharacterComponent) {
        if (player.hasComponent(PlayerCharacterComponent.class)) {
            event.consume();
            String team = player.getComponent(LASTeamComponent.class).team;
            // if a player dies on their own account, we don't want to update kill statistics
            //TODO: 'OwnerSpecific#getUltimateOwner' in CombatSystem may return 'null' if it cannot find the owner of an attacking weapon
            //      or projectile. Therefore, the event's instigator might be 'null'.
            //      This happens, for instance, for the thrown spear. We need to investigate why the spear projectile does not carry the
            //      information about the instigator to fix the root cause. Until then, adding a null check here to prevent a crash by NPE.
            if (event.getInstigator() != null && event.getInstigator() != EntityRef.NULL) {
                updateStatistics(event.getInstigator(), StatisticType.KILLS);
            }
            updateStatistics(player, StatisticType.DEATHS);
            dropItemsFromInventory(player);
            player.send(new RestoreFullHealthEvent(player));
            Vector3f randomVector = new Vector3f(-1 + RANDOM.nextInt(3), 0, -1 + RANDOM.nextInt(3));
            player.send(new CharacterTeleportEvent(randomVector.add(LASUtils.getTeleportDestination(team))));

            player.send(new SetDirectionEvent(LASUtils.getYaw(LASUtils.getTeleportDestination(team).
                    sub(LASUtils.getTeleportDestination(LASUtils.getOppositionTeam(team)), new Vector3f())), 0));
            player.addOrSaveComponent(startingInventory);
            player.send(new RequestInventoryEvent(startingInventory.items));
        }
    }

    private void dropItemsFromInventory(EntityRef player) {
        Vector3fc deathPosition = player.getComponent(LocationComponent.class).getLocalPosition();
        Vector3f impulse = new Vector3f();
        int inventorySize = inventoryManager.getNumSlots(player);
        for (int slotNumber = 0; slotNumber <= inventorySize; slotNumber++) {
            EntityRef slot = inventoryManager.getItemInSlot(player, slotNumber);
            Prefab currentPrefab = slot.getParentPrefab();
            if (currentPrefab != null) {
                int count = inventoryManager.getStackSize(slot);
                player.send(new DropItemRequest(slot, player, impulse, deathPosition, count));
            }
        }
    }

    /**
     * Count up the respective statistic value for the given player and save it in their {@link PlayerStatisticsComponent}.
     *
     * @param player a player entity that must have a {@link PlayerStatisticsComponent}.
     * @param statisticType which statistic to update
     */
    private void updateStatistics(EntityRef player, StatisticType statisticType) {
        PlayerStatisticsComponent playerStatisticsComponent = player.getComponent(PlayerStatisticsComponent.class);
        switch (statisticType) {
            case KILLS:
                playerStatisticsComponent.kills += 1;
                break;
            case DEATHS:
                playerStatisticsComponent.deaths += 1;
                break;
            default:
                return;
        }
        player.saveComponent(playerStatisticsComponent);
    }

    private enum StatisticType {
        KILLS, DEATHS
    }
}
