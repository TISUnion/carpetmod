package carpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public class TouchCommand extends AbstractCommand {
    public static final String NAME = "touch";
    private static final TouchCommand INSTANCE = new TouchCommand(NAME);

    private TouchCommand(String name) {
        super(name);
    }

    public static TouchCommand getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerCommand(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> literalargumentbuilder = literal(NAME).
                then(argument("x", integer()).
                        then(argument("y", integer())
                                .then(argument("z", integer()).executes((commandContext) -> {
                                    BlockPos pos = new BlockPos(commandContext.getArgument("x", int.class),
                                            commandContext.getArgument("y", int.class),
                                            commandContext.getArgument("z", int.class));
                                    commandContext.getSource().getWorld().getBlockState(pos);
                                    return 1;
                                }))));
        dispatcher.register(literalargumentbuilder);
    }
}
