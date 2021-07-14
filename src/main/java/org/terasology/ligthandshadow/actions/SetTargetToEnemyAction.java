// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.actions;

import org.terasology.behaviors.components.FollowComponent;
import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.ligthandshadow.componentsystem.components.NearbyEnemiesComponent;

/**
 * Action which sets this agent's move target to the nearest player from the opponent team, as
 * defined in {@link NearbyEnemiesComponent}.
 */
@BehaviorAction(name = "set_target_to_enemy")
public class SetTargetToEnemyAction extends BaseAction {

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        if (!actor.hasComponent(NearbyEnemiesComponent.class)) {
            return BehaviorState.SUCCESS;
        }

        NearbyEnemiesComponent enemiesComponent = actor.getComponent(NearbyEnemiesComponent.class);
        FollowComponent followComponent = new FollowComponent();

        followComponent.entityToFollow = enemiesComponent.closestEnemy;
        actor.getEntity().addComponent(followComponent);

        return BehaviorState.SUCCESS;
    }

}