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
package org.terasology.ligthandshadow.logic;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.registry.In;
import org.terasology.world.WorldComponent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class TeamPointsAuthoritySystem extends BaseComponentSystem {
    @In
    EntityManager entityManager;
    @In
    PrefabManager prefabManager;


    @ReceiveEvent
    public void onScorePoints(ScoreTeamPointsEvent scoreTeamPointsEvent, EntityRef worldEntity) {
        if (worldEntity.equals(getWorldEntity())) {
            TeamPointsComponent teamPointsComponent = worldEntity.getComponent(TeamPointsComponent.class);
            if (teamPointsComponent == null) {
                teamPointsComponent = new TeamPointsComponent();
            }

            teamPointsComponent.addPoints(scoreTeamPointsEvent.team, scoreTeamPointsEvent.points);

            if (worldEntity.hasComponent(TeamPointsComponent.class)) {
                worldEntity.saveComponent(teamPointsComponent);
            } else {
                worldEntity.addComponent(teamPointsComponent);
            }
        }
    }

    private EntityRef getWorldEntity() {
        for (EntityRef entityRef : entityManager.getEntitiesWith(WorldComponent.class)) {
            return entityRef;
        }
        return EntityRef.NULL;
    }

    @Command(shortDescription = "Gives points to a particular team")
    public String giveTeamPoints(@CommandParam("team prefab") String team, @CommandParam("points") int points) {
        Prefab teamPrefab = prefabManager.getPrefab(team);
        if (teamPrefab == null) {
            return "Team not found";
        }

        getWorldEntity().send(new ScoreTeamPointsEvent(teamPrefab.getName(), points));

        TeamDescriptionComponent teamDescriptionComponent = teamPrefab.getComponent(TeamDescriptionComponent.class);
        if (teamDescriptionComponent != null) {
            return teamDescriptionComponent.name + " given " + points + " points";
        } else {
            return team + " given " + points + " points";
        }
    }
}
