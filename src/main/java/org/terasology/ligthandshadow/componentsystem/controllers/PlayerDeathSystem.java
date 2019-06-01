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
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.LASTeamComponent;
import org.terasology.logic.characters.AliveCharacterComponent;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.CharacterTeleportEvent;
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

    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH)
    public void beforeDestroy(BeforeDestroyEvent event, EntityRef player, CharacterComponent characterComponent, AliveCharacterComponent aliveCharacterComponent) {
        if (player.hasComponent(PlayerCharacterComponent.class)) {
            event.consume();
            String team = player.getComponent(LASTeamComponent.class).team;

            Prefab staffPrefab = assetManager.getAsset(LASUtils.MAGIC_STAFF_URI, Prefab.class).orElse(null);

            Vector3f startPosition = new Vector3f(player.getComponent(LocationComponent.class).getLocalPosition());
            Vector3f impulse = Vector3f.zero();
            int inventorySize = inventoryManager.getNumSlots(player);
            for (int slotNumber = 0; slotNumber <= inventorySize; slotNumber++) {
                EntityRef slot = inventoryManager.getItemInSlot(player, slotNumber);
                Prefab currentPrefab = slot.getParentPrefab();
                if (currentPrefab != null && !currentPrefab.equals(staffPrefab)) {
                    int count = inventoryManager.getStackSize(slot);
                    player.send(new DropItemRequest(slot, player, impulse, startPosition, count));
                }
            }

            player.send(new DoHealEvent(100000, player));
            player.send(new CharacterTeleportEvent(LASUtils.getTeleportDestination(team)));
        }
    }
}
