package org.terasology.ligthandshadow.componentsystem.controllers;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.ligthandshadow.componentsystem.components.SpawnerComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;

import javax.vecmath.Vector3f;
import java.util.List;
import java.util.Random;

/**
 * Spawns minions at a given frequency. All spawner blocks spawns one minion of its side.
 *
 * @author synopia
 */
@RegisterSystem
public class SpawnerSystem implements ComponentSystem, UpdateSubscriberSystem {
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
