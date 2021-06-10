// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadow.gamestate.components;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.utilities.Assets;

import java.util.List;
import java.util.Optional;

/**
 * @author synopia
 */
public class SpawnerComponent implements Component {
    public List<String> prefabs = Lists.newArrayList();
    public float each;
    public int spawn;
    public int max;
    public int nextSpawnedPrefab;
    public int currentlyAlive;
    public float cooldown;

    public List<Prefab> spawn(EntityRef entity) {
        List<Prefab> result = Lists.newArrayList();
        for (int i = 0; i < spawn; i++) {
            Optional<Prefab> opt = nextPrefab(entity);
            if (opt.isPresent()) {
                currentlyAlive++;
                result.add(opt.get());
            } else {
                break;
            }
        }
        return result;
    }

    public Optional<Prefab> nextPrefab(EntityRef entity) {
        if (currentlyAlive < max) {
            String name = prefabs.get(nextSpawnedPrefab);
            nextSpawnedPrefab++;
            nextSpawnedPrefab %= prefabs.size();
            entity.saveComponent(this);
            return Assets.getPrefab(name);
        }
        return null;
    }
}
