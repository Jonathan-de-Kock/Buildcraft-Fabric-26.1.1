/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.factory;

/**
 * Factory module standalone items.
 * Currently no standalone items -- all factory items are block items registered via BCFactoryBlocks.
 */
public final class BCFactoryItems {
    private BCFactoryItems() {}

    public static void register() {
        // No standalone items yet. Block items are registered automatically in BCFactoryBlocks.
    }
}
