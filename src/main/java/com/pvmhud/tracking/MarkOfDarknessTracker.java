package com.pvmhud.tracking;

import net.runelite.api.ChatMessageType;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

import javax.inject.Singleton;

@Singleton
public class MarkOfDarknessTracker extends BaseTimedSpellTracker {
    private static final String MARK_PLACED_MESSAGE =
            "you have placed a mark of darkness upon yourself.";
    private static final String MARK_FADED_MESSAGE =
            "your mark of darkness has faded away.";
    private static final String MARK_EXPIRING_MESSAGE =
            "your mark of darkness is about to run out.";

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE) {
            return;
        }

        String message = Text.standardize(event.getMessage());

        if (MARK_PLACED_MESSAGE.equals(message)) {
            markActive(TimeConstants.ticksToNanos(getDurationTicks()));
        } else if (MARK_EXPIRING_MESSAGE.equals(message)) {
            setExpiringSoon(true);
        } else if (MARK_FADED_MESSAGE.equals(message)) {
            clearActive();
        }
    }

    @Override
    protected void sync() {
        setCooldownActive(false);
    }

    private int getDurationTicks() {
        int ticks = client.getRealSkillLevel(Skill.MAGIC) * 3;

        if (isPurgingStaffEquipped()) {
            ticks *= 5;
        }

        return ticks;
    }

    private boolean isPurgingStaffEquipped() {
        ItemContainer equipment = client.getItemContainer(InventoryID.WORN);
        if (equipment == null) {
            return false;
        }

        Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
        return weapon != null && weapon.getId() == ItemID.PURGING_STAFF;
    }
}