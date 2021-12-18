// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.lightandshadow.phases.authority;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.console.commandSystem.adapter.ParameterAdapterManager;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.engine.logic.console.commandSystem.annotations.Sender;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.registry.In;
import org.terasology.module.inventory.components.ItemCommands;
import org.terasology.module.lightandshadow.phases.Phase;
import org.terasology.module.lightandshadow.phases.PhaseParameterAdapter;

@RegisterSystem(RegisterMode.ALWAYS)
public class PhaseCommandsSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(ItemCommands.class);

    @In
    PhaseSystem phaseSystem;

    @In
    ParameterAdapterManager parameterAdapterManager;

    @Override
    public void initialise() {
        // globally register the adapter for `Phase` values. Required for `forcePhase` command.
        parameterAdapterManager.registerAdapter(Phase.class, new PhaseParameterAdapter());
    }

    @Command(shortDescription = "Forces a phase transition to a specified target phase",
            helpText = "Puts the Light & Shadow World into a desired phase",
            runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String forcePhase(
            @Sender EntityRef client,
            @CommandParam("targetPhase") Phase targetPhase) {

        switch (targetPhase) {
            case IDLE:
            case PRE_GAME:
            case COUNTDOWN:
            case IN_GAME:
            case POST_GAME:
                break;
            default:
                // this case can only occur if we add a new phase and (deliberately or not) don't add it here.
                // TODO(skaldarnar): I'd like to remove this altogether, as I think this is just maintenance overhead and a potential
                //                   pitfall in the case we're adding a new phase. Less code is better code.
                StringBuilder errorMsg = new StringBuilder(targetPhase + " is not a valid phase, please choose one of the following:");
                for (Phase validPhase : Phase.values()) {
                    errorMsg.append(" ").append(validPhase.toString());
                }
                logger.error(errorMsg.toString());
                return errorMsg.toString();
        }

        Phase currentPhase = phaseSystem.getCurrentPhase();
        phaseSystem.transitionPhase(currentPhase, targetPhase);
        return "Initiated phase transition from " + currentPhase + " to " + targetPhase;
    }
}
