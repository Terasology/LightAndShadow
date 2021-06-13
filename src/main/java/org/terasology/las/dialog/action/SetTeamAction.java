// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.las.dialog.action;

import org.terasology.dialogs.action.PlayerAction;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.network.ClientComponent;
import org.terasology.lightandshadowresources.components.LASTeamComponent;

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
        EntityRef clientInfo = clientComponent.clientInfo;

        clientInfo.addComponent(new LASTeamComponent(team));
    }

    public String getTeam() {
        return team;
    }

}
