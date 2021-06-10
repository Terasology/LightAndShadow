// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.lightandshadow.logic;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.world.block.family.BlockFamily;

/**
 * This is the component class for playing cards, which are constructed of a top and a bottom block.
 */
public class CardComponent implements Component {
    public BlockFamily topBlockFamily;
    public BlockFamily bottomBlockFamily;
    public Prefab cardBlockPrefab;
}
