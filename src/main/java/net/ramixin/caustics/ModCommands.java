package net.ramixin.caustics;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.ramixin.caustics.items.components.NetworkFrequency;
import net.ramixin.caustics.nodes.CrystalNetwork;
import net.ramixin.caustics.nodes.CrystalNode;

import java.util.Optional;

public class ModCommands {

    @SuppressWarnings("unused") // selection & buildContext
    public static void onInitialize(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection selection) {

        RootCommandNode<CommandSourceStack> root = dispatcher.getRoot();
        CommandNode<CommandSourceStack> caustics = Commands.literal("caustics").build();

        caustics.addChild(printNodes());
        caustics.addChild(nuke());
        caustics.addChild(testVisibility());
        caustics.addChild(getFrequency());
        caustics.addChild(depositPos());

        root.addChild(caustics);
    }

    private static CommandNode<CommandSourceStack> printNodes() {
        return Commands.literal("printnodes").executes(ctx -> {
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

    private static CommandNode<CommandSourceStack> getFrequency() {
        return Commands.literal("getfrequency").then(Commands.argument("pos", BlockPosArgument.blockPos())
                .executes(ctx -> {
                    BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                    Optional<CrystalNode> maybeNode = CrystalNetwork.get(ctx.getSource().getLevel()).getNodeAt(pos);
                    if(maybeNode.isEmpty()) {
                        ctx.getSource().sendFailure(Component.literal("No node at " + pos));
                        return 1;
                    }
                    CrystalNode node = maybeNode.get();
                    Optional<NetworkFrequency> freq = node.networkFrequencyAt(pos);
                    if(freq.isEmpty())
                        ctx.getSource().sendFailure(Component.literal("No sunstone cluster at " + pos));
                    else
                        ctx.getSource().sendSuccess(() -> Component.literal("Frequency at " + pos + " is " + freq.get()), false);
                    return 0;
        })).build();
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

}
