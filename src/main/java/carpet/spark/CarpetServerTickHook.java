/*
 * This file is part of spark.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package carpet.spark;

import me.lucko.spark.common.tick.AbstractTickHook;
import me.lucko.spark.common.tick.TickHook;

public class CarpetServerTickHook extends AbstractTickHook implements TickHook {
    protected boolean closed = false;

    @Override
    protected void onTick() {
        if (!this.closed) {
            super.onTick();
        }
    }

    @Override
    public void start() {
        // used for registering event callbacks in fabric
    }

    @Override
    public void close() {
        this.closed = true;
    }

    // fabric-api: ServerTickEvents.START_SERVER_TICK
    public void onStartServerTick() {
        this.onTick();
    }
}
