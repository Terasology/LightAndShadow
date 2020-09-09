// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.las;

import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.BaseBooleanFieldFacet2D;

public class PlayAreaFacet extends BaseBooleanFieldFacet2D {
    public PlayAreaFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
