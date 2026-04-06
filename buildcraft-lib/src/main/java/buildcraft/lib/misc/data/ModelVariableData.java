/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import java.util.Arrays;
import java.util.List;

/** Tracks model variable data for tickable model nodes.
 * This is a simplified port -- the expression node system (ITickableNode, NodeStateful, NodeUpdatable)
 * will need to be ported separately. For now this provides the structural skeleton. */
public class ModelVariableData {
    private static int currentBakeId = 0;

    private int bakeId = -1;
    private Object[] tickableNodes; // Will be ITickableNode[] once the expression system is ported

    public static void onModelBake() {
        currentBakeId++;
    }

    public boolean hasNoNodes() {
        return tickableNodes == null;
    }

    public void setNodes(Object[] nodes) {
        bakeId = currentBakeId;
        tickableNodes = nodes;
    }

    public void addNodes(Object[] additional) {
        int originalLength = tickableNodes.length;
        tickableNodes = Arrays.copyOf(tickableNodes, originalLength + additional.length);
        System.arraycopy(additional, 0, tickableNodes, originalLength, additional.length);
    }

    private boolean checkModelBake() {
        if (tickableNodes == null) {
            return false;
        }
        if (currentBakeId == bakeId) {
            return true;
        }
        tickableNodes = null;
        return false;
    }

    public void refresh() {
        if (checkModelBake()) {
            // TODO: call refresh() on each ITickableNode once the expression system is ported
        }
    }

    public void tick() {
        if (checkModelBake()) {
            // TODO: call tick() on each ITickableNode once the expression system is ported
        }
    }

    public void addDebugInfo(List<String> to) {
        // TODO: implement once the expression system is ported
    }
}
