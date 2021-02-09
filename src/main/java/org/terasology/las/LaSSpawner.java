// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.las;

import org.joml.Vector3f;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.logic.spawner.Spawner;
import org.terasology.world.generation.World;

public class LaSSpawner implements Spawner {

    @Override
    public Vector3f getSpawnPosition(World world, EntityRef clientEntity) {
        return new Vector3f(LASUtils.FLOATING_PLATFORM_POSITION)
                .add(0.0f, 5.0f, 0.0f);
    }

}
