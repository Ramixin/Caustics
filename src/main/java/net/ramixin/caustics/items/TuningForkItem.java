package net.ramixin.caustics.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.ramixin.caustics.items.components.NetworkFrequency;
import net.ramixin.caustics.nodes.CrystalNetwork;
import net.ramixin.caustics.nodes.CrystalNode;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class TuningForkItem extends Item {

    public TuningForkItem(Properties properties) {
        super(properties);
    }

    //sneak + right = copy frequency from node
    //right = set node to frequency
    //left = debug visibility


    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        if(!(level instanceof ServerLevel serverLevel)) return InteractionResult.PASS;
        HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(player, EntitySelector.CAN_BE_PICKED, player.blockInteractionRange());
        if(!(hitResult instanceof BlockHitResult blockHitResult)) return InteractionResult.PASS;
        ItemStack stack = player.getItemInHand(hand);
        BlockPos pos = blockHitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if(player.isShiftKeyDown())
            return ObtainFrequency(pos, serverLevel, stack, state);
        else
            return setFrequency(pos, serverLevel, stack);
    }

    private static InteractionResult ObtainFrequency(BlockPos pos, ServerLevel serverLevel, ItemStack stack, BlockState state) {
        if(state.isAir()) {
            stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
            stack.remove(ModDataComponents.NETWORK_FREQUENCY);
            return InteractionResult.SUCCESS;
        }
        Optional<CrystalNode> maybeNode = CrystalNetwork.get(serverLevel).getNodeAt(pos);
        if(maybeNode.isEmpty()) return InteractionResult.PASS;
        CrystalNode node = maybeNode.get();
        Optional<NetworkFrequency> freq = node.networkFrequencyAt(pos);
        if(freq.isEmpty()) return InteractionResult.PASS;

        stack.set(ModDataComponents.NETWORK_FREQUENCY, freq.get());
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        return InteractionResult.SUCCESS;
    }

    private static InteractionResult setFrequency(BlockPos pos, ServerLevel serverLevel, ItemStack stack) {
        NetworkFrequency freq = stack.get(ModDataComponents.NETWORK_FREQUENCY);
        if(freq == null) return InteractionResult.PASS;
        Optional<CrystalNode> maybeNode = CrystalNetwork.get(serverLevel).getNodeAt(pos);
        if(maybeNode.isEmpty()) return InteractionResult.PASS;
        CrystalNode node = maybeNode.get();
        node.setFrequency(pos, stack.get(ModDataComponents.NETWORK_FREQUENCY));
        return InteractionResult.CONSUME;
    }
}
