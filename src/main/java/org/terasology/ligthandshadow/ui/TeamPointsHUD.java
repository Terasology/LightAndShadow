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
package org.terasology.ligthandshadow.ui;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.ligthandshadow.logic.TeamDescriptionComponent;
import org.terasology.ligthandshadow.logic.TeamPointsComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.rendering.nui.layouts.FlowLayout;
import org.terasology.rendering.nui.layouts.RowLayoutHint;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.world.WorldComponent;

public class TeamPointsHUD extends CoreHudWidget {
    @Override
    public void initialise() {
        FlowLayout container = find("container", FlowLayout.class);

        PrefabManager prefabManager = CoreRegistry.get(PrefabManager.class);

        for (final Prefab teamPrefab : prefabManager.listPrefabs(TeamDescriptionComponent.class)) {
            final TeamDescriptionComponent teamDescriptionComponent = teamPrefab.getComponent(TeamDescriptionComponent.class);
            UILabel teamName = new UILabel();
            teamName.setText(teamDescriptionComponent.name);


            UILabel teamPoints = new UILabel();
            teamPoints.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    TeamPointsComponent teamPointsComponent = getWorldEntity().getComponent(TeamPointsComponent.class);
                    Integer teamPoints = 0;
                    if (teamPointsComponent != null) {
                        teamPoints = teamPointsComponent.getPoints(teamPrefab.getName());
                    }

                    return teamDescriptionComponent.name + ": " + teamPoints.toString();
                }
            });
            container.addWidget(teamPoints, new RowLayoutHint());
        }
    }

    private EntityRef getWorldEntity() {
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        for (EntityRef entityRef : entityManager.getEntitiesWith(WorldComponent.class)) {
            return entityRef;
        }
        return EntityRef.NULL;
    }
}
