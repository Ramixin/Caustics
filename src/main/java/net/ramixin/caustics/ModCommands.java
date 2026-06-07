package net.ramixin.caustics;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.ramixin.caustics.nodes.CrystalNetwork;

public class ModCommands {

    @SuppressWarnings("unused") // selection & buildContext
    public static void onInitialize(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection selection) {

        RootCommandNode<CommandSourceStack> root = dispatcher.getRoot();
        CommandNode<CommandSourceStack> caustics = Commands.literal("caustics").build();

        caustics.addChild(printNodes());
        caustics.addChild(nuke());

        root.addChild(caustics);
    }

    public static CommandNode<CommandSourceStack> printNodes() {
        return Commands.literal("printnodes").executes(ctx -> {
            CrystalNetwork.get(ctx.getSource().getLevel()).printNodes();
            return 0;
        }).build();
    }

    public static CommandNode<CommandSourceStack> nuke() {
        return Commands.literal("nuke").executes(ctx -> {
            CrystalNetwork.get(ctx.getSource().getLevel()).nuke();
            return 0;
        }).build();
    }

}
