// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.las.yinyang;

import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.SparseObjectFacet3D;

public class YinYangFacet extends SparseObjectFacet3D<YinYang> {
    public YinYangFacet(BlockRegion targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
