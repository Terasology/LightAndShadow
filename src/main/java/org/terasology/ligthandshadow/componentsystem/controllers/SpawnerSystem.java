// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.controllers;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.ligthandshadow.componentsystem.components.SpawnerComponent;
import org.terasology.math.geom.Vector3f;

import java.util.List;
import java.util.Random;

/**
 * Spawns minions at a given frequency. All spawner blocks spawns one minion of its side.
 *
 * @author synopia
 */
@RegisterSystem
public class SpawnerSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private final Random random = new Random();
    @In
    private EntityManager entityManager;
    @In
    private WorldProvider worldProvider;

    @Override
    public void initialise() {
    }

    @Override
    public void update(float delta) {
        spawnAll(delta);
    }

    private void spawnAll(float delta) {
        for (EntityRef block : entityManager.getEntitiesWith(SpawnerComponent.class, LocationComponent.class)) {
            SpawnerComponent spawnerBlock = block.getComponent(SpawnerComponent.class);
            spawnerBlock.cooldown -= delta;
            if (spawnerBlock.cooldown <= 0) {
                spawnerBlock.cooldown = spawnerBlock.each;
                if (spawnerBlock.currentlyAlive < spawnerBlock.max) {
                    LocationComponent location = block.getComponent(LocationComponent.class);
                    Vector3f worldPosition = location.getWorldPosition();
                    List<Prefab> spawn = spawnerBlock.spawn(block);
                    for (Prefab prefab : spawn) {
                        float x = worldPosition.x + random.nextInt(3) - 1;
                        float z = worldPosition.z + random.nextInt(3) - 1;
                        Vector3f spawnPosition = new Vector3f(x, worldPosition.y + 1, z);
                        entityManager.create(prefab, spawnPosition);
                    }
                }
            }
            block.saveComponent(spawnerBlock);
        }
    }

    @Override
    public void shutdown() {

    }
}
