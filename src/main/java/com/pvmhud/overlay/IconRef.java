package com.pvmhud.overlay;

import net.runelite.api.Skill;

final class IconRef {
    final IconGroup group;
    final int id;

    private IconRef(IconGroup group, int id) {
        this.group = group;
        this.id = id;
    }

    static IconRef statSkill(Skill skill) {
        return new IconRef(IconGroup.STAT, skill.ordinal());
    }

    static IconRef statSprite(int spriteId) {
        return new IconRef(IconGroup.SPRITE, spriteId);
    }

    static IconRef spell(int spriteId) {
        return new IconRef(IconGroup.SPELL, spriteId);
    }

    static IconRef item(int itemId) {
        return new IconRef(IconGroup.ITEM, itemId);
    }

    long cacheKey(int size) {
        return ((long) group.ordinal() << 48) | ((long) id << 16) | size;
    }
}
