/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.ligthandshadow.componentsystem.components;

import com.google.common.collect.Lists;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;

import java.util.List;

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
            Prefab prefab = nextPrefab(entity);
            currentlyAlive++;
            if (prefab != null) {
                result.add(prefab);
            } else {
                break;
            }
        }
        return result;
    }

    public Prefab nextPrefab(EntityRef entity) {
        if (currentlyAlive < max) {
            String name = prefabs.get(nextSpawnedPrefab);
            nextSpawnedPrefab++;
            nextSpawnedPrefab %= prefabs.size();
            entity.saveComponent(this);
            return Assets.getPrefab(name).get();
        }
        return null;
    }
}
