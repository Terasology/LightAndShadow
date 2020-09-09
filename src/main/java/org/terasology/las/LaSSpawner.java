// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.las;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.spawner.Spawner;
import org.terasology.engine.world.generation.World;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;

public class LaSSpawner implements Spawner {

    @Override
    public Vector3f getSpawnPosition(World world, EntityRef clientEntity) {
        return new Vector3i(LASUtils.FLOATING_PLATFORM_POSITION)
                .add(0, 5, 0)
                .toVector3f();
    }

}
