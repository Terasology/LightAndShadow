// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.lightandshadow.worldgeneration.general;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.spawner.Spawner;
import org.terasology.engine.world.generation.World;
import org.terasology.lightandshadow.gamestate.LASUtils;

public class LaSSpawner implements Spawner {

    @Override
    public Vector3f getSpawnPosition(World world, EntityRef clientEntity) {
        return new Vector3f(LASUtils.FLOATING_PLATFORM_POSITION)
                .add(0.0f, 5.0f, 0.0f);
    }

}
