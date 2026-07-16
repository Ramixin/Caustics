package net.ramixin.caustics.client.nodes.icons;

import net.minecraft.core.BlockPos;

public class DowserIcon extends NodeIcon {

    private int timer = 0;
    private int cooldown = 0;
    private double xOffset;
    private double yOffset;
    private int red;
    private int green;
    private int blue;

    public DowserIcon(BlockPos pos) {
        super(pos);
    }

    @Override
    public void tick(boolean lookingAt) {
        super.tick(lookingAt);
        if(timer-- > 0) return;
        if(cooldown-- > 0) {
            if(timer <= 0) {
                xOffset = 0;
                yOffset = 0;
                red = 0xBB;
                green = 0xBB;
                blue = 0xBB;
            }
        }
        cooldown = RANDOM.nextIntBetweenInclusive(4, 40);
        timer = RANDOM.nextIntBetweenInclusive(4, 30);
        xOffset = RANDOM.nextDouble() * 0.4 - 0.2;
        yOffset = RANDOM.nextDouble() * 0.4 - 0.2;
        red = RANDOM.nextIntBetweenInclusive(200, 255);
        green = RANDOM.nextIntBetweenInclusive(200, 255);
        blue = RANDOM.nextIntBetweenInclusive(200, 255);
    }

    public double xOffset() {
        return xOffset;
    }

    public double yOffset() {
        return yOffset;
    }

    public int red() {
        return red;
    }

    public int green() {
        return green;
    }

    public int blue() {
        return blue;
    }
}
