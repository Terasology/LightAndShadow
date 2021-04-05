/*
 * Copyright 2014 MovingBlocks
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

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.joml.Vector3i;
import org.terasology.biomesAPI.Biome;
import org.terasology.core.world.CoreBiome;
import org.terasology.core.world.generator.facetProviders.PositionFilters;
import org.terasology.core.world.generator.facetProviders.SurfaceObjectProvider;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.core.world.generator.facets.TreeFacet;
import org.terasology.core.world.generator.trees.TreeGenerator;
import org.terasology.core.world.generator.trees.Trees;
import org.terasology.engine.entitySystem.Component;
import org.terasology.nui.properties.Range;
import org.terasology.engine.utilities.procedural.Noise;
import org.terasology.engine.utilities.procedural.WhiteNoise;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.ConfigurableFacetProvider;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetBorder;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.engine.world.generation.facets.SeaLevelFacet;
import org.terasology.engine.world.generation.facets.SurfacesFacet;

import java.util.List;

/**
 * Determines where trees can be placed.  Will put trees one block above the surface.
 */
@Produces(TreeFacet.class)
@Requires({
        @Facet(value = SeaLevelFacet.class, border = @FacetBorder(sides = Trees.MAXRADIUS)),
        @Facet(value = SurfacesFacet.class, border = @FacetBorder(sides = Trees.MAXRADIUS + 1, bottom = Trees.MAXHEIGHT + 1)),
        @Facet(value = BiomeFacet.class, border = @FacetBorder(sides = Trees.MAXRADIUS))
})
public class LaSTreeProvider extends SurfaceObjectProvider<Biome, TreeGenerator> implements ConfigurableFacetProvider {

    private Noise densityNoiseGen;
    private Configuration configuration = new Configuration();

    public LaSTreeProvider() {
        register(CoreBiome.MOUNTAINS, Trees.oakTree(), 0.04f);
        register(CoreBiome.MOUNTAINS, Trees.pineTree(), 0.06f);

        register(CoreBiome.FOREST, Trees.oakTree(), 0.25f);
        register(CoreBiome.FOREST, Trees.oakVariationTree(), 0.25f);
        register(CoreBiome.FOREST, Trees.redTree(), 0.05f);
        register(CoreBiome.FOREST, Trees.pineTree(), 0.10f);
        register(CoreBiome.FOREST, Trees.birchTree(), 0.10f);

        register(CoreBiome.SNOW, Trees.birchTree(), 0.02f);
        register(CoreBiome.SNOW, Trees.pineTree(), 0.10f);

        register(CoreBiome.PLAINS, Trees.redTree(), 0.01f);
        register(CoreBiome.PLAINS, Trees.birchTree(), 0.03f);
        register(CoreBiome.PLAINS, Trees.oakTree(), 0.01f);

        register(CoreBiome.DESERT, Trees.cactus(), 0.04f);
    }

    /**
     * @param configuration the default configuration to use
     */
    public LaSTreeProvider(Configuration configuration) {
        this();
        this.configuration = configuration;
    }

    @Override
    public void setSeed(long seed) {
        super.setSeed(seed);

        densityNoiseGen = new WhiteNoise(seed);
    }

    @Override
    public void process(GeneratingRegion region) {
        SurfacesFacet surfaces = region.getRegionFacet(SurfacesFacet.class);
        BiomeFacet biome = region.getRegionFacet(BiomeFacet.class);

        List<Predicate<Vector3i>> filters = getFilters(region);

        Border3D borderForTreeFacet = region.getBorderForFacet(TreeFacet.class);
        TreeFacet facet = new TreeFacet(region.getRegion(), borderForTreeFacet.extendBy(0, Trees.MAXHEIGHT, Trees.MAXRADIUS));

        populateFacet(facet, surfaces, biome, filters);

        region.setRegionFacet(TreeFacet.class, facet);
    }

    protected List<Predicate<Vector3i>> getFilters(GeneratingRegion region) {
        List<Predicate<Vector3i>> filters = Lists.newArrayList();

        SeaLevelFacet seaLevel = region.getRegionFacet(SeaLevelFacet.class);
        filters.add(PositionFilters.minHeight(seaLevel.getSeaLevel()));

        filters.add(PositionFilters.probability(densityNoiseGen, configuration.density * 0.05f));

        SurfacesFacet surface = region.getRegionFacet(SurfacesFacet.class);
        filters.add(PositionFilters.flatness(surface, 1, 0));
        filters.add(LaSExtraPositionFilters.notOnBase(surface));

        return filters;
    }

    @Override
    public String getConfigurationName() {
        return "Trees";
    }

    @Override
    public Component getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(Component configuration) {
        this.configuration = (Configuration) configuration;
    }

    public static class Configuration implements Component {
        @Range(min = 0, max = 1.0f, increment = 0.05f, precision = 2, description = "Define the overall tree density")
        public float density = 0.2f;
    }
}
