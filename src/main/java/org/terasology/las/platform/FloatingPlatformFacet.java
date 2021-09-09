// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.las.platform;

import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.BaseFacet2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 *
 */
public class FloatingPlatformFacet extends BaseFacet2D {

    private final Collection<FloatingPlatform> platforms = new ArrayList<>();

    public FloatingPlatformFacet(BlockRegion targetRegion, Border3D border) {
        super(targetRegion, border);
    }

    /**
     * @param platform the platform to add
     */
    public void add(FloatingPlatform platform) {
        platforms.add(platform);
    }

    /**
     * @return an unmodifiable view
     */
    public Collection<FloatingPlatform> getPlatforms() {
        return Collections.unmodifiableCollection(platforms);
    }

}
