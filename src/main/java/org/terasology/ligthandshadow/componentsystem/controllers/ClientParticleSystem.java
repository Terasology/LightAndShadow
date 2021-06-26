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

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.location.Location;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.FlagParticleComponent;
import org.terasology.ligthandshadow.componentsystem.events.OnFlagDropEvent;
import org.terasology.ligthandshadow.componentsystem.events.OnFlagPickupEvent;

@RegisterSystem(RegisterMode.CLIENT)
public class ClientParticleSystem extends BaseComponentSystem {
    @In
    private EntityManager entityManager;

    @ReceiveEvent
    public void onFlagPickup(OnFlagPickupEvent event, EntityRef entity) {
        String team = event.getTeam();
        EntityRef player = event.getPlayer();

        if (!player.hasComponent(FlagParticleComponent.class)) {
            EntityRef particleEntity = entityManager.create(LASUtils.getFlagParticle(team));
            LocationComponent targetLoc = player.getComponent(LocationComponent.class);
            LocationComponent childLoc = particleEntity.getComponent(LocationComponent.class);
            childLoc.setWorldPosition(targetLoc.getWorldPosition(new Vector3f()));
            Location.attachChild(player, particleEntity);
            particleEntity.setOwner(player);
            player.addComponent(new FlagParticleComponent());
            player.getComponent(FlagParticleComponent.class).particleEntity = particleEntity;
        }
    }

    @ReceiveEvent
    public void onFlagDrop(OnFlagDropEvent event, EntityRef entity) {
        EntityRef player = event.getPlayer();
        if (player.hasComponent(FlagParticleComponent.class)) {
            EntityRef particleEntity = player.getComponent(FlagParticleComponent.class).particleEntity;
            if (particleEntity != EntityRef.NULL) {
                particleEntity.destroy();
            }
            player.removeComponent(FlagParticleComponent.class);
        }
    }
}
