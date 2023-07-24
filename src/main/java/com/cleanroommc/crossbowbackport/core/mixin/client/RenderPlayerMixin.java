package com.cleanroommc.crossbowbackport.core.mixin.client;

import com.cleanroommc.crossbowbackport.item.ItemCrossbow;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPlayer.class)
public abstract class RenderPlayerMixin {
    @Shadow public abstract ModelPlayer getMainModel();

    @Inject(method = "setModelVisibilities", at = @At("TAIL"), remap = false)
    private void handleCrossbowPose(AbstractClientPlayer player, CallbackInfo ci) {
        if(!player.isSpectator()) {
            ModelBiped.ArmPose modelbiped$armpose = ModelBiped.ArmPose.EMPTY;
            ModelBiped.ArmPose modelbiped$armpose1 = ModelBiped.ArmPose.EMPTY;
            ItemStack itemstack = player.getHeldItemMainhand();
            ItemStack itemstack1 = player.getHeldItemOffhand();
            if(itemstack.getItem() instanceof ItemCrossbow) {
                if(player.getActiveHand() == EnumHand.MAIN_HAND || ItemCrossbow.getProjectile(itemstack) != ItemStack.EMPTY)
                    modelbiped$armpose = ModelBiped.ArmPose.BOW_AND_ARROW;
            }
            if(itemstack1.getItem() instanceof ItemCrossbow) {
                if(player.getActiveHand() == EnumHand.OFF_HAND || ItemCrossbow.getProjectile(itemstack1) != ItemStack.EMPTY)
                    modelbiped$armpose1 = ModelBiped.ArmPose.BOW_AND_ARROW;
            }
            if(modelbiped$armpose == ModelBiped.ArmPose.EMPTY && modelbiped$armpose1 == ModelBiped.ArmPose.EMPTY)
                return;
            ModelPlayer modelplayer = this.getMainModel();
            if (player.getPrimaryHand() == EnumHandSide.RIGHT)
            {
                modelplayer.rightArmPose = modelbiped$armpose;
                modelplayer.leftArmPose = modelbiped$armpose1;
            }
            else
            {
                modelplayer.rightArmPose = modelbiped$armpose1;
                modelplayer.leftArmPose = modelbiped$armpose;
            }
        }
    }
}
