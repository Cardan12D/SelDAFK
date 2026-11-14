package dev.knyui.data;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

/** Данные игрока: уровень, ранг, очки, статы, открытые стили. Сервер — источник правды. */
public class KnyData {
    public int level = 1;
    public int rank = 0;
    public int points = 15;
    public int strength = Content.BASE_STAT;
    public int agility = Content.BASE_STAT;
    public int endurance = Content.BASE_STAT;
    public final Set<String> unlocked = new HashSet<>();
    public final Set<String> trained = new HashSet<>();

    public int getStat(int i) { return i == 0 ? strength : i == 1 ? agility : endurance; }
    public void setStat(int i, int v) { if (i == 0) strength = v; else if (i == 1) agility = v; else endurance = v; }

    public CompoundTag save() {
        CompoundTag t = new CompoundTag();
        t.putInt("level", level); t.putInt("rank", rank); t.putInt("points", points);
        t.putInt("str", strength); t.putInt("agi", agility); t.putInt("end", endurance);
        t.put("unlocked", toList(unlocked)); t.put("trained", toList(trained));
        return t;
    }

    public void load(CompoundTag t) {
        level = t.getInt("level"); rank = t.getInt("rank"); points = t.getInt("points");
        strength = t.getInt("str"); agility = t.getInt("agi"); endurance = t.getInt("end");
        fromList(t.getList("unlocked", Tag.TAG_STRING), unlocked);
        fromList(t.getList("trained", Tag.TAG_STRING), trained);
    }

    public void copyFrom(KnyData o) { load(o.save()); }

    public void write(FriendlyByteBuf b) {
        b.writeInt(level); b.writeInt(rank); b.writeInt(points);
        b.writeInt(strength); b.writeInt(agility); b.writeInt(endurance);
        b.writeCollection(unlocked, FriendlyByteBuf::writeUtf);
        b.writeCollection(trained, FriendlyByteBuf::writeUtf);
    }

    public static KnyData read(FriendlyByteBuf b) {
        KnyData d = new KnyData();
        d.level = b.readInt(); d.rank = b.readInt(); d.points = b.readInt();
        d.strength = b.readInt(); d.agility = b.readInt(); d.endurance = b.readInt();
        d.unlocked.addAll(b.readCollection(HashSet::new, FriendlyByteBuf::readUtf));
        d.trained.addAll(b.readCollection(HashSet::new, FriendlyByteBuf::readUtf));
        return d;
    }

    private static ListTag toList(Set<String> s) {
        ListTag l = new ListTag();
        for (String x : s) l.add(StringTag.valueOf(x));
        return l;
    }

    private static void fromList(ListTag l, Set<String> out) {
        out.clear();
        for (int i = 0; i < l.size(); i++) out.add(l.getString(i));
    }
}
