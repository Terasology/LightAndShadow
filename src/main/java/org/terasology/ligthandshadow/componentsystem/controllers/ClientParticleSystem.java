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
import org.terasology.ligthandshadow.componentsystem.events.AttachParticleEmitterToPlayerEvent;
import org.terasology.ligthandshadow.componentsystem.events.RemoveParticleEmitterFromPlayerEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.particles.components.ParticleEmitterComponent;
import org.terasology.registry.In;

@RegisterSystem(RegisterMode.CLIENT)
public class ClientParticleSystem extends BaseComponentSystem {
    @In
    private EntityManager entityManager;

    private EntityBuilder builder;

    @ReceiveEvent
    public void onAttachParticleEmitterToPlayer(AttachParticleEmitterToPlayerEvent event, EntityRef entity) {
        String team = event.team;
        EntityRef player = event.player;
        if (team.equals(LASUtils.RED_TEAM)) {
            builder = entityManager.newBuilder(LASUtils.HEARTS_PARTICLE);
            builder.saveComponent(player.getComponent(LocationComponent.class));
            builder.build();
            return;
        }
        if (team.equals(LASUtils.BLACK_TEAM)) {
            builder = entityManager.newBuilder(LASUtils.SPADES_PARTICLE);
            builder.saveComponent(player.getComponent(LocationComponent.class));
            builder.build();
            return;
        }
    }

    @ReceiveEvent
    public void onRemoveParticleEmitterFromPlayer(RemoveParticleEmitterFromPlayerEvent event, EntityRef entity) {
        EntityRef particleEntity = event.entity;
        particleEntity.removeComponent(ParticleEmitterComponent.class);
    }
}
