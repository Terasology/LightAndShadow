// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.las.bases;

import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.SparseObjectFacet3D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class BaseFacet extends SparseObjectFacet3D<Base> {
    private final Collection<Base> bases = new ArrayList<>();

    public BaseFacet(BlockRegion targetRegion, Border3D border) {
        super(targetRegion, border);
    }

    public void add(Base base) {
        bases.add(base);
    }

    public Collection<Base> getBases() {
        return Collections.unmodifiableCollection(bases);
    }
}
