// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.las.platform;

import org.terasology.engine.world.block.BlockArea;
import org.terasology.engine.world.block.BlockAreac;
import org.terasology.engine.world.block.BlockRegionc;

public class FloatingPlatform {
    private final BlockAreac area;
    private final int baseHeight;
    private final BlockRegionc redTeleporterRegion;
    private final BlockRegionc blackTeleporterRegion;

    public FloatingPlatform(BlockAreac area, int baseHeight, BlockRegionc redTeleporterRegion, BlockRegionc blackTeleporterRegion) {
        this.area = new BlockArea(area);
        this.redTeleporterRegion = redTeleporterRegion;
        this.blackTeleporterRegion = blackTeleporterRegion;
        this.baseHeight = baseHeight;
    }

    public BlockAreac getArea() {
        return area;
    }

    public int getBaseHeight() {
        return baseHeight;
    }

    public BlockRegionc getRedTeleporterRegion() {
        return redTeleporterRegion;
    }

    public BlockRegionc getBlackTeleporterRegion() {
        return blackTeleporterRegion;
    }
}
