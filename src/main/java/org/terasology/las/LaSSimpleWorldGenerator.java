// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.las;

import org.terasology.coreworlds.generator.facetProviders.DefaultFloraProvider;
import org.terasology.coreworlds.generator.facetProviders.DefaultTreeProvider;
import org.terasology.coreworlds.generator.facetProviders.SeaLevelProvider;
import org.terasology.coreworlds.generator.facetProviders.SimplexHumidityProvider;
import org.terasology.coreworlds.generator.facetProviders.SimplexSurfaceTemperatureProvider;
import org.terasology.coreworlds.generator.facetProviders.SurfaceToDensityProvider;
import org.terasology.coreworlds.generator.rasterizers.SolidRasterizer;
import org.terasology.coreworlds.generator.rasterizers.TreeRasterizer;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.spawner.Spawner;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.generation.BaseFacetedWorldGenerator;
import org.terasology.engine.world.generation.WorldBuilder;
import org.terasology.engine.world.generator.RegisterWorldGenerator;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.las.bases.BaseProvider;
import org.terasology.las.bases.BaseRasterizer;
import org.terasology.las.platform.FloatingPlatformProvider;
import org.terasology.las.platform.FloatingPlatformRasterizer;
import org.terasology.math.geom.Vector3f;

@RegisterWorldGenerator(id = "lasWorld", displayName = "A World of Light And Shadow")
public class LaSSimpleWorldGenerator extends BaseFacetedWorldGenerator {
    private final Spawner spawner = new LaSSpawner();
    @In
    private WorldGeneratorPluginLibrary worldGeneratorPluginLibrary;
    @In
    private BlockManager blockManager;

    public LaSSimpleWorldGenerator(SimpleUri uri) {
        super(uri);
    }

    @Override
    public Vector3f getSpawnPosition(EntityRef entity) {
        Vector3f pos = spawner.getSpawnPosition(getWorld(), entity);
        return pos != null ? pos : super.getSpawnPosition(entity);
    }

    @Override
    protected WorldBuilder createWorld() {
        int seaLevel = 0;

        return new WorldBuilder(worldGeneratorPluginLibrary)
                .addProvider(new LaSSurfaceProvider())
                .addProvider(new PlayAreaProvider())
                .addProvider(new SeaLevelProvider(seaLevel))
                .addProvider(new SimplexHumidityProvider())
                .addProvider(new SimplexSurfaceTemperatureProvider())
                .addProvider(new MountainsProvider())
                .addProvider(new LaSBiomeProvider())
                .addProvider(new SurfaceToDensityProvider())
                .addProvider(new DefaultFloraProvider())
                .addProvider(new DefaultTreeProvider())
                .addProvider(new BaseProvider())
                .addProvider(new FloatingPlatformProvider())
                .addPlugins()
                .addRasterizer(new SolidRasterizer())
                .addRasterizer(new FloatingPlatformRasterizer())
                .addRasterizer(new BaseRasterizer())
                .addRasterizer(new LaSFloraRasterizer())
                .addRasterizer(new TreeRasterizer());
    }
}
