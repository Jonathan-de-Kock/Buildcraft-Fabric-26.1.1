/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib.net;

import java.io.IOException;

import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public interface IPayloadReceiver {
    /**
     * @param player The player who sent the packet, or null if this is a client-side receive
     * @param buffer The packet data
     */
    void receivePayload(@Nullable ServerPlayer player, PacketBufferBC buffer) throws IOException;
}
