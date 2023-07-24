package com.cleanroommc.crossbowbackport;

import com.cleanroommc.crossbowbackport.item.ItemCrossbow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import static com.cleanroommc.crossbowbackport.CrossbowBackport.itemCrossbow;

public class ClientHelper {
    @SubscribeEvent
    public void onRegisterModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(itemCrossbow, 0, new ModelResourceLocation("crossbowbackport:crossbow", "inventory"));
    }

    @SubscribeEvent
    public void onRenderPlayPre(RenderPlayerEvent.Pre event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack rightHand = player.getHeldItemMainhand();
        ItemStack leftHand = player.getHeldItemOffhand();
        if(rightHand.getItem() == itemCrossbow) {
            event.getRenderer().getMainModel().rightArmPose = ModelBiped.ArmPose.BOW_AND_ARROW;
        }
    }

    @SubscribeEvent
    public void onCrossbowRenderFirstPerson(RenderSpecificHandEvent event) {
        if(event.getItemStack().getItem() instanceof ItemCrossbow) {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            boolean bl2 = ItemCrossbow.getProjectile(event.getItemStack()) != ItemStack.EMPTY;
            boolean bl3 = event.getHand() == EnumHand.MAIN_HAND;
            int i = bl3 ? 1 : -1;
            GlStateManager.pushMatrix();
            if (player.getActiveItemStack() != ItemStack.EMPTY && (player.getItemInUseMaxCount()-player.getItemInUseCount()) > 0 && player.getActiveHand() == event.getHand()) {
                ItemStack item = player.getActiveItemStack();
                GlStateManager.translate((double)((float)i * -0.4785682F), -0.094387F, 0.05731531F);
                GlStateManager.rotate(-11.935F, 1.0f, 0f, 0f);
                GlStateManager.rotate((float)i * 65.3F, 0f, 1.0f, 0f);
                GlStateManager.rotate((float)i * -9.785F, 0f, 0f, 1.0f);
                float f = (float)item.getMaxItemUseDuration() - ((float)(player.getItemInUseMaxCount()-player.getItemInUseCount()) - event.getPartialTicks() + 1.0F);
                float g = (float)ItemCrossbow.getChargingTime(item);
                if (g > 1.0F) {
                    g = 1.0F;
                }

                if (g > 0.1F) {
                    float h = MathHelper.sin((f - 0.1F) * 1.3F);
                    float j = g - 0.1F;
                    float k = h * j;
                    GlStateManager.translate((double)(k * 0.0F), (double)(k * 0.004F), (double)(k * 0.0F));
                }

                GlStateManager.translate((double)(g * 0.0F), (double)(g * 0.0F), (double)(g * 0.04F));
                GlStateManager.scale(1.0F, 1.0F, 1.0F + g * 0.2F);
                GlStateManager.rotate((float)i * 45.0F, 0f, -1f, 0f);
            } else {
                float swingProgress = event.getSwingProgress();
                float l = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                float m = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) (Math.PI * 2));
                float n = -0.2F * MathHelper.sin(swingProgress * (float) Math.PI);
                GlStateManager.translate((double)((float)i * l), (double)m, (double)n);
                if (bl2 && swingProgress < 0.001F) {
                    GlStateManager.translate((double)((float)i * -0.5F), 0.0, 0.0);
                    GlStateManager.rotate((float)i * 10.0F, 0f, 1f, 0f);
                }
            }
            Minecraft.getMinecraft().getItemRenderer().renderItemInFirstPerson(player, event.getPartialTicks(), event.getInterpolatedPitch(), event.getHand(), 0f, event.getItemStack(), event.getEquipProgress());
            GlStateManager.popMatrix();
            event.setCanceled(true);
        }
    }
}
