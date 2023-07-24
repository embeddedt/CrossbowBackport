package com.cleanroommc.crossbowbackport.item;

import com.cleanroommc.crossbowbackport.CrossbowBackport;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ItemCrossbow extends Item {
    public ItemCrossbow() {
        super();
        this.setMaxStackSize(1);
        this.setRegistryName("crossbow");
        this.setTranslationKey("crossbow");
        this.setMaxDamage(465);
        this.setCreativeTab(CreativeTabs.COMBAT);
        this.addPropertyOverride(new ResourceLocation("pull"), new IItemPropertyGetter()
        {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
            {
                if (entityIn == null)
                {
                    return 0.0F;
                }
                else
                {
                    return !(entityIn.getActiveItemStack().getItem() instanceof ItemCrossbow) ? 0.0F : Math.max(0, (float)(ItemCrossbow.getChargingTime(stack) + 3 - entityIn.getItemInUseCount())) / 20.0F;
                }
            }
        });
        this.addPropertyOverride(new ResourceLocation("pulling"), new IItemPropertyGetter()
        {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
            {
                return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
            }
        });
        this.addPropertyOverride(new ResourceLocation("projectile_type"), new IItemPropertyGetter()
        {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
            {
                if(entityIn != null && stack.getItem() instanceof ItemCrossbow) {
                    ItemStack projectile = getProjectile(stack);
                    if(projectile.getItem() instanceof ItemArrow)
                        return 1f;
                    else if(projectile.getItem() instanceof ItemFirework)
                        return 0.5f;
                }
                return 0f;
            }
        });
    }

    public static ItemStack getProjectile(ItemStack crossbow) {
        if(!crossbow.hasTagCompound())
            return ItemStack.EMPTY;
        NBTTagCompound compound = crossbow.getSubCompound("LoadedProjectile");
        if(compound != null) {
            return new ItemStack(compound);
        }
        return ItemStack.EMPTY;
    }

    private void setProjectile(ItemStack crossbow, ItemStack projectile) {
        NBTTagCompound cbCompound = crossbow.getTagCompound();
        if(cbCompound == null)
            cbCompound = new NBTTagCompound();
        if(projectile != ItemStack.EMPTY) {
            NBTTagCompound compound = new NBTTagCompound();
            projectile.writeToNBT(compound);
            cbCompound.setTag("LoadedProjectile", compound);
        } else if(cbCompound.hasKey("LoadedProjectile"))
            cbCompound.removeTag("LoadedProjectile");
        crossbow.setTagCompound(cbCompound);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }

    public EnumAction getItemUseAction(ItemStack stack)
    {
        return EnumAction.NONE;
    }

    public static int getChargingTime(ItemStack crossbow) {
        return Math.max(1, 25 - (5 * EnchantmentHelper.getEnchantmentLevel(CrossbowBackport.enchantmentQuickCharge, crossbow)));
    }

    private static void shootProjectileOnVector(Entity projectileEntity, double x, double y, double z, double velocity, double divergence) {
        Vec3d vec3d = (new Vec3d(x, y, z)).normalize();
        vec3d = vec3d.add(
                projectileEntity.rand.nextGaussian() * 0.0075F * divergence,
                projectileEntity.rand.nextGaussian() * 0.0075F * divergence,
                projectileEntity.rand.nextGaussian() * 0.0075F * divergence);
        vec3d = vec3d.scale(velocity);
        projectileEntity.motionX = vec3d.x;
        projectileEntity.motionY = vec3d.y;
        projectileEntity.motionZ = vec3d.z;
        float f = MathHelper.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
        projectileEntity.rotationYaw = (float)(MathHelper.atan2(vec3d.x, vec3d.z) * 180.0F / (float)Math.PI);
        projectileEntity.rotationPitch = (float)(MathHelper.atan2(vec3d.y, (double)f) * 180.0F / (float)Math.PI);
        projectileEntity.prevRotationYaw = projectileEntity.rotationYaw;
        projectileEntity.prevRotationPitch = projectileEntity.rotationPitch;
    }

    private static void shootProjectile(Entity projectileEntity, EntityLivingBase shooter, double velocity, double divergence, float angle) {
        float pitch = shooter.rotationPitch;
        float yaw = shooter.rotationYaw;
        float f = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
        float f1 = -MathHelper.sin(pitch * 0.017453292F);
        float f2 = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
        shootProjectileOnVector(projectileEntity, (double)f, (double)f1, (double)f2, velocity, divergence);

        projectileEntity.motionX += shooter.motionX;
        projectileEntity.motionZ += shooter.motionZ;

        if (!shooter.onGround)
        {
            projectileEntity.motionY += shooter.motionY;
        }
    }

    private static void shoot(World world, EntityLivingBase shooter, EnumHand hand, ItemStack crossbow, ItemStack projectile, boolean creative, float speed, float divergence, float extraRotation) {
        if (!world.isRemote) {
            boolean isFirework = projectile.getItem() == Items.FIREWORKS;
            Entity projectileEntity;
            if (isFirework) {
                projectileEntity = new EntityFireworkRocket(world, shooter.posX, shooter.posY + shooter.getEyeHeight() - 0.15F, shooter.posZ, projectile);
            } else {
                projectileEntity = ((ItemArrow)projectile.getItem()).createArrow(world, projectile, shooter);
                if (creative || extraRotation != 0f) {
                    ((EntityArrow)projectileEntity).pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;
                }
                ((EntityArrow)projectileEntity).setIsCritical(true);
            }

            shootProjectile(projectileEntity, shooter, speed, divergence, extraRotation);

            crossbow.damageItem(isFirework ? 3 : 1, shooter);
            world.spawnEntity(projectileEntity);
            //world.playSound((EntityPlayer) null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1.0F, soundPitch);
        }
    }

    private static void shootAll(World world, EntityLivingBase shooter, EnumHand hand, ItemStack crossbow, ItemStack projectile) {
        boolean creative = (shooter instanceof EntityPlayer && ((EntityPlayer) shooter).capabilities.isCreativeMode);
        float speed = projectile.getItem() instanceof ItemFirework ? 1.6f : 3.15f;
        shoot(world, shooter, hand, crossbow, projectile, creative, speed, 0f, 0f);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        int usedTime = stack.getMaxItemUseDuration() - timeLeft;
        if(getProjectile(stack) == ItemStack.EMPTY && entityLiving instanceof EntityPlayer && usedTime >= getChargingTime(stack)) {
            EntityPlayer player = (EntityPlayer)entityLiving;
            ItemStack projectile = findAmmo(player);
            if(projectile != ItemStack.EMPTY) {
                setProjectile(stack, projectile);
                if (!player.capabilities.isCreativeMode)
                {
                    projectile.shrink(1);
                    if (projectile.isEmpty())
                    {
                        player.inventory.deleteStack(projectile);
                    }
                }
            }
        }
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack)
    {
        return 72000;
    }

    @Override
    public int getItemEnchantability()
    {
        return 1;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        ItemStack projectile = getProjectile(itemstack);
        if(projectile != ItemStack.EMPTY) {
            shootAll(worldIn, playerIn, handIn, itemstack, projectile);
            setProjectile(itemstack, ItemStack.EMPTY);
            return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
        } else if(findAmmo(playerIn) != ItemStack.EMPTY) {
            playerIn.setActiveHand(handIn);
            return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
        } else
            return new ActionResult<>(EnumActionResult.FAIL, itemstack);
    }

    protected ItemStack findAmmo(EntityPlayer player)
    {
        if (this.isEligibleAmmo(player.getHeldItem(EnumHand.OFF_HAND)))
        {
            return player.getHeldItem(EnumHand.OFF_HAND);
        }
        else if (this.isEligibleAmmo(player.getHeldItem(EnumHand.MAIN_HAND)))
        {
            return player.getHeldItem(EnumHand.MAIN_HAND);
        }
        else
        {
            for (int i = 0; i < player.inventory.getSizeInventory(); ++i)
            {
                ItemStack itemstack = player.inventory.getStackInSlot(i);

                if (this.isEligibleAmmo(itemstack))
                {
                    return itemstack;
                }
            }

            return ItemStack.EMPTY;
        }
    }

    protected boolean isEligibleAmmo(ItemStack stack) {
        return stack.getItem() instanceof ItemArrow || stack.getItem() instanceof ItemFirework;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment) || enchantment == Enchantments.INFINITY;
    }
}
