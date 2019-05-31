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
import org.terasology.logic.players.PlayerCharacterComponent;
import org.terasology.registry.In;

import java.util.List;


@RegisterSystem
public class PlayerDeathSystem extends BaseComponentSystem {
    Logger logger = LoggerFactory.getLogger(PlayerDeathSystem.class);

    @In
    private AssetManager assetManager;

    @In
    private InventoryManager inventoryManager;

    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH)
    public void beforeDestroy(BeforeDestroyEvent event, EntityRef player, CharacterComponent characterComponent, AliveCharacterComponent aliveCharacterComponent) {
        if (player.hasComponent(PlayerCharacterComponent.class)) {
            event.consume();
            String team = player.getComponent(LASTeamComponent.class).team;
            Prefab flagPrefab;
            List<EntityRef> itemsSlots = player.getComponent(InventoryComponent.class).itemSlots;
            if(team == LASUtils.RED_TEAM) {
                flagPrefab = assetManager.getAsset(LASUtils.BLACK_FLAG_URI, Prefab.class).orElse(null);
            } else {
                flagPrefab = assetManager.getAsset(LASUtils.RED_FLAG_URI, Prefab.class).orElse(null);
            }

            Prefab staffPrefab = assetManager.getAsset(LASUtils.MAGIC_STAFF_URI, Prefab.class).orElse(null);

            for (EntityRef slot : itemsSlots) {
                Prefab currentPrefab = slot.getParentPrefab();
                if (currentPrefab != null && !currentPrefab.equals(staffPrefab)) {
                    inventoryManager.removeItem(player, EntityRef.NULL, slot, true, 1);
                }
            }

            player.send(new DoHealEvent(100000, player));
            player.send(new CharacterTeleportEvent(LASUtils.getTeleportDestination(team)));
        }
    }
}
