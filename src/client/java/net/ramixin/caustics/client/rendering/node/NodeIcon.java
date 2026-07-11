package net.ramixin.caustics.client.rendering.node;

import net.minecraft.util.RandomSource;

public class NodeIcon {

    private double angle;
    private double previousAngle;
    private double velocity;
    private double bump;
    private double targetBump;
    private int lookedAt;
    private int previousLookedAt;

    public NodeIcon(RandomSource random) {
        angle = random.nextDouble();
    }

    public void tick(boolean lookingAt) {
        previousAngle = angle;
        previousLookedAt = lookedAt;
        double lookBump;
        if(lookingAt) {
            lookBump = 2;
            if(lookedAt < 2)
                lookedAt++;
        } else {
            lookBump = 0;
            if(lookedAt > 0)
                lookedAt--;
        }
        double realTargetBump = targetBump + lookBump;
        bump += (realTargetBump - bump) * 0.6;
        bump *= 0.8;
        targetBump *= 0.5;
        if(Math.abs(targetBump) < 0.0001) targetBump = 0;
        double targetVelocity = 1 + bump;
        velocity += (targetVelocity - velocity) * 0.90;
        angle += velocity;
    }

    public void bump() {
        targetBump += 30;
    }

    public void negativeBump() {
        targetBump -= 37;
    }

    public double angle() {
        return angle;
    }

    public double previousAngle() {
        return previousAngle;
    }

    public int lookedAt() {
        return lookedAt;
    }

    public int previousLookedAt() {
        return previousLookedAt;
    }
}
