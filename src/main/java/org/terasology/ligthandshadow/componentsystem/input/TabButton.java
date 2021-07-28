// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.input;

import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;

@RegisterBindButton(id = "statistics", description = "Show statistics screen", category = "general")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.V)
public class TabButton extends BindButtonEvent {
}
