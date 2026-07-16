package net.ramixin.caustics.client.nodes.icons;

import net.minecraft.core.BlockPos;

public class AlidadeIcon extends NodeIcon {

    private double angle;
    private double previousAngle;
    private double velocity;
    private double bump;
    private double targetBump;

    public AlidadeIcon(BlockPos pos) {
        super(pos);
        angle = RANDOM.nextDouble();
    }

    @Override
    public void tick(boolean lookingAt) {
        super.tick(lookingAt);
        previousAngle = angle;
        double lookBump = lookingAt ? 2 : 0;
        double realTargetBump = targetBump + lookBump;
        bump += (realTargetBump - bump) * 0.6;
        bump *= 0.8;
        targetBump *= 0.5;
        if(Math.abs(targetBump) < 0.0001) targetBump = 0;
        double targetVelocity = 1 + bump;
        velocity += (targetVelocity - velocity) * 0.90;
        angle += velocity;
    }

    @Override
    public void bump() {
        targetBump += 30;
    }

    @Override
    public void negativeBump() {
        targetBump -= 47;
    }

    public double angle() {
        return angle;
    }

    public double previousAngle() {
        return previousAngle;
    }
}
