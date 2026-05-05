package com.pvmhud.overlay;

import net.runelite.api.Skill;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@Singleton
final class HudIconCache {
    private static final Skill[] SKILLS = Skill.values();

    @Inject
    private ItemManager itemManager;

    @Inject
    private SkillIconManager skillIconManager;

    @Inject
    private SpriteManager spriteManager;

    private final Map<Long, BufferedImage> iconCache = new HashMap<>();

    BufferedImage load(IconRef iconRef, int size) {
        if (iconRef == null || size <= 0) {
            return null;
        }

        long key = iconRef.cacheKey(size);
        BufferedImage cached = iconCache.get(key);
        if (cached != null) {
            return cached;
        }

        BufferedImage image = loadSource(iconRef);
        if (image == null) {
            return null;
        }

        BufferedImage scaled = ImageUtil.resizeImage(image, size, size);
        iconCache.put(key, scaled);
        return scaled;
    }

    void clear() {
        iconCache.clear();
    }

    private BufferedImage loadSource(IconRef iconRef) {
        switch (iconRef.group) {
            case STAT:
                return skillIconManager.getSkillImage(SKILLS[iconRef.id]);
            case SPRITE:
            case SPELL:
                try {
                    return spriteManager.getSprite(iconRef.id, 0);
                } catch (Exception ignored) {
                    return null;
                }
            case ITEM:
                return itemManager.getImage(iconRef.id);
            default:
                return null;
        }
    }
}
