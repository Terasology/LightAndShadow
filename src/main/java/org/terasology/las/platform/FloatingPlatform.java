/*
 * Copyright 2015 MovingBlocks
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
        this.area= new BlockArea(area);
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
