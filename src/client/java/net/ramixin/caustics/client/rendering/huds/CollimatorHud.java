package net.ramixin.caustics.client.rendering.huds;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.ramixin.caustics.ModTags;
import net.ramixin.caustics.client.ModKeys;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.client.nodes.cache.SimpleIconCache;
import net.ramixin.caustics.client.nodes.icons.CollimatorIcon;
import net.ramixin.caustics.client.rendering.TooltipRenderer;
import net.ramixin.caustics.items.components.Frequency;
import net.ramixin.caustics.networking.serverbound.FrequencyChangePayload;
import net.ramixin.caustics.networking.serverbound.FrequencyRenamedPayload;
import net.ramixin.caustics.utils.LookUtil;

import java.util.Optional;
import java.util.function.Function;

import static net.ramixin.caustics.client.rendering.RenderUtil.getFrequencyName;

public class CollimatorHud extends AbstractHud<CollimatorIcon, CollimatorHud.RenderState, SimpleIconCache<CollimatorIcon>> implements ListeningHud {

    private static final Component SELECTED_HEADER = Component.translatable("caustics.hud.collimator.selected");
    private static final Component LOOKING_HEADER = Component.translatable("caustics.hud.collimator.looking");
    private static final Component UNNAMED_GENERIC = Component.translatable("caustics.hud.collimator.unnamed");
    private static final Component COPY_PROMPT = Component.translatable("caustics.hud.collimator.copy");
    private static final Component TUNE_PROMPT = Component.translatable("caustics.hud.collimator.tune");
    private static final Component CLEAR_PROMPT = Component.translatable("caustics.hud.collimator.clear");
    private static final Function<String, Component> RENAME_PROMPT = s -> Component.translatable("caustics.hud.collimator.rename", s);
    private static final Component CAN_RELEASE_PROMPT = Component.translatable("caustics.hud.collimator.can_release").withStyle(ChatFormatting.GRAY);
    private static final Component TYPE_PROMPT = Component.translatable("caustics.hud.collimator.type");

    private EditBox renameBox = null;
    private BlockPos focusPos = null;
    private boolean initialRelease = false;
    private int totallyNotJankFixForKeyPressFiringTwice = 0;
    private int ticksHoldingLeft = 0;

    public CollimatorHud() {
        super(ClientCrystalNetwork.getInstance().caches().collimator(), ModTags.Items.COLLIMATOR_LENS);
    }

    @Override
    protected void onTick(Minecraft minecraft) {
        super.onTick(minecraft);
        if(renameBox != null && !initialRelease) {
            ticksHoldingLeft++;
        }
    }

    @Override
    protected RenderState extractHudLooking(Optional<Integer> maybeClosest, float partialTicks) {
        if(renameBox != null) {
            String name = renameBox.getValue() + " ";
            return new RenderState.RenameRenderState(name, renameBox.getCursorPosition(), ticksHoldingLeft > 80);
        }
        if(maybeClosest.isEmpty()) return null;
        int closestIndex = maybeClosest.get();
        BlockPos closestPos = cache.getPositions()[closestIndex];
        Component freqName = getFrequencyName(closestPos, UNNAMED_GENERIC);
        boolean lookingAtSelected = cache.getSelectedPos().map(closestPos::equals).orElse(false);
        boolean canCopy = ClientCrystalNetwork.getInstance().frequencyRegistry().getFrequencyAt(closestPos).isPresent();
        float progress = Mth.lerp(partialTicks, prevTicksLooking, ticksLooking) / 2f;

        return new RenderState.LookingRenderState(freqName, lookingAtSelected, canCopy, progress);
    }

    @Override
    protected RenderState extractHudSelected(Optional<BlockPos> maybeSelected) {
        if(maybeSelected.isEmpty()) return null;
        BlockPos selectedPos = maybeSelected.get();

        return new RenderState.SelectRenderState(getFrequencyName(selectedPos, UNNAMED_GENERIC));
    }

    @SuppressWarnings("PatternVariableHidesField")
    @Override
    protected void renderHud(TooltipRenderer renderer, RenderState state) {
        switch (state) {
            case RenderState.SelectRenderState selectState -> renderSelectHud(renderer, selectState);
            case RenderState.RenameRenderState renameState -> renderRenameHud(renderer, renameState);
            case RenderState.LookingRenderState lookingState -> renderLookingHud(renderer, lookingState);
            default -> throw new IllegalStateException("Unexpected value: " + state);
        }
    }

    private void renderRenameHud(TooltipRenderer renderer, RenderState.RenameRenderState state) {
        renderer.setAlpha(255);
        renderer.centerAlign(455);
        MutableComponent before = Component.literal(state.newName.substring(0, state.cursorPos));
        MutableComponent after = Component.literal(state.newName.substring(state.cursorPos+1));
        MutableComponent at = Component.literal(state.newName.substring(state.cursorPos, state.cursorPos + 1)).withStyle(ChatFormatting.UNDERLINE);
        renderer.render(TYPE_PROMPT, 117);
        renderer.render(before.append(at).append(after), 1);
        if(state.canRelease) renderer.render(CAN_RELEASE_PROMPT, 25);
    }

    private void renderLookingHud(TooltipRenderer renderer, RenderState.LookingRenderState state) {
        renderer.setAlpha((int) (255 * state.progress));
        renderer.render(LOOKING_HEADER, 22);
        renderer.render(state.frequency, 1);

        Optional<BlockPos> selectedPos = cache.getSelectedPos();
        Optional<Component> prompt;
        if(selectedPos.isEmpty()) {
            if(state.canCopy) prompt = Optional.of(COPY_PROMPT);
            else prompt = Optional.empty();
        } else {
            prompt = Optional.of(state.lookingAtSelected ? CLEAR_PROMPT : TUNE_PROMPT);
        }

        String name = KeyMappingHelper.getBoundKeyOf(ModKeys.renameFreq).getDisplayName().getString();
        if(prompt.isPresent()) {
            renderer.render(prompt.get(), 10);
            renderer.render(RENAME_PROMPT.apply(name), 1);
        } else {
            renderer.render(RENAME_PROMPT.apply(name), 27);
        }

    }

    private void renderSelectHud(TooltipRenderer renderer, RenderState.SelectRenderState state) {
        renderer.setAlpha(255);
        renderer.render(SELECTED_HEADER, 22);
        renderer.render(state.frequency, 1);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean released) {
        if(Minecraft.getInstance().screen != null) return false;
        if(event.button() == 0)
            return onLeftClick(released);
        else if(event.button() == 1)
            return onRightClick(released);
        else return false;
    }

    private boolean onRightClick(boolean released) {
        if(renameBox == null) return false;
        if(released) {
            if(!initialRelease) {
                initialRelease = true;
                return true;
            }
            return false;
        }

        ClientPlayNetworking.send(new FrequencyRenamedPayload(focusPos, renameBox.getValue()));
        renameBox = null;
        focusPos = null;
        return true;
    }

    private boolean onLeftClick(boolean released) {
        if(released) return false;
        if(renameBox != null) return true;
        Optional<Integer> closest = LookUtil.calculateClosestLooking(cache.getAngles());
        if(closest.isEmpty()) {
            cache.resetSelection();
            focusPos = null;
            return false;
        }
        BlockPos pos = cache.getPositions()[closest.get()];
        if(cache.getSelectedPos().isEmpty()) {
            Optional<Frequency> maybeFrequency = ClientCrystalNetwork.getInstance().frequencyRegistry().getFrequencyAt(pos);
            if(maybeFrequency.isEmpty()) return false;
            cache.select(pos);
            focusPos = pos;
            return true;
        }

        BlockPos selectedPos = cache.getSelectedPos().get();
        if(selectedPos.equals(pos)) {
            ClientPlayNetworking.send(new FrequencyChangePayload(pos, Optional.empty()));
            cache.resetSelection();
            focusPos = null;
            return true;
        }

        Optional<Frequency> maybeFrequency = ClientCrystalNetwork.getInstance().frequencyRegistry().getFrequencyAt(selectedPos);
        if(maybeFrequency.isEmpty()) return false;
        Frequency frequency = maybeFrequency.get();
        ClientPlayNetworking.send(new FrequencyChangePayload(pos, Optional.of(frequency)));
        cache.resetSelection();
        focusPos = null;
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if(Minecraft.getInstance().screen != null) return false;
        if(renameBox == null) return false;
        if(event.isEscape()) {
            renameBox = null;
            focusPos = null;
            return true;
        }
        if(!renameBox.canConsumeInput()) return false;
        if(totallyNotJankFixForKeyPressFiringTwice++ % 2 == 1) return true;
        return !renameBox.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if(renameBox == null) return false;
        if(!event.isAllowedChatCharacter()) return false;
        return renameBox.charTyped(event);
    }

    public void startRename() {
        Optional<Integer> closest = LookUtil.calculateClosestLooking(cache.getAngles());
        if(closest.isEmpty()) return;
        focusPos = cache.getPositions()[closest.get()];
        renameBox = new EditBox(Minecraft.getInstance().font, 0, 0, 0, 0, Component.literal(""));
        renameBox.setFocused(true);
        renameBox.setEditable(true);
        renameBox.setMaxLength(16);
        ticksHoldingLeft = 0;
        initialRelease = false;
    }

    protected sealed interface RenderState extends ProgressState<RenderState> {

        record LookingRenderState(Component frequency, boolean lookingAtSelected, boolean canCopy, float progress) implements RenderState {
            @Override
            public LookingRenderState withProgress(float progress) {
                return new LookingRenderState(frequency, lookingAtSelected, canCopy, progress);
            }
        }

        record SelectRenderState(Component frequency) implements RenderState {
            @Override
            public SelectRenderState withProgress(float progress) {
                return this;
            }
        }

        record RenameRenderState(String newName, int cursorPos, boolean canRelease) implements RenderState {
            @Override
            public RenameRenderState withProgress(float progress) {
                return this;
            }
        }

    }


}
