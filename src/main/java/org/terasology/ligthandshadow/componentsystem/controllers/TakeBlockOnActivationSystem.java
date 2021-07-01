// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.lightandshadowresources.components.FlagComponent;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.lightandshadowresources.components.LASTeamComponent;
import org.terasology.lightandshadowresources.components.TakeBlockOnActivateComponent;
import org.terasology.ligthandshadow.componentsystem.events.GiveFlagEvent;

/**
 * System responsible for giving the flag only when a player from the opposing team tries to take it.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class TakeBlockOnActivationSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(TakeBlockOnActivationSystem.class);

    @ReceiveEvent(components = {TakeBlockOnActivateComponent.class, BlockComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        EntityRef flagTaker = event.getInstigator();

        // If the flag being taken is a red flag and the player is on the black team, let them take the flag
        if (!playerTeamMatchesFlagTeam(entity, flagTaker)) {
            flagTaker.send(new GiveFlagEvent(entity));
        }
    }

    private boolean playerTeamMatchesFlagTeam(EntityRef flag, EntityRef player) {
        FlagComponent flagComponent = flag.getComponent(FlagComponent.class);
        LASTeamComponent playerTeamComponent = player.getComponent(LASTeamComponent.class);
        return (flagComponent.team.equals(playerTeamComponent.team));
    }

}
