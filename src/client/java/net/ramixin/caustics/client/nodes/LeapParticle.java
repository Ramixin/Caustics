package net.ramixin.caustics.client.nodes;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public final class LeapParticle {

    private final int MAX_AGE = 30;

    private Vec3 position;
    private Vec3 oldPosition;
    private Vec3 velocity;
    private final double deltaDropOff;
    private int age = 0;
    private final int u;
    private final int v;

    public LeapParticle(Vec3 position, Vec3 velocity, double deltaDropOff, int u, int v) {
        this.position = position;
        this.oldPosition = position;
        this.velocity = velocity;
        this.deltaDropOff = deltaDropOff;
        this.u = u;
        this.v = v;
    }

    public void tick() {
        this.oldPosition = this.position;
        this.position = this.position.add(this.velocity);
        this.velocity = this.velocity.multiply(this.deltaDropOff, this.deltaDropOff, this.deltaDropOff);
        age++;
    }

    public Vec3 position() {
        return position;
    }

    public Vec3 oldPosition() {
        return oldPosition;
    }

    public double scale(float partialTicks) {
        if(age >= 5) return 1;
        return Mth.lerp(partialTicks, age / 5d, (age + 1) / 5d);
    }

    public int u() {
        return u;
    }

    public int v() {
        return v;
    }

    public boolean invalid() {
        return age >= MAX_AGE;
    }

    public double opacity() {
        double progress = (double) age / MAX_AGE;
        return 1 - Math.pow(progress, 5);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, velocity, deltaDropOff);
    }

    @Override
    public String toString() {
        return "LeapParticle{" +
                "position=" + position +
                ", velocity=" + velocity +
                ", acceleration=" + deltaDropOff +
                ", age=" + age +
                ", u=" + u +
                ", v=" + v +
                '}';
    }
}
