/*
 * Copyright 2017 MovingBlocks
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
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.FlagDropOnActivateComponent;
import org.terasology.ligthandshadow.componentsystem.components.LASTeamComponent;
import org.terasology.ligthandshadow.componentsystem.components.RaycastOnActivateComponent;
import org.terasology.logic.characters.CharacterHeldItemComponent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.events.DropItemRequest;
import org.terasology.logic.inventory.events.GiveItemEvent;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.PlayerCharacterComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.physics.Physics;
import org.terasology.protobuf.EntityData;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.BlockItemComponent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class AttackSystem extends BaseComponentSystem {
    @In
    InventoryManager inventoryManager;
    @In
    EntityManager entityManager;
    @In
    private LocalPlayer localPlayer;
    @In
    private WorldProvider worldProvider;
    @In
    private BlockManager blockManager;

    public EntityRef blackFlagSlot;
    public EntityRef redFlagSlot;

    @ReceiveEvent(components = {FlagDropOnActivateComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        // Entity refers to the thing being activated (in this case the other player)
        EntityRef attackingPlayer = event.getInstigator(); // The player using the staff

        // If the attacking player is holding the magic staff when activating
        if (attackingPlayer.getComponent(CharacterHeldItemComponent.class).selectedItem.hasComponent(RaycastOnActivateComponent.class)) {
            // If raycast hits another player and another player has flag, make player drop flag
            if (entity.hasComponent(PlayerCharacterComponent.class)) {
                blackFlagSlot = searchInventoryForFlag(entity, LASUtils.BLACK_FLAG_URI);
                if (blackFlagSlot != EntityRef.NULL) {
                    Vector3f position = new Vector3f(entity.getComponent(LocationComponent.class).getLocalPosition());
                    Vector3f impulseVector = new Vector3f(attackingPlayer.getComponent(LocationComponent.class).getLocalPosition());
                    entity.send(new DropItemRequest(blackFlagSlot, entity, impulseVector, position));
                    return;
                }
                redFlagSlot = searchInventoryForFlag(entity, LASUtils.RED_FLAG_URI);
                if (redFlagSlot != EntityRef.NULL) {
                    Vector3f position = new Vector3f(entity.getComponent(LocationComponent.class).getLocalPosition());
                    Vector3f impulseVector = new Vector3f(attackingPlayer.getComponent(LocationComponent.class).getLocalPosition());
                    entity.send(new DropItemRequest(redFlagSlot, entity, impulseVector, position));
                    return;
                }
            }
        }
    }

    @ReceiveEvent
    public void onInventorySlotChanged(InventorySlotChangedEvent event, EntityRef entity) {
        // Check inventory to see if player picks up flag of the same team
        // If so, move flag back to base
        EntityRef playerEntity = localPlayer.getCharacterEntity();
        EntityRef item = event.getNewItem();
        if (playerEntity.hasComponent(LASTeamComponent.class) && item.hasComponent(BlockItemComponent.class)) {
            // If player team and flag team are the same, return flag to base
            if (item.getComponent(BlockItemComponent.class).blockFamily.getURI().toString().equals(LASUtils.RED_FLAG_URI)
                    && playerEntity.getComponent(LASTeamComponent.class).team.equals(LASUtils.RED_TEAM)) {
                worldProvider.setBlock(new Vector3i(LASUtils.CENTER_RED_BASE_POSITION.x, LASUtils.CENTER_RED_BASE_POSITION.y + 1, LASUtils.CENTER_RED_BASE_POSITION.z), blockManager.getBlock(LASUtils.RED_FLAG_URI));
                inventoryManager.removeItem(playerEntity, EntityRef.NULL, item, true, 1);
                return;
            }
            if (item.getComponent(BlockItemComponent.class).blockFamily.getURI().toString().equals(LASUtils.BLACK_FLAG_URI)
                    && playerEntity.getComponent(LASTeamComponent.class).team.equals(LASUtils.BLACK_TEAM)) {
                worldProvider.setBlock(new Vector3i(LASUtils.CENTER_BLACK_BASE_POSITION.x, LASUtils.CENTER_BLACK_BASE_POSITION.y + 1, LASUtils.CENTER_BLACK_BASE_POSITION.z), blockManager.getBlock(LASUtils.BLACK_FLAG_URI));
                inventoryManager.removeItem(playerEntity, EntityRef.NULL, item, true, 1);
                return;
            }
        }
    }

    /** Returns entityRef of item in inventory */
    public EntityRef searchInventoryForFlag(EntityRef entity, String flagTeam) {
        int inventorySize = inventoryManager.getNumSlots(entity);
        for (int slotNumber = 0; slotNumber <= inventorySize; slotNumber++) {
            EntityRef inventorySlot = inventoryManager.getItemInSlot(entity, slotNumber);
            if (inventorySlot.hasComponent(BlockItemComponent.class)) {
                if (inventorySlot.getComponent(BlockItemComponent.class).blockFamily.getURI().toString().equals(flagTeam)) {
                    return inventorySlot;
                }
            }
        }
        return EntityRef.NULL;
    }
}
