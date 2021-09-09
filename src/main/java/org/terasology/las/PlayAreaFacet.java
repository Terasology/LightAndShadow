// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.las;

import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.BaseBooleanFieldFacet2D;

public class PlayAreaFacet extends BaseBooleanFieldFacet2D {
    public PlayAreaFacet(BlockRegion targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
