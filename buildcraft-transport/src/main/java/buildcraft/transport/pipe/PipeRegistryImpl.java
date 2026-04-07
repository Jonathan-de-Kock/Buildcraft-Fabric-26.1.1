/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.transport.pipe;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import buildcraft.api.core.BCLog;
import buildcraft.api.transport.pipe.IPipeRegistry;
import buildcraft.api.transport.pipe.PipeDefinition;

/**
 * Simple map-based implementation of {@link IPipeRegistry}.
 */
public class PipeRegistryImpl implements IPipeRegistry {
    public static final PipeRegistryImpl INSTANCE = new PipeRegistryImpl();

    private final Map<Identifier, PipeDefinition> definitions = new HashMap<>();
    private final Map<PipeDefinition, Item> pipeItems = new IdentityHashMap<>();

    @Override
    public void registerPipe(PipeDefinition definition) {
        if (definition.identifier == null) {
            throw new IllegalArgumentException("PipeDefinition must have a non-null identifier!");
        }
        PipeDefinition existing = definitions.put(definition.identifier, definition);
        if (existing != null) {
            BCLog.logger.warn("Replaced pipe definition for {}", definition.identifier);
        }
    }

    @Override
    @Nullable
    public PipeDefinition getDefinition(Identifier identifier) {
        return definitions.get(identifier);
    }

    public void setItemForPipe(PipeDefinition definition, Item item) {
        pipeItems.put(definition, item);
    }

    @Nullable
    public Item getItemForPipe(PipeDefinition definition) {
        return pipeItems.get(definition);
    }

    public Map<Identifier, PipeDefinition> getAllDefinitions() {
        return definitions;
    }
}
