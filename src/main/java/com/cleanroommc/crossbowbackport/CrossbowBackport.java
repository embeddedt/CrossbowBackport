package com.cleanroommc.crossbowbackport;

import com.cleanroommc.assetmover.AssetMoverAPI;
import com.cleanroommc.crossbowbackport.enchant.EnchantmentQuickCharge;
import com.cleanroommc.crossbowbackport.item.ItemCrossbow;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;

@Mod(name = "Crossbow Backport", modid = "crossbowbackport", version = "1.0")
@Mod.EventBusSubscriber
public class CrossbowBackport {
    public static final String[] TEXTURES = new String[] {
            "crossbow_pulling_0",
            "crossbow_pulling_1",
            "crossbow_pulling_2",
            "crossbow_arrow",
            "crossbow_firework",
            "crossbow_standby"
    };
    public static final EnumEnchantmentType CROSSBOW_TYPE = EnumHelper.addEnchantmentType("crossbow", item -> item instanceof ItemCrossbow);


    @GameRegistry.ObjectHolder("crossbowbackport:crossbow")
    public static Item itemCrossbow;

    @GameRegistry.ObjectHolder("crossbowbackport:quick_charge")
    public static Enchantment enchantmentQuickCharge;

    @Mod.EventHandler
    public void getAssets(FMLPreInitializationEvent event) {
        HashMap<String, String> map = new HashMap<>();
        for(String texture : TEXTURES) {
            map.put("assets/minecraft/textures/item/" + texture + ".png", "assets/crossbowbackport/textures/items/" + texture + ".png");
        }
        AssetMoverAPI.fromMinecraft("1.16.5", map);
        if(FMLCommonHandler.instance().getSide() == Side.CLIENT)
            MinecraftForge.EVENT_BUS.register(new ClientHelper());
    }
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemCrossbow());
    }
    @SubscribeEvent
    public static void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
        event.getRegistry().register(new EnchantmentQuickCharge());
    }
}
