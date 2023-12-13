// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.lightandshadow.phases;

import org.terasology.engine.logic.console.commandSystem.adapter.ParameterAdapter;
import org.terasology.engine.logic.console.commandSystem.adapter.ParameterAdapterManager;

/**
 * Adapter to allow direct parsing of {@link Phase} arguments in the in-game console.
 * <p>
 * If this adapter is registered with the {@link ParameterAdapterManager}, a command can accept a {@link Phase} as input parameter without
 * the need to manually parse it.
 * <pre>{@code
 *      public String myPhaseCommand(
 *             @Sender EntityRef client,
 *             @CommandParam("phase") Phase phase) { ... }
 * }</pre>
 *
 * This adapter can parse exact matches that conform to {@link Phase#valueOf(String)}.
 * It is case-insensitive, e.g., it compensates lower-case characters.
 */
public class PhaseParameterAdapter implements ParameterAdapter<Phase> {

    @Override
    public Phase parse(String raw) {
        // TODO: Make the user aware of the valid values. Needs some adjustments in CommandParameter.
        //       "Unknown phase '<raw>'. Valid values are: <Phase.values()>"
        return Phase.valueOf(raw.toUpperCase());
    }

    @Override
    public String convertToString(Phase value) {
        return value.toString();
    }
}
