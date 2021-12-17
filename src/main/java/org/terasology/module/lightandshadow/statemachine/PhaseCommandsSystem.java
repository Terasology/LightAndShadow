// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.lightandshadow.statemachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.engine.logic.console.commandSystem.annotations.Sender;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.registry.In;
import org.terasology.module.inventory.components.ItemCommands;

public class PhaseCommandsSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(ItemCommands.class);

    @In
    PhaseSystem phaseSystem;

    @Command(shortDescription = "Forces a phase transition to a specified target phase",
            helpText = "Puts the Light & Shadow World into a desired phase",
            runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String forcePhase(
            @Sender EntityRef client,
            @CommandParam("targetPhase") PhaseSystem.Phase targetPhase) {

        switch(targetPhase) {
            case IDLE:
            case PREGAME:
            case COUNTDOWN:
            case GAME:
            case POSTGAME:
                break;
            default:
                StringBuilder errorMsg = new StringBuilder(targetPhase + " is not a valid phase, please choose one of the following:");
                for (PhaseSystem.Phase validPhase : PhaseSystem.Phase.values()) {
                    errorMsg.append(" ").append(validPhase.toString());
                }
                logger.error(errorMsg.toString());
                return errorMsg.toString();
        }

        PhaseSystem.Phase currentPhase = phaseSystem.getCurrentPhase();
        phaseSystem.transitionPhase(currentPhase, targetPhase);
        return "Initiated phase transition from " + currentPhase + " to " + targetPhase;
    }
}
