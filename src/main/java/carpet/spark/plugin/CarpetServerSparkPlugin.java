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
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.lucko.spark.common.monitor.ping.PlayerPingProvider;
import me.lucko.spark.common.platform.MetadataProvider;
import me.lucko.spark.common.platform.PlatformInfo;
import me.lucko.spark.common.platform.serverconfig.ServerConfigProvider;
import me.lucko.spark.common.platform.world.WorldInfoProvider;
import me.lucko.spark.common.sampler.ThreadDumper;
import me.lucko.spark.common.tick.TickHook;
import me.lucko.spark.common.tick.TickReporter;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class CarpetServerSparkPlugin extends CarpetSparkPlugin implements Command<CommandSource>, SuggestionProvider<CommandSource> {

    // TISCM store singleton
    private static CarpetServerSparkPlugin plugin;

    public static CarpetServerSparkPlugin getInstance() {
        return plugin;
    }

    public static CarpetServerSparkPlugin register(CarpetSparkMod mod, MinecraftServer server) {
        plugin = new CarpetServerSparkPlugin(mod, server);
        plugin.enable();
        return plugin;
    }

    private final MinecraftServer server;
    private final ThreadDumper gameThreadDumper;

    // TISCM store hooks so their fabric api events can be simulated
    private final CarpetServerTickHook tickHook;
    private final CarpetServerTickReporter tickReporter;

    public CarpetServerSparkPlugin(CarpetSparkMod mod, MinecraftServer server) {
        super(mod);
        this.server = server;
        this.gameThreadDumper = new ThreadDumper.Specific(server.getServerThread());

        this.tickHook = new CarpetServerTickHook();
        this.tickReporter = new CarpetServerTickReporter();
    }
    public CarpetServerTickHook getTickHook() {
        return this.tickHook;
    }
    public CarpetServerTickReporter getTickReporter() {
        return this.tickReporter;
    }

    @Override
    public void enable() {
        super.enable();

        // register commands
        registerCommands(this.server.getCommandManager().getDispatcher());
    }

    public void registerCommands(CommandDispatcher<CommandSource> dispatcher) {
        registerCommands(dispatcher, this, this, "spark");
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        String[] args = processArgs(context, false, "/spark", "spark");
        if (args == null) {
            return 0;
        }

        ICommandSource source = context.getSource().getEntity() != null ? context.getSource().getEntity() : context.getSource().getServer();
        this.platform.executeCommand(new CarpetCommandSender(source, this), args);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        String[] args = processArgs(context, true, "/spark", "spark");
        if (args == null) {
            return Suggestions.empty();
        }

        return generateSuggestions(new CarpetCommandSender(context.getSource().asPlayer(), this), args, builder);
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

    @Override
    public void executeSync(Runnable task) {
        this.server.addScheduledTask(task);
    }

    @Override
    public ThreadDumper getDefaultThreadDumper() {
        return this.gameThreadDumper;
    }

    @Override
    public TickHook createTickHook() {
        return this.getTickHook();
    }

    @Override
    public TickReporter createTickReporter() {
        return this.getTickReporter();
    }

    @Override
    public PlayerPingProvider createPlayerPingProvider() {
        return new CarpetPlayerPingProvider(this.server);
    }

    @Override
    public ServerConfigProvider createServerConfigProvider() {
        // never try to think about exposing it
        return null;
    }

    @Override
    public MetadataProvider createExtraMetadataProvider() {
        // not that ez to be ported uwu
        return null;
    }

    @Override
    public WorldInfoProvider createWorldInfoProvider() {
        return new CarpetWorldInfoProvider.Server(this.server);
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
