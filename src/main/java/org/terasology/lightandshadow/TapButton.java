// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadow;

import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;

/**
 * Shows the statistics screen.
 */
@RegisterBindButton(id = "statistics", description = "Show statistics screen", category = "general", repeating = true)
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.G)
public class TapButton extends BindButtonEvent {
}
