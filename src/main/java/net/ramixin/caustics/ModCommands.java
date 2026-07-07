package net.ramixin.caustics;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.ramixin.caustics.items.ModItems;
import net.ramixin.caustics.items.components.Frequency;
import net.ramixin.caustics.items.components.LeaperCharge;
import net.ramixin.caustics.items.components.LeaperMaterial;
import net.ramixin.caustics.items.components.ModDataComponents;
import net.ramixin.caustics.nodes.Node;
import net.ramixin.caustics.nodes.core.CrystalNetwork;
import net.ramixin.caustics.registries.Handle;
import net.ramixin.caustics.registries.ModRegistries;

import java.util.Optional;
import java.util.Set;

public class ModCommands {

    @SuppressWarnings("unused") // selection
    public static void onInitialize(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection selection) {

        RootCommandNode<CommandSourceStack> root = dispatcher.getRoot();
        CommandNode<CommandSourceStack> caustics = Commands.literal("caustics").build();

        caustics.addChild(createPrint());
        caustics.addChild(nuke());
        caustics.addChild(testVisibility());
        caustics.addChild(createFrequency());
        caustics.addChild(depositPos());
        caustics.addChild(giveLeaper(buildContext));

        root.addChild(caustics);
    }

    private static CommandNode<CommandSourceStack> printNodes() {
        return Commands.literal("nodes").executes(ctx -> {
            CrystalNetwork.get(ctx.getSource().getLevel()).nodeWorker().printNodes();
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
                    Optional<Node> maybeNode = network.nodeIndex().getNodeAt(pos);
                    if(maybeNode.isEmpty()) {
                        ctx.getSource().sendFailure(Component.literal("No node at " + pos));
                        return 1;
                    }
                    Node node = maybeNode.get();
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
                    Optional<Frequency> freq = network.frequencyRegistry().getFrequencyAt(pos);
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
                    network.frequencyRegistry().register(pos, frequency);
                    Optional<String> maybeName = network.frequencyRegistry().getFrequencyName(frequency);
                    if(maybeName.isEmpty())
                        network.frequencyRegistry().register(frequency, freqStr);
                    else {
                        String name = maybeName.get();
                        if(!name.equals(freqStr)) {
                            ctx.getSource().sendFailure(Component.literal("Frequency name conflict: " + name + " != " + freqStr));
                            return 1;
                        }
                        network.frequencyRegistry().register(frequency, freqStr);
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
                    Optional<Node> maybeNode = CrystalNetwork.get(ctx.getSource().getLevel()).nodeIndex().getNodeAt(pos);
                    if(maybeNode.isEmpty()) {
                        ctx.getSource().sendFailure(Component.literal("No node at " + pos));
                        return 1;
                    }
                    Node node = maybeNode.get();
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
            CrystalNetwork.get(ctx.getSource().getLevel()).routingManager().printRouting();
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

    public static CommandNode<CommandSourceStack> giveLeaper(CommandBuildContext buildCtx) {
        return Commands.literal("giveleaper").then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("handle", ResourceArgument.resource(buildCtx, ModRegistries.HANDLE_KEY)).then(Commands.argument("decoration", ResourceArgument.resource(buildCtx, ModRegistries.DECORATION_KEY)).then(Commands.argument("core", BoolArgumentType.bool()).then(Commands.argument("max_charges", IntegerArgumentType.integer(1))
                .executes(ctx -> {
                    Player player = EntityArgument.getPlayer(ctx, "player");
                    Holder.Reference<Handle> handle = ResourceArgument.getResource(ctx, "handle", ModRegistries.HANDLE_KEY);
                    Holder.Reference<Item> decoration = ResourceArgument.getResource(ctx, "decoration", ModRegistries.DECORATION_KEY);
                    boolean hasCore = BoolArgumentType.getBool(ctx, "core");
                    int maxCharges = IntegerArgumentType.getInteger(ctx, "max_charges");
                    LeaperMaterial material = new LeaperMaterial(handle, decoration, hasCore);
                    LeaperCharge charge = new LeaperCharge(0, maxCharges);
                    ItemStack stack = new ItemStack(ModItems.LEAPER);
                    stack.set(ModDataComponents.LEAPER_MATERIAL, material);
                    stack.set(ModDataComponents.LEAPER_CHARGE, charge);
                    player.addItem(stack);
                    return 0;
                })))))).build();
    }

}
