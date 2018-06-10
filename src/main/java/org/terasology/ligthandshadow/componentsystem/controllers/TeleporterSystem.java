/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.ligthandshadow.componentsystem.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.ligthandshadow.componentsystem.components.LASTeam;
import org.terasology.ligthandshadow.componentsystem.components.SetTeamOnActivateComponent;
import org.terasology.logic.characters.CharacterTeleportEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.math.geom.Vector3f;

@RegisterSystem(RegisterMode.AUTHORITY)
public class TeleporterSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(TeleporterSystem.class);

    // The position near the team's base that player will be teleported to on choosing a team
    private static final Vector3f RED_TELEPORT_DESTINATION = new Vector3f(29, 12, 0);
    private static final Vector3f BLACK_TELEPORT_DESTINATION = new Vector3f(-29, 12, 0);

    @ReceiveEvent(components = {SetTeamOnActivateComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        LASTeam teleporterTeamComponent = entity.getComponent(LASTeam.class);
        EntityRef player = event.getInstigator();
        LASTeam playerTeamComponent = player.getComponent(LASTeam.class);

        /* Depending on which teleporter the player chooses, they are set to that team
        * and teleported to that base */
        if (teleporterTeamComponent.team.equals(teleporterTeamComponent.RED)) {
            playerTeamComponent.team = teleporterTeamComponent.team;
            player.send(new CharacterTeleportEvent(new Vector3f(RED_TELEPORT_DESTINATION)));
        }

        if (teleporterTeamComponent.team.equals(teleporterTeamComponent.BLACK)) {
            playerTeamComponent.team = teleporterTeamComponent.team;
            player.send(new CharacterTeleportEvent(new Vector3f(BLACK_TELEPORT_DESTINATION)));
        }
    }
}
