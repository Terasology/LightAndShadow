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

package org.terasology.las.platform;

import org.terasology.dialogs.components.DialogComponent;
import org.terasology.entitySystem.entity.EntityStore;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.NetworkComponent;
import org.terasology.utilities.Assets;
import org.terasology.world.generation.EntityBuffer;
import org.terasology.world.generation.EntityProviderPlugin;
import org.terasology.world.generation.Region;
import org.terasology.world.generator.plugin.RegisterPlugin;

/**
 *
 */
@RegisterPlugin
public class GuidanceNpcProvider implements EntityProviderPlugin {

    @Override
    public void process(Region region, EntityBuffer buffer) {
        if (region.getRegion().encompasses(0, 64, 24)) {
            Prefab dialog = Assets.getPrefab("LightAndShadow:TeamDialog").get();
            DialogComponent dialogComp = dialog.getComponent(DialogComponent.class);

            Prefab chooseFactionNpc = Assets.getPrefab("LightAndShadow:MagicFool").get();
            EntityStore entity = new EntityStore(chooseFactionNpc);
            entity.addComponent(new LocationComponent(new Vector3f(0, 61, 24)));
            entity.addComponent(dialogComp);
            entity.addComponent(new NetworkComponent());
            buffer.enqueue(entity);

            Prefab beaconMark = Assets.getPrefab("BeaconMark").get();
            EntityStore entityStore = new EntityStore(beaconMark);
            entityStore.addComponent(new LocationComponent(new Vector3f(0, 63, 24)));
            entityStore.addComponent(new NetworkComponent());
            buffer.enqueue(entityStore);
        }

    }

}
