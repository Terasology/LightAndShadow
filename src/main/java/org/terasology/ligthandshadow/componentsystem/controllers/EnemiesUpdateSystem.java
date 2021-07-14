// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.controllers;

import com.google.common.collect.Lists;
import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.characters.AliveCharacterComponent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.ligthandshadow.componentsystem.components.LASTeamComponent;
import org.terasology.ligthandshadow.componentsystem.components.NearbyEnemiesComponent;
import org.terasology.ligthandshadow.componentsystem.components.PlayerStatisticsComponent;

/**
 * Tracks nearby enemies much like {@link org.terasology.behaviors.system.FindNearbyPlayersSystem}, and stores
 * the results in the required entity {@link NearbyEnemiesComponent}.
 */
@RegisterSystem(value = RegisterMode.AUTHORITY)
public class EnemiesUpdateSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private static final float ENEMY_CHECK_DELAY = 5;

    private float counter;

    @In
    private EntityManager entityManager;

    @Override
    public void update(float delta) {
        counter += delta;

        if (counter < ENEMY_CHECK_DELAY) {
            return;
        }

        counter = 0;

        for (EntityRef entity : entityManager.getEntitiesWith(NearbyEnemiesComponent.class)) {
            checkForEnemies(entity);
        }
    }

    /**
     * Checks for players of the oppsite team that are near the entity.
     * <p>
     * Results are stored in {@link NearbyEnemiesComponent}.
     *
     * @param entity The entity to check enemies for.
     */
    private void checkForEnemies(EntityRef entity) {
        if (!entity.hasComponent(NearbyEnemiesComponent.class) || !entity.hasComponent(LASTeamComponent.class)) {
            return;
        }

        NearbyEnemiesComponent enemiesComponent = entity.getComponent(NearbyEnemiesComponent.class);
        LASTeamComponent teamComponent = entity.getComponent(LASTeamComponent.class);

        enemiesComponent.enemiesWithinRange = Lists.newArrayList();
        enemiesComponent.closestEnemy = EntityRef.NULL;

        float minDistance = Float.MAX_VALUE;
        Vector3f actorPosition = entity.getComponent(LocationComponent.class).getWorldPosition(new Vector3f());

        for (EntityRef otherEntity : entityManager.getEntitiesWith(AliveCharacterComponent.class)) {
            if (!otherEntity.hasComponent(PlayerStatisticsComponent.class) || otherEntity.equals(entity)) {
                continue;
            }

            LASTeamComponent otherTeamComponent = otherEntity.getComponent(LASTeamComponent.class);
            if (otherTeamComponent.team.equals(teamComponent.team)) {
                continue;
            }

            LocationComponent otherLocationComponent = otherEntity.getComponent(LocationComponent.class);
            float distanceApart = otherLocationComponent.getWorldPosition(new Vector3f()).distanceSquared(actorPosition);

            if (distanceApart > enemiesComponent.searchRadius * enemiesComponent.searchRadius) {
                continue;
            }

            if (distanceApart < minDistance) {
                enemiesComponent.closestEnemy = otherEntity;
                minDistance = distanceApart;
            }

            enemiesComponent.enemiesWithinRange.add(otherEntity);
        }

        entity.saveComponent(enemiesComponent);
    }
}
