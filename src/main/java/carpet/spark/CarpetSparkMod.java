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

import carpet.spark.plugin.CarpetServerSparkPlugin;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CarpetSparkMod
{
    public static CarpetSparkMod mod;

    public static final String MOD_ID = "spark";
    public static final String VERSION = "1.10.13-TISCM";

    private Path configDirectory;
    private CarpetServerSparkPlugin activeServerPlugin = null;

    public void onInitialize() {
        CarpetSparkMod.mod = this;

        this.configDirectory = Paths.get("config/" + MOD_ID);
    }

    public String getVersion() {
        return VERSION;
    }

    public Path getConfigDirectory() {
        if (this.configDirectory == null) {
            throw new IllegalStateException("Config directory not set");
        }
        return this.configDirectory;
    }

    // Server hooks

    // fabric-api: ServerLifecycleEvents.SERVER_STARTING
    public void initializeServer(MinecraftServer server) {
        this.activeServerPlugin = CarpetServerSparkPlugin.register(this, server);
    }

    // ServerLifecycleEvents.SERVER_STOPPING
    public void onServerStopping(MinecraftServer stoppingServer) {
        if (this.activeServerPlugin != null) {
            this.activeServerPlugin.disable();
            this.activeServerPlugin = null;
        }
    }

    // no onServerCommandRegister
    // no need to register command again, spark wh
}
