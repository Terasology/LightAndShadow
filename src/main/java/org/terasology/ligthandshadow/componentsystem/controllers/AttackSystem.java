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

import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.ligthandshadow.componentsystem.components.RaycastComponent;
import org.terasology.ligthandshadow.componentsystem.components.RaycastOnActivateComponent;
import org.terasology.logic.characters.GazeMountPointComponent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.events.DropItemEvent;
import org.terasology.logic.inventory.events.DropItemRequest;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.PlayerCharacterComponent;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.Physics;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.physics.events.ImpulseEvent;
import org.terasology.registry.In;
import org.terasology.utilities.Assets;

public class AttackSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    @In
    InventoryManager inventoryManager;

    @In
    EntityManager entityManager;

    @In
    private Physics physicsRenderer;

    @In
    private LocalPlayer localPlayer;



    private CollisionGroup filter = StandardCollisionGroup.ALL;

    @Override
    public void update(float delta) {
        for (EntityRef projectile : entityManager.getEntitiesWith(RaycastComponent.class)) {
            LocationComponent location = projectile.getComponent(LocationComponent.class);
            RaycastComponent shot = projectile.getComponent(RaycastComponent.class);

            location.setWorldPosition(location.getWorldPosition().add(location.getWorldDirection().mul(shot.velocity)));
            projectile.saveComponent(location);
        }
    }

    @ReceiveEvent(components = {RaycastOnActivateComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity, RaycastOnActivateComponent raycastOnActivateComponent) {
        // Shoot a raycast
        Vector3f target = event.getHitNormal();
        Vector3i blockPos = new Vector3i(target);

        Vector3f position = new Vector3f(event.getOrigin());
        Vector3f dir = new Vector3f(event.getDirection());

        HitResult result;
        result = physicsRenderer.rayTrace(position, dir, 50, filter);

        EntityBuilder builder = entityManager.newBuilder("Core:defaultBlockParticles");
        builder.getComponent(LocationComponent.class).setWorldPosition(target);
        builder.build();
        EntityRef hitEntity = result.getEntity();

        // If raycast hits another player and another player has flag, make player drop flag
        if (hitEntity.hasComponent(PlayerCharacterComponent.class) && hitEntity.hasComponent(InventoryComponent.class)) {
            int flagSlot = inventoryManager.findSlotWithItem(hitEntity, entityManager.create("LightAndShadowResources:redFlag"));

             //If hit person has flag
            if (flagSlot != -1) {
                EntityRef flag = InventoryUtils.getItemAt(hitEntity, flagSlot);
                Vector3f direction = localPlayer.getViewDirection();
                Vector3f newPosition = new Vector3f(position.x + direction.x * 1.5f,
                        position.y + direction.y * 1.5f,
                        position.z + direction.z * 1.5f
                );
                Vector3f impulseVector = new Vector3f(direction);
                hitEntity.send(new DropItemRequest(flag, hitEntity, impulseVector, newPosition));
//                inventoryManager.removeItem(hitEntity, EntityRef.NULL, flagSlot, false, 1);
            }
        }

    }
}
