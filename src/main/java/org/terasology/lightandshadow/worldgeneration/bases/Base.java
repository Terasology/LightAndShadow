// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadow.worldgeneration.bases;

import org.terasology.engine.world.block.BlockRegion;

public class Base {
    private BlockRegion area;
    private BlockRegion flagArea;

    public Base(BlockRegion baseRegion, BlockRegion flagRegion) {
        this.area = baseRegion;
        this.flagArea = flagRegion;
    }

    public BlockRegion getArea() {
        return area;
    }

    public BlockRegion getFlagArea() {
        return flagArea;
    }
}
