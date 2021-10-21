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

package carpet.spark.plugin;

import carpet.settings.SettingsManager;
import carpet.spark.*;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.lucko.spark.common.platform.PlatformInfo;
import me.lucko.spark.common.tick.TickHook;
import me.lucko.spark.common.tick.TickReporter;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class CarpetServerSparkPlugin extends CarpetSparkPlugin implements Command<CommandSource>, SuggestionProvider<CommandSource> {

    private static CarpetServerSparkPlugin plugin;

    public static CarpetServerSparkPlugin getInstance() {
        return plugin;
    }

    // fabric-api: ServerLifecycleEvents.SERVER_STARTING
    public static void register(CarpetSparkMod mod, MinecraftServer server) {
        plugin = new CarpetServerSparkPlugin(mod, server);
        plugin.enable();

        // register commands
        registerCommands(server.getCommandManager().getDispatcher(), plugin, plugin, "spark");
    }

    // ServerLifecycleEvents.SERVER_STOPPING
    public static void onServerStopping(MinecraftServer stoppingServer) {
        if (stoppingServer == plugin.server) {
            plugin.disable();
        }
    }

    private final MinecraftServer server;
    private final CarpetServerTickHook tickHook;
    private final CarpetServerTickReporter tickReporter;

    public CarpetServerSparkPlugin(CarpetSparkMod mod, MinecraftServer server) {
        super(mod);
        this.server = server;
        this.tickHook = new CarpetServerTickHook();
        this.tickReporter =  new CarpetServerTickReporter();
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        String[] args = processArgs(context, false);
        if (args == null) {
            return 0;
        }

        this.threadDumper.ensureSetup();
        ICommandSource source = context.getSource().getEntity() != null ? context.getSource().getEntity() : context.getSource().getServer();
        this.platform.executeCommand(new CarpetCommandSender(source, this), args);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        String[] args = processArgs(context, true);
        if (args == null) {
            return Suggestions.empty();
        }

        return generateSuggestions(new CarpetCommandSender(context.getSource().asPlayer(), this), args, builder);
    }

    private static String[] processArgs(CommandContext<CommandSource> context, boolean tabComplete) {
        String[] split = context.getInput().split(" ", tabComplete ? -1 : 0);
        if (split.length == 0 || !split[0].equals("/spark") && !split[0].equals("spark")) {
            return null;
        }

        return Arrays.copyOfRange(split, 1, split.length);
    }

    @Override
    public boolean hasPermission(ICommandSource sender, String permission) {
        if (sender instanceof EntityPlayer) {
            return SettingsManager.canUseCommand(((EntityPlayer) sender).getCommandSource(), "4");
        } else {
            return true;
        }
    }

    @Override
    public Stream<CarpetCommandSender> getCommandSenders() {
        return Stream.concat(
                this.server.getPlayerList().getPlayers().stream(),
                Stream.of(this.server)
        ).map(sender -> new CarpetCommandSender(sender, this));
    }

    public CarpetServerTickHook getTickHook() {
        return this.tickHook;
    }

    @Override
    public TickHook createTickHook() {
        return this.getTickHook();
    }

    public CarpetServerTickReporter getTickReporter() {
        return this.tickReporter;
    }

    @Override
    public TickReporter createTickReporter() {
        return this.getTickReporter();
    }

    @Override
    public PlatformInfo getPlatformInfo() {
        return new CarpetPlatformInfo(PlatformInfo.Type.SERVER);
    }

    @Override
    public String getCommandName() {
        return "spark";
    }
}
