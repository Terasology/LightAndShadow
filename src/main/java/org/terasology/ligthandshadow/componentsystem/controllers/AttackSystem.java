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
import org.terasology.ligthandshadow.componentsystem.components.RaycastOnActivateComponent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.PlayerCharacterComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.Physics;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.registry.In;

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

    @ReceiveEvent(components = {RaycastOnActivateComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity, RaycastOnActivateComponent raycastOnActivateComponent) {
        // Shoot a raycast
        Vector3f dir;
        Vector3f position = new Vector3f(event.getOrigin());
        dir = new Vector3f(event.getDirection());
        HitResult result;
        result = physicsRenderer.rayTrace(position, dir, 100f, filter);
        EntityRef hitEntity = result.getEntity();

        // If raycast hits another player and another player has flag, make player drop flag
        if (hitEntity.hasComponent(PlayerCharacterComponent.class) && hitEntity.hasComponent(InventoryComponent.class)) {
            int flagSlot = inventoryManager.findSlotWithItem(hitEntity, entityManager.create("LightAndShadowResources:RedFlag"));
            // If hit person has flag
            if (flagSlot != -1) {
                inventoryManager.removeItem(hitEntity, EntityRef.NULL, flagSlot, false, 1);
            }
        }

    }
}
