package net.ramixin.caustics;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.ramixin.caustics.items.components.Frequency;
import net.ramixin.caustics.nodes.CrystalNode;
import net.ramixin.caustics.nodes.core.CrystalNetwork;

import java.util.Optional;
import java.util.Set;

public class ModCommands {

    @SuppressWarnings("unused") // selection & buildContext
    public static void onInitialize(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection selection) {

        RootCommandNode<CommandSourceStack> root = dispatcher.getRoot();
        CommandNode<CommandSourceStack> caustics = Commands.literal("caustics").build();

        caustics.addChild(createPrint());
        caustics.addChild(nuke());
        caustics.addChild(testVisibility());
        caustics.addChild(createFrequency());
        caustics.addChild(depositPos());

        root.addChild(caustics);
    }

    private static CommandNode<CommandSourceStack> printNodes() {
        return Commands.literal("nodes").executes(ctx -> {
            CrystalNetwork.get(ctx.getSource().getLevel()).printNodes();
            return 0;
        }).build();
    }

    private static CommandNode<CommandSourceStack> nuke() {
        return Commands.literal("nuke").executes(ctx -> {
            CrystalNetwork.get(ctx.getSource().getLevel()).nuke();
            return 0;
        }).build();
    }

    private static CommandNode<CommandSourceStack> testVisibility() {
        return Commands.literal("testvisibility").then(Commands.argument("pos", BlockPosArgument.blockPos())
                .executes(ctx -> {
                    BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                    CrystalNetwork network = CrystalNetwork.get(ctx.getSource().getLevel());
                    Optional<CrystalNode> maybeNode = network.getNodeAt(pos);
                    if(maybeNode.isEmpty()) {
                        ctx.getSource().sendFailure(Component.literal("No node at " + pos));
                        return 1;
                    }
                    CrystalNode node = maybeNode.get();
                    boolean visible = node.visibleClusterAt(pos);
                    if(visible)
                        ctx.getSource().sendSuccess(() -> Component.literal("Node at " + pos + " is visible"), false);
                    else
                        ctx.getSource().sendSuccess(() -> Component.literal("Node at " + pos + " is not visible, or isn't being tracked"), false);
                    return 0;
                })).build();
    }



    private static CommandNode<CommandSourceStack> createFrequency() {
        CommandNode<CommandSourceStack> frequency = Commands.literal("frequency").build();

        frequency.addChild(getFrequency());
        frequency.addChild(setFrequency());

        return frequency;
    }

    private static CommandNode<CommandSourceStack> getFrequency() {
        return Commands.literal("get").then(Commands.argument("pos", BlockPosArgument.blockPos())
                .executes(ctx -> {
                    BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                    CrystalNetwork network = CrystalNetwork.get(ctx.getSource().getLevel());
                    Optional<Frequency> freq = network.getRegistry().getFrequencyAt(pos);
                    if(freq.isEmpty())
                        ctx.getSource().sendFailure(Component.literal("No tuned frequency at " + pos));
                    else
                        ctx.getSource().sendSuccess(() -> Component.literal("Frequency at " + pos + " is " + freq.get()), false);
                    return 0;
                })).build();
    }

    private static CommandNode<CommandSourceStack> setFrequency() {
        return Commands.literal("set").then(Commands.argument("pos", BlockPosArgument.blockPos()).then(Commands.argument("frequency", StringArgumentType.greedyString())
                .executes(ctx -> {
                    BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                    if(!ctx.getSource().getLevel().getBlockState(pos).is(ModTags.Blocks.TUNABLE_CLUSTER)) {
                        ctx.getSource().sendFailure(Component.literal("No tunable cluster at " + pos));
                        return 1;
                    }
                    String freqStr = StringArgumentType.getString(ctx, "frequency");
                    CrystalNetwork network = CrystalNetwork.get(ctx.getSource().getLevel());
                    Frequency frequency = Frequency.fromName(freqStr);
                    network.getRegistry().register(pos, frequency);
                    Optional<String> maybeName = network.getFrequencyName(frequency);
                    if(maybeName.isEmpty())
                        network.setFrequencyName(frequency, freqStr);
                    else {
                        String name = maybeName.get();
                        if(!name.equals(freqStr)) {
                            ctx.getSource().sendFailure(Component.literal("Frequency name conflict: " + name + " != " + freqStr));
                            return 1;
                        }
                        network.setFrequencyName(frequency, freqStr);
                    }


                    ctx.getSource().sendSuccess(() -> Component.literal("Frequency at " + pos + " set to " + freqStr), false);
                    return 0;
                })
        )).build();
    }

    public static CommandNode<CommandSourceStack> depositPos() {
        return Commands.literal("depositpos").then(Commands.argument("pos", BlockPosArgument.blockPos())
                .executes(ctx -> {
                    BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                    Optional<CrystalNode> maybeNode = CrystalNetwork.get(ctx.getSource().getLevel()).getNodeAt(pos);
                    if(maybeNode.isEmpty()) {
                        ctx.getSource().sendFailure(Component.literal("No node at " + pos));
                        return 1;
                    }
                    CrystalNode node = maybeNode.get();
                    Optional<Optional<BlockPos>> maybeMaybeDepositPos = node.getDepositingPosAt(pos);
                    if(maybeMaybeDepositPos.isEmpty()) {
                        ctx.getSource().sendFailure(Component.literal("No peridot cluster at " + pos));
                        return 1;
                    }
                    Optional<BlockPos> maybeDepositPos = maybeMaybeDepositPos.get();
                    if(maybeDepositPos.isEmpty())
                        ctx.getSource().sendSuccess(() -> Component.literal("No viable depositing location at " + pos), false);
                    else
                        ctx.getSource().sendSuccess(() -> Component.literal("Depositing at " + maybeDepositPos.get()), false);
                    return 0;
        })).build();
    }

    public static CommandNode<CommandSourceStack> createPrint() {
        CommandNode<CommandSourceStack> print = Commands.literal("print").build();

        print.addChild(printNodes());
        print.addChild(printNetworksAt());
        print.addChild(printRouting());

        return print;
    }

    public static CommandNode<CommandSourceStack> printRouting() {
        return Commands.literal("routing").executes(ctx -> {
            CrystalNetwork.get(ctx.getSource().getLevel()).printRouting();
            return 0;
        }).build();
    }

    public static CommandNode<CommandSourceStack> printNetworksAt() {
        return Commands.literal("networksat").then(Commands.argument("pos", BlockPosArgument.blockPos())
                .executes(ctx -> {
                    BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                    Set<Frequency> networks = CrystalNetwork.get(ctx.getSource().getLevel()).getNetworks(pos);
                    System.out.println(networks);
                    return 0;
        })).build();
    }

}
