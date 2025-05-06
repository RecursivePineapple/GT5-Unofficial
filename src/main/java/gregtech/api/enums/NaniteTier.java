package gregtech.api.enums;

import java.text.MessageFormat;

import net.minecraft.item.ItemStack;

import gregtech.api.objects.ItemData;
import gregtech.api.util.GTOreDictUnificator;

public enum NaniteTier {

    Carbon(1),
    Glowstone(1),
    Silver(2),
    Neutronium(2),
    Gold(3),
    // intentionally above gold because transmetal nanites are harder to make
    Transcendent(4),
    SixPhasedCopper(5),
    // eoh t1-t3
    WhiteDwarf(6),
    // eoh t4-t6
    BlackDwarf(7),
    // eoh t7-t9
    Universium(8),
    Eternity(9),
    MagMatter(10);

    public final int tier;

    private ItemStack stack = null;

    private NaniteTier(int tier) {
        this.tier = tier;
    }

    public Materials getMaterial() {
        return switch (this) {
            case Carbon -> Materials.Carbon;
            case Glowstone -> Materials.Glowstone;
            case Silver -> Materials.Silver;
            case Neutronium -> Materials.Neutronium;
            case Gold -> Materials.Gold;
            case Transcendent -> MaterialsUEVplus.TranscendentMetal;
            case WhiteDwarf -> MaterialsUEVplus.WhiteDwarfMatter;
            case BlackDwarf -> MaterialsUEVplus.BlackDwarfMatter;
            case SixPhasedCopper -> MaterialsUEVplus.SixPhasedCopper;
            case Universium -> MaterialsUEVplus.Universium;
            case MagMatter -> MaterialsUEVplus.MagMatter;
            case Eternity -> MaterialsUEVplus.Eternity;
        };
    }

    public ItemStack getStack() {
        if (this.stack == null) {
            stack = GTOreDictUnificator.get(OrePrefixes.nanite, getMaterial(), 1);
        }

        return stack.copy();
    }

    public String describe() {
        return MessageFormat.format("{0} (Tier {1})", getMaterial().mLocalizedName, tier);
    }

    public static NaniteTier fromMaterial(Materials mat) {
        if (mat == Materials.Carbon) return Carbon;
        if (mat == Materials.Glowstone) return Glowstone;
        if (mat == Materials.Silver) return Silver;
        if (mat == Materials.Neutronium) return Neutronium;
        if (mat == Materials.Gold) return Gold;
        if (mat == MaterialsUEVplus.TranscendentMetal) return Transcendent;
        if (mat == MaterialsUEVplus.WhiteDwarfMatter) return WhiteDwarf;
        if (mat == MaterialsUEVplus.BlackDwarfMatter) return BlackDwarf;
        if (mat == MaterialsUEVplus.SixPhasedCopper) return SixPhasedCopper;
        if (mat == MaterialsUEVplus.Universium) return Universium;
        if (mat == MaterialsUEVplus.MagMatter) return MagMatter;
        if (mat == MaterialsUEVplus.Eternity) return Eternity;

        return null;
    }

    public static NaniteTier fromStack(ItemStack stack) {
        if (stack == null) return null;

        ItemData data = GTOreDictUnificator.getAssociation(stack);

        if (data == null) return null;
        if (data.mPrefix != OrePrefixes.nanite) return null;

        return fromMaterial(data.mMaterial.mMaterial);
    }
}
