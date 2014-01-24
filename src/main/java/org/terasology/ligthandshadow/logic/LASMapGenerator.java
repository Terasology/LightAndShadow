package org.terasology.ligthandshadow.logic;

import org.terasology.cities.CityTerrainGenerator;
import org.terasology.cities.CityWorldConfig;
import org.terasology.cities.FloraGeneratorFast;
import org.terasology.cities.HeightMapTerrainGenerator;
import org.terasology.cities.terrain.NoiseHeightMap;
import org.terasology.core.world.generator.AbstractBaseWorldGenerator;
import org.terasology.engine.SimpleUri;
import org.terasology.world.generator.RegisterWorldGenerator;

/**
 * @author synopia
 */
@RegisterWorldGenerator(id = "lasmap", displayName = "Light and Shadow World")
public class LASMapGenerator extends AbstractBaseWorldGenerator {

    private NoiseHeightMap heightMap;

    /**
     * @param uri the uri
     */
    public LASMapGenerator(SimpleUri uri) {
        super(uri);
    }

    @Override
    public void initialize() {

        // TODO: this should come from elsewhere
        CityWorldConfig config = new CityWorldConfig();

        heightMap = new NoiseHeightMap();

        register(new HeightMapTerrainGenerator(heightMap, config));
//        register(new BoundaryGenerator(heightMap));
        register(new CityTerrainGenerator(heightMap, config));
        register(new FloraGeneratorFast(heightMap));
    }

    @Override
    public void setWorldSeed(String seed) {
        if (seed == null) {
            return;
        }

        if (heightMap == null) {
            heightMap = new NoiseHeightMap();
        }

        heightMap.setSeed(seed);

        super.setWorldSeed(seed);
    }
}
