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
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.FlagParticleComponent;
import org.terasology.ligthandshadow.componentsystem.components.HasFlagComponent;
import org.terasology.ligthandshadow.componentsystem.events.AttachParticleEmitterToPlayerEvent;
import org.terasology.ligthandshadow.componentsystem.events.FlagDropEvent;
import org.terasology.ligthandshadow.componentsystem.events.FlagPickupEvent;
import org.terasology.ligthandshadow.componentsystem.events.RemoveParticleEmitterFromPlayerEvent;
import org.terasology.logic.location.Location;
import org.terasology.logic.location.LocationComponent;
import org.terasology.particles.components.ParticleEmitterComponent;
import org.terasology.registry.In;

@RegisterSystem(RegisterMode.CLIENT)
public class ClientParticleSystem extends BaseComponentSystem {
    @In
    private EntityManager entityManager;

    private EntityBuilder builder;

    @ReceiveEvent
    public void onFlagPickup(FlagPickupEvent event, EntityRef entity) {
        String team = event.team;
        EntityRef player = event.player;

        EntityRef particleEntity = entityManager.create(LASUtils.getFlagParticle(team));
        FlagParticleComponent particleComponent = new FlagParticleComponent(particleEntity);

        LocationComponent targetLoc = player.getComponent(LocationComponent.class);
        LocationComponent childLoc = particleEntity.getComponent(LocationComponent.class);
        childLoc.setWorldPosition(targetLoc.getWorldPosition());
        Location.attachChild(player, particleEntity);
        particleEntity.setOwner(player);

        player.addOrSaveComponent(particleComponent);
    }

    @ReceiveEvent
    public void onFlagDrop(FlagDropEvent event, EntityRef entity) {
        EntityRef player = event.player;
        if (player.hasComponent(FlagParticleComponent.class)) {
            EntityRef particleEntity = player.getComponent(FlagParticleComponent.class).particleEntity;
            if (particleEntity != EntityRef.NULL) {
                particleEntity.destroy();
            }
            player.removeComponent(FlagParticleComponent.class);
        }
    }
}
