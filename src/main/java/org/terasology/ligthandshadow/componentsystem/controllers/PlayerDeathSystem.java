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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.PlayerCharacterComponent;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.BlockItemComponent;

import java.util.List;


@RegisterSystem
public class PlayerDeathSystem extends BaseComponentSystem {
    Logger logger = LoggerFactory.getLogger(PlayerDeathSystem.class);

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
            List<EntityRef> itemsSlots = player.getComponent(InventoryComponent.class).itemSlots;

            Prefab staffPrefab = assetManager.getAsset(LASUtils.MAGIC_STAFF_URI, Prefab.class).orElse(null);

            for (EntityRef slot : itemsSlots) {
                Prefab currentPrefab = slot.getParentPrefab();
                if (currentPrefab != null && !currentPrefab.equals(staffPrefab)) {
                    if (slot.hasComponent(BlockItemComponent.class) &&
                            (slot.getComponent(BlockItemComponent.class).blockFamily.getURI().toString().equals(LASUtils.BLACK_FLAG_URI) ||
                                    slot.getComponent(BlockItemComponent.class).blockFamily.getURI().toString().equals(LASUtils.RED_FLAG_URI) )) {
                        if(team.equals(LASUtils.BLACK_TEAM)) {
                            worldProvider.setBlock(LASUtils.getFlagLocation(LASUtils.RED_TEAM), blockManager.getBlock(LASUtils.getFlagURI(LASUtils.RED_TEAM)));
                        } else {
                            worldProvider.setBlock(LASUtils.getFlagLocation(LASUtils.BLACK_TEAM), blockManager.getBlock(LASUtils.getFlagURI(LASUtils.BLACK_TEAM)));
                        }
                    }
                    inventoryManager.removeItem(player, EntityRef.NULL, slot, true);
                }
            }

            player.send(new DoHealEvent(100000, player));
            player.send(new CharacterTeleportEvent(LASUtils.getTeleportDestination(team)));
        }
    }
}
