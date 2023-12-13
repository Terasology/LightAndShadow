// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;

import java.util.List;

/**
 * Keeps track of enemies in the range of the entity.
 */
public class NearbyEnemiesComponent implements Component {

    public float searchRadius = 20f;

    public List<EntityRef> enemiesWithinRange;

    public EntityRef closestEnemy;

}