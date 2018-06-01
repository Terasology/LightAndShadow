/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.las.dialog.action;

import org.terasology.dialogs.action.PlayerAction;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.ligthandshadow.componentsystem.components.LASTeam;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.network.ClientComponent;

/**
 *
 */
public class SetTeamAction implements PlayerAction {

    private String team;

    public SetTeamAction(String team) {
        this.team = team;
    }

    @Override
    public void execute(EntityRef charEntity, EntityRef talkTo) {

        EntityRef controller = charEntity.getComponent(CharacterComponent.class).controller; // the client
        ClientComponent clientComponent = controller.getComponent(ClientComponent.class);
        EntityRef playerEntity = clientComponent.character;
        LASTeam playerTeam = playerEntity.getComponent(LASTeam.class);
        playerTeam.setTeam(team);
        //EntityRef clientInfo = clientComponent.clientInfo;
        //clientInfo.addComponent(new LASTeam(team));
    }

    public String getTeam() {
        return team;
    }

}
