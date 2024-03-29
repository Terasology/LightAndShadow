// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.lightandshadow.systems;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.location.Location;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.module.lightandshadow.LASUtils;
import org.terasology.module.lightandshadow.components.FlagParticleComponent;
import org.terasology.module.lightandshadow.events.OnFlagDropEvent;
import org.terasology.module.lightandshadow.events.OnFlagPickupEvent;

@RegisterSystem(RegisterMode.CLIENT)
public class ClientParticleSystem extends BaseComponentSystem {
    @In
    private EntityManager entityManager;

    /**
     * Attaches a particle emitter to the player when the player picks up a flag.
     * Emitted particles are based on the flag's team.
     * @see OnFlagPickupEvent
     * @see AttackSystem
     *
     * @param event
     * @param entity
     */
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

    /**
     * Removes the particle emitter from the player when the player drops the flag.
     * @see OnFlagDropEvent
     * @see AttackSystem
     *
     * @param event
     * @param entity
     */
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
