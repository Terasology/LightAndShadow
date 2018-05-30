/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.las;

import org.terasology.cities.BlockTheme;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.spawner.Spawner;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.world.block.BlockManager;
import org.terasology.world.generation.BaseFacetedWorldGenerator;
import org.terasology.world.generator.RegisterWorldGenerator;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.las.platform.FloatingPlatformProvider;
import org.terasology.las.platform.FloatingPlatformRasterizer;
import org.terasology.logic.spawner.Spawner;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.generation.WorldBuilder;
import org.terasology.core.world.generator.facetProviders.SeaLevelProvider;
import org.terasology.las.bases.BaseProvider;
import org.terasology.las.bases.BaseRasterizer;
import org.terasology.cities.DefaultBlockType;

@RegisterWorldGenerator(id = "LaSSimpleWorld", displayName = "Light and Shadow (Simple)")
public class LaSSimpleWorldGenerator extends BaseFacetedWorldGenerator {
    @In
    private WorldGeneratorPluginLibrary worldGeneratorPluginLibrary;

    @In
    private BlockManager blockManager;

    public LaSSimpleWorldGenerator(SimpleUri uri) {
        super(uri);
    }

    private final Spawner spawner = new LaSSpawner();

    private BlockTheme theme;

    @Override
    public Vector3f getSpawnPosition(EntityRef entity) {
        Vector3f pos = spawner.getSpawnPosition(getWorld(), entity);
        return pos != null ? pos : super.getSpawnPosition(entity);
    }

    @Override
    protected WorldBuilder createWorld() {
        int seaLevel = 0;

        theme = BlockTheme.builder(blockManager)
                .register(DefaultBlockType.BUILDING_FLOOR, "Cities:stonawall1dark")
                .register(DefaultBlockType.ROOF_FLAT, "Cities:rooftiles2")
                // -- requires Fences module
                .registerFamily(DefaultBlockType.FENCE, "Fences:Fence")
                .build();

        return new WorldBuilder(worldGeneratorPluginLibrary)
                .addProvider(new LaSSurfaceProvider())
                .addProvider(new SeaLevelProvider(0))
                .addProvider(new BaseProvider())
                .addProvider(new FloatingPlatformProvider())
                .addRasterizer(new LaSSimpleWorldRasterizer())
                .addRasterizer(new FloatingPlatformRasterizer())
                .addRasterizer(new BaseRasterizer());
    }
}
