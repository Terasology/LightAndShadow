/*
 * Copyright 2015 MovingBlocks
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
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.ligthandshadow.componentsystem.components.SpawnerComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;

import java.util.List;
import java.util.Random;

/**
 * Spawns minions at a given frequency. All spawner blocks spawns one minion of its side.
 *
 * @author synopia
 */
@RegisterSystem
public class SpawnerSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    @In
    private EntityManager entityManager;
    @In
    private WorldProvider worldProvider;
    private Random random = new Random();

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
