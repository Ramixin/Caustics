package net.ramixin.caustics.client.nodes;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.ramixin.caustics.client.entities.ClientLeapGhost;

import java.util.*;

public class LeapParticleEngine {

    private static final int[][] SKIN_REGIONS = {
            //Head
            {8, 0, 8, 8, 0},
            {16, 0, 8, 8, 0},
            {0, 8, 8, 8, 0},
            {8, 8, 8, 8, 0},
            {16, 8, 8, 8, 0},
            {24, 8, 8, 8, 0},
            //Body
            {20, 16, 8, 12, 1},
            {28, 16, 8, 12, 1},
            {16, 20, 4, 12, 1},
            {20, 20, 8, 12, 1},
            {28, 20, 4, 12, 1},
            {32, 20, 8, 12, 1},
            //Right Arm
            {44, 16, 4, 12, 2},
            {48, 16, 4, 12, 2},
            {40, 20, 4, 12, 2},
            {44, 20, 4, 12, 2},
            {48, 20, 4, 12, 2},
            {52, 20, 4, 12, 2},
            //Left Arm
            {36, 48, 4, 12, 3},
            {40, 48, 4, 12, 3},
            {32, 52, 4, 12, 3},
            {36, 52, 4, 12, 3},
            {40, 52, 4, 12, 3},
            {44, 52, 4, 12, 3},
            //Right Leg
            {4, 16, 4, 12, 4},
            {8, 16, 4, 12, 4},
            {0, 20, 4, 12, 4},
            {4, 20, 4, 12, 4},
            {8, 20, 4, 12, 4},
            {12, 20, 4, 12, 4},
            //Left Leg
            {20, 48, 4, 12, 5},
            {24, 48, 4, 12, 5},
            {16, 52, 4, 12, 5},
            {20, 52, 4, 12, 5},
            {24, 52, 4, 12, 5},
            {28, 52, 4, 12, 5}
    };
    private static final double[][] PART_HEIGHTS = {
            {1.75, 2.0}, // Head
            {0.75, 1.5}, // Body
            {0.75, 1.5}, // Right arm
            {0.75, 1.5}, // Left arm
            {0.0, 0.75}, // Right leg
            {0.0, 0.75}  // Left leg
    };

    private final HashMap<UUID, List<LeapParticle>> particles = new HashMap<>();

    public void addParticle(Player avatar) {
        RandomSource random = avatar.getRandom();
        if(reduce(random)) return;

        Optional<ClientLeap> maybeLeap = ClientCrystalNetwork.getInstance().getLeap(avatar.getUUID());
        if(maybeLeap.isEmpty()) return;
        ClientLeap leap = maybeLeap.get();
        int[] region = SKIN_REGIONS[random.nextInt(SKIN_REGIONS.length)];
        int u = random.nextIntBetweenInclusive(region[0], region[0] + region[2]);
        int v = random.nextIntBetweenInclusive(region[1], region[1] + region[3]);
        double[] heights = PART_HEIGHTS[region[4]];
        double deltaHeight = heights[1] - heights[0];
        Vec3 position = avatar.position().add(random.nextDouble()*0.8-0.9, random.nextDouble() * deltaHeight + heights[0]-0.5, random.nextDouble()*0.8-0.9);
        double speed = random.nextDouble() / 2;
        Vec3 deltas = leap.sapphirePos().offset(0,-1,0).getCenter().subtract(avatar.position()).normalize().multiply(speed, speed, speed);
        LeapParticle particle = new LeapParticle(position, deltas,0.9, u, v);
        List<LeapParticle> particles = this.particles.computeIfAbsent(avatar.getUUID(), _ -> new ArrayList<>());
        particles.add(particle);
    }

    public void addParticle(ClientLeapGhost ghost) {
        RandomSource random = ghost.getRandom();
        if(reduce(random)) return;
        int[] region = SKIN_REGIONS[random.nextInt(SKIN_REGIONS.length)];
        int u = random.nextIntBetweenInclusive(region[0], region[0] + region[2]);
        int v = random.nextIntBetweenInclusive(region[1], region[1] + region[3]);
        Vec3 center = ghost.position().add(-0.5, random.nextDouble() * 2 - 0.5, -0.5);
        Vec3 position = center.offsetRandom(random, 4);
        double speed = center.distanceTo(ghost.position()) / 7;
        Vec3 deltas = center.subtract(position).normalize().multiply(speed, speed, speed);
        LeapParticle particle = new LeapParticle(position, deltas,0.9, u, v);
        List<LeapParticle> particles = this.particles.computeIfAbsent(ghost.getProfileId(), _ -> new ArrayList<>());
        particles.add(particle);
    }

    private boolean reduce(RandomSource random) {
        ParticleStatus particleStatus = Minecraft.getInstance().options.particles().get();
        if(particleStatus == ParticleStatus.MINIMAL) return true;
        else return particleStatus == ParticleStatus.DECREASED && random.nextInt(3) != 2;
    }

    public void tick() {
        for(List<LeapParticle> particles : this.particles.values()) {
            for(LeapParticle particle : particles)
                particle.tick();
            particles.removeIf(LeapParticle::invalid);
        }
    }

    public List<LeapParticle> getParticles(UUID uuid) {
        return particles.getOrDefault(uuid, Collections.emptyList());
    }

    public void clear() {
        particles.clear();
    }

}
