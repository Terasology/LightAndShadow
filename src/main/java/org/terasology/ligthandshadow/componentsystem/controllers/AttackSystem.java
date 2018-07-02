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
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.FlagDropOnActivateComponent;
import org.terasology.ligthandshadow.componentsystem.components.RaycastOnActivateComponent;
import org.terasology.logic.characters.CharacterHeldItemComponent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.events.DropItemRequest;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.PlayerCharacterComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.Physics;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.registry.In;
import org.terasology.world.block.items.BlockItemComponent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class AttackSystem extends BaseComponentSystem {
    @In
    InventoryManager inventoryManager;
    @In
    EntityManager entityManager;
    @In
    private Physics physicsRenderer;
    @In
    private LocalPlayer localPlayer;

    private CollisionGroup filter = StandardCollisionGroup.ALL;

    @ReceiveEvent(components = {FlagDropOnActivateComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        // Entity refers to the thing being activated (in this case the other player)
        EntityRef attackingPlayer = event.getInstigator(); // The player using the staff

        // If the attacking player is holding the magic staff when activating
        if (attackingPlayer.getComponent(CharacterHeldItemComponent.class).selectedItem.hasComponent(RaycastOnActivateComponent.class)) {
            // If raycast hits another player and another player has flag, make player drop flag
            if (entity.hasComponent(PlayerCharacterComponent.class)) {
                int inventorySize = inventoryManager.getNumSlots(entity);
                for (int slotNumber = 0; slotNumber <= inventorySize; slotNumber++) {
                    EntityRef inventorySlot = inventoryManager.getItemInSlot(entity, slotNumber);
                    if (inventorySlot.getComponent(BlockItemComponent.class).blockFamily.getURI().toString().equals(LASUtils.BLACK_FLAG_URI)
                            || inventorySlot.getComponent(BlockItemComponent.class).blockFamily.getURI().toString().equals(LASUtils.RED_FLAG_URI)) {
                        Vector3f position = new Vector3f(attackingPlayer.getComponent(LocationComponent.class).getLocalPosition());
                        Vector3f direction = localPlayer.getViewDirection();
                        Vector3f newPosition = new Vector3f(position.x + direction.x,
                                position.y + direction.y,
                                position.z + direction.z
                        );
                        Vector3f impulseVector = new Vector3f(direction);
                        entity.send(new DropItemRequest(inventorySlot, entity, impulseVector, newPosition));
                        return;
                    }
                }
            }
        }
    }
}
