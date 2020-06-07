// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.las.yinyang;

import org.terasology.math.Region3i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.base.SparseObjectFacet3D;

public class YinYangFacet extends SparseObjectFacet3D<YinYang> {
    public YinYangFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
