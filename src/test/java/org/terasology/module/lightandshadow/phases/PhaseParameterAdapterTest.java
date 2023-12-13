// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.lightandshadow.phases;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PhaseParameterAdapterTest {

    static PhaseParameterAdapter adapter;

    @BeforeAll
    static void setup() {
        // the adapter is a stateless component and can therefore safely be reused.
        adapter = new PhaseParameterAdapter();
    }

    @Test
    void testInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> adapter.parse("foobar"));
    }

    @ParameterizedTest
    @EnumSource(Phase.class)
    void testExactInput(Phase phase) {
        assertEquals(phase, adapter.parse(phase.toString()));
    }

    @ParameterizedTest
    @EnumSource(Phase.class)
    void testLowercaseInput(Phase phase) {
        assertEquals(phase, adapter.parse(phase.toString().toLowerCase()));
    }
}
