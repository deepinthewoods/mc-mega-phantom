package ninja.trek.megaphantom.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.component.DataComponents;

public class ModItems {
    public static ItemStack createBrokenElytra(RegistryAccess registryAccess) {
        ItemStack stack = new ItemStack(Items.ELYTRA);
        // Set damage to max - 1 (1 durability remaining)
        stack.setDamageValue(stack.getMaxDamage() - 1);
        // Set custom purple name
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Broken Elytra").withStyle(ChatFormatting.DARK_PURPLE));
        // Apply curses
        var enchantmentRegistry = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> bindingCurse = enchantmentRegistry.getOrThrow(Enchantments.BINDING_CURSE);
        Holder<Enchantment> vanishingCurse = enchantmentRegistry.getOrThrow(Enchantments.VANISHING_CURSE);
        EnchantmentHelper.updateEnchantments(stack, mutable -> {
            mutable.set(bindingCurse, 1);
            mutable.set(vanishingCurse, 1);
        });
        return stack;
    }
}
