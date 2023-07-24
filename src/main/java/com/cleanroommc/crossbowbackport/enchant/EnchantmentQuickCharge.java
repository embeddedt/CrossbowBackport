package com.cleanroommc.crossbowbackport.enchant;

import com.cleanroommc.crossbowbackport.CrossbowBackport;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentQuickCharge extends Enchantment {
    public EnchantmentQuickCharge() {
        super(Rarity.RARE, CrossbowBackport.CROSSBOW_TYPE, new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND });
        setRegistryName("quick_charge");
        setName("quick_charge");
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }
}
