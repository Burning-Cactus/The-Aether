package com.gildedgames.aether.common.entity.miscellaneous;

import com.gildedgames.aether.common.registry.AetherEntityTypes;
import com.gildedgames.aether.core.api.registers.ParachuteType;
import com.gildedgames.aether.core.registry.AetherParachuteTypes;
import net.minecraft.entity.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ParachuteEntity extends Entity
{
    private static final DataParameter<String> DATA_PARACHUTE_TYPE = EntityDataManager.defineId(ParachuteEntity.class, DataSerializers.STRING);
    private float parachuteSpeed;

    public ParachuteEntity(EntityType<?> entityType, World world) {
        super(entityType, world);
        this.blocksBuilding = true;
    }

    public ParachuteEntity(World worldIn, double x, double y, double z) {
        this(AetherEntityTypes.PARACHUTE.get(), worldIn);
        this.setPos(x, y, z);
        this.setDeltaMovement(Vector3d.ZERO);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_PARACHUTE_TYPE, AetherParachuteTypes.COLD_PARACHUTE.getRegistryName());
    }

    @Override
    public void tick() {
        super.tick();
        boolean hasControllingPassenger = this.getControllingPassenger() != null;
        if (hasControllingPassenger) {
            Entity passenger = this.getControllingPassenger();
            this.fallDistance = 0.0F;
            this.moveParachute(passenger);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.spawnExplosionParticle();
            if (this.isOnGround() || this.isInWater() || this.isInLava()) {
                this.ejectPassengers();
                this.die();
            }
        } else {
            this.die();
        }
    }

    private void moveParachute(Entity passenger) {
        if (this.isVehicle()) {
            Vector3d parachuteVec = this.getDeltaMovement();
            Vector3d passengerVec = passenger.getDeltaMovement();
            if (passengerVec.x() != 0.0D || passengerVec.z() != 0.0D) {
                this.parachuteSpeed = MathHelper.approach(this.parachuteSpeed, 0.6F, 0.025F);
            } else {
                this.parachuteSpeed = MathHelper.approach(this.parachuteSpeed, 0.0F, 0.0005F);
            }
            double x = this.parachuteSpeed * (passengerVec.x() * 10.0D);
            double z = this.parachuteSpeed * (passengerVec.z() * 10.0D);
            this.setDeltaMovement(parachuteVec.add((new Vector3d(x, 0.0D, z)).subtract(parachuteVec).scale(0.2D)));
            Vector3d parachuteVec2 = this.getDeltaMovement();
            this.setDeltaMovement(parachuteVec2.x(), -0.15, parachuteVec2.z());
        }
    }

    public void spawnExplosionParticle() {
        for (int i = 0; i < 1; ++i) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            double d3 = 10.0D;
            double d4 = this.getX() + ((double) this.random.nextFloat() * this.getBbWidth() * 2.0D) - this.getBbWidth() - d0 * d3;
            double d5 = this.getY() + ((double) this.random.nextFloat() * this.getBbHeight()) - d1 * d3;
            double d6 = this.getZ() + ((double) this.random.nextFloat() * this.getBbWidth() * 2.0D) - this.getBbWidth() - d2 * d3;
            this.level.addParticle(ParticleTypes.POOF, d4, d5, d6, d0, d1, d2);
        }
    }

    public void die() {
        if (!this.level.isClientSide) {
            this.kill();
        }
        this.spawnExplosionParticle();
    }

    @Override
    protected boolean canRide(Entity entityIn) {
        return true;
    }

    @Override
    public boolean canRiderInteract() {
        return false;
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 1.35D;
    }

    @Override
    @Nullable
    public Entity getControllingPassenger() {
        List<Entity> list = this.getPassengers();
        return list.isEmpty() ? null : list.get(0);
    }

    public void setParachuteType(ParachuteType type) {
        this.entityData.set(DATA_PARACHUTE_TYPE, type.getRegistryName());
    }

    public ParachuteType getParachuteType() {
        return AetherParachuteTypes.PARACHUTES.get(this.getEntityData().get(DATA_PARACHUTE_TYPE));
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        nbt.putString("Type", this.getParachuteType().getRegistryName());
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        if (nbt.contains("Type", 8)) {
            this.setParachuteType(AetherParachuteTypes.PARACHUTES.get(nbt.getString("Type")));
        }
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
