// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.las.platform;

import org.terasology.engine.entitySystem.entity.EntityStore;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.generation.EntityBuffer;
import org.terasology.engine.world.generation.EntityProviderPlugin;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.math.geom.Vector3f;

/**
 *
 */
@RegisterPlugin
public class GuidanceNpcProvider implements EntityProviderPlugin {

    @Override
    public void process(Region region, EntityBuffer buffer) {
        if (region.getRegion().encompasses(
                0,
                LASUtils.FLOATING_PLATFORM_HEIGHT_LEVEL + 4,
                LASUtils.FLOATING_PLATFORM_POSITION.z + LASUtils.NPC_OFFSET)) {

            Prefab chooseFactionNpc = Assets.getPrefab("LightAndShadow:MagicFool").get();
            EntityStore entity = new EntityStore(chooseFactionNpc);
            entity.addComponent(new LocationComponent(new Vector3f(
                    0,
                    LASUtils.FLOATING_PLATFORM_HEIGHT_LEVEL + 1,
                    LASUtils.FLOATING_PLATFORM_POSITION.z + LASUtils.NPC_OFFSET)));
            buffer.enqueue(entity);

            Prefab beaconMark = Assets.getPrefab("BeaconMark").get();
            EntityStore entityStore = new EntityStore(beaconMark);
            entityStore.addComponent(new LocationComponent(new Vector3f(
                    0,
                    LASUtils.FLOATING_PLATFORM_HEIGHT_LEVEL + 3,
                    LASUtils.FLOATING_PLATFORM_POSITION.z + LASUtils.NPC_OFFSET)));
            buffer.enqueue(entityStore);
        }

    }

}
