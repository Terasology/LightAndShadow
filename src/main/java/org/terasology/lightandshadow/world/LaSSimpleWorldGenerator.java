// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadow.world;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.core.world.generator.facetProviders.DefaultFloraProvider;
import org.terasology.core.world.generator.facetProviders.DefaultTreeProvider;
import org.terasology.core.world.generator.facetProviders.SeaLevelProvider;
import org.terasology.core.world.generator.facetProviders.SimplexHumidityProvider;
import org.terasology.core.world.generator.facetProviders.SimplexSurfaceTemperatureProvider;
import org.terasology.core.world.generator.facetProviders.SurfaceToDensityProvider;
import org.terasology.core.world.generator.rasterizers.SolidRasterizer;
import org.terasology.core.world.generator.rasterizers.TreeRasterizer;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.spawner.Spawner;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.generation.BaseFacetedWorldGenerator;
import org.terasology.engine.world.generation.WorldBuilder;
import org.terasology.engine.world.generator.RegisterWorldGenerator;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.lightandshadow.world.bases.BaseProvider;
import org.terasology.lightandshadow.world.bases.BaseRasterizer;
import org.terasology.lightandshadow.world.platform.FloatingPlatformProvider;
import org.terasology.lightandshadow.world.platform.FloatingPlatformRasterizer;

@RegisterWorldGenerator(id = "lasWorld", displayName = "A World of Light And Shadow")
public class LaSSimpleWorldGenerator extends BaseFacetedWorldGenerator {
    @In
    private WorldGeneratorPluginLibrary worldGeneratorPluginLibrary;
    @In
    private BlockManager blockManager;

    private final Spawner spawner = new LaSSpawner();

    public LaSSimpleWorldGenerator(SimpleUri uri) {
        super(uri);
    }

    @Override
    public Vector3fc getSpawnPosition(EntityRef entity) {
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
