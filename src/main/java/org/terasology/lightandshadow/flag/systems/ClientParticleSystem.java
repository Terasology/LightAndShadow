// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadow.flag.systems;

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
import org.terasology.lightandshadow.LASUtils;
import org.terasology.lightandshadow.flag.components.FlagParticleComponent;
import org.terasology.lightandshadow.flag.events.FlagDropEvent;
import org.terasology.lightandshadow.flag.events.FlagPickupEvent;

@RegisterSystem(RegisterMode.CLIENT)
public class ClientParticleSystem extends BaseComponentSystem {
    @In
    private EntityManager entityManager;

    @ReceiveEvent
    public void onFlagPickup(FlagPickupEvent event, EntityRef entity) {
        String team = event.team;
        EntityRef player = event.player;

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
