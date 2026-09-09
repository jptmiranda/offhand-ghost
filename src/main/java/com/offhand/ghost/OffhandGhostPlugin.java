/*
 * Copyright (c) 2026, dot <joao.t.m@proton.me>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.offhand.ghost;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Offhand Ghost",
	description = "Shows a faded sprite of your two-handed weapon in the equipment slot it blocks",
	tags = {"equipment", "two", "handed", "2h", "shield", "offhand", "overlay", "ghost"}
)
public class OffhandGhostPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ItemManager itemManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private OffhandGhostConfig config;

	private final List<Overlay> overlays = new ArrayList<>();
	private final List<Widget> hiddenGlyphs = new ArrayList<>();

	@Getter
	private EquipmentInventorySlot ghostSlot;

	@Getter
	private BufferedImage ghostImage;

	@Getter
	private BufferedImage shadowImage;

	private int ghostItemId = -1;

	@Override
	protected void startUp()
	{
		overlays.add(new OffhandGhostOverlay(client, this, config,
			InterfaceID.WORNITEMS, InterfaceID.Wornitems.SLOT3, InterfaceID.Wornitems.SLOT5));
		overlays.add(new OffhandGhostOverlay(client, this, config,
			InterfaceID.EQUIPMENT, InterfaceID.Equipment.SLOT3, InterfaceID.Equipment.SLOT5));
		overlays.forEach(overlayManager::add);

		clientThread.invokeLater(this::updateGhost);
	}

	@Override
	protected void shutDown()
	{
		overlays.forEach(overlayManager::remove);
		overlays.clear();
		restoreSlotGlyphs();
		clearGhost();
	}

	@Provides
	OffhandGhostConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(OffhandGhostConfig.class);
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == InventoryID.WORN)
		{
			updateGhost();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		final GameState state = event.getGameState();
		if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING)
		{
			clearGhost();
		}
	}

	@Subscribe
	public void onBeforeRender(BeforeRender event)
	{
		// Reapplied from scratch every frame; the interface rebuilds itself as gear changes.
		restoreSlotGlyphs();

		if (ghostSlot == null)
		{
			return;
		}

		final boolean shield = ghostSlot == EquipmentInventorySlot.SHIELD;
		hideSlotGlyph(shield ? InterfaceID.Wornitems.SLOT5 : InterfaceID.Wornitems.SLOT3);
		hideSlotGlyph(shield ? InterfaceID.Equipment.SLOT5 : InterfaceID.Equipment.SLOT3);
	}

	private void restoreSlotGlyphs()
	{
		for (Widget glyph : hiddenGlyphs)
		{
			glyph.setHidden(false);
		}
		hiddenGlyphs.clear();
	}

	private void hideSlotGlyph(int component)
	{
		final Widget slot = client.getWidget(component);
		if (slot == null)
		{
			return;
		}

		final Widget glyph = findSlotGlyph(slot);
		if (glyph != null && !glyph.isHidden())
		{
			glyph.setHidden(true);
			hiddenGlyphs.add(glyph);
		}
	}

	// A slot's sprite children are the stone plate and, over it, the slot-type glyph that the game
	// hides whenever a real item occupies the slot.
	private Widget findSlotGlyph(Widget slot)
	{
		final Widget[] children = slot.getDynamicChildren();
		if (children == null)
		{
			return null;
		}

		Widget glyph = null;
		int sprites = 0;
		for (Widget child : children)
		{
			if (child != null && child.getSpriteId() != -1)
			{
				glyph = child;
				sprites++;
			}
		}
		return sprites > 1 ? glyph : null;
	}

	private void updateGhost()
	{
		final ItemContainer equipment = client.getItemContainer(InventoryID.WORN);
		if (equipment == null)
		{
			clearGhost();
			return;
		}

		final int weaponId = itemIdAt(equipment, EquipmentInventorySlot.WEAPON);
		final int shieldId = itemIdAt(equipment, EquipmentInventorySlot.SHIELD);

		if (weaponId != -1 && shieldId == -1 && isTwoHanded(weaponId))
		{
			setGhost(weaponId, EquipmentInventorySlot.SHIELD);
		}
		else if (shieldId != -1 && weaponId == -1 && isTwoHanded(shieldId))
		{
			setGhost(shieldId, EquipmentInventorySlot.WEAPON);
		}
		else
		{
			clearGhost();
		}
	}

	private int itemIdAt(ItemContainer container, EquipmentInventorySlot slot)
	{
		final Item item = container.getItem(slot.getSlotIdx());
		return item == null || item.getId() <= 0 ? -1 : item.getId();
	}

	private boolean isTwoHanded(int itemId)
	{
		final ItemStats stats = itemManager.getItemStats(itemId);
		final ItemEquipmentStats equipment = stats == null ? null : stats.getEquipment();
		return equipment != null && equipment.isTwoHanded();
	}

	private void setGhost(int itemId, EquipmentInventorySlot slot)
	{
		if (itemId == ghostItemId && slot == ghostSlot)
		{
			return;
		}

		log.debug("Ghosting item {} into the {} slot", itemId, slot);
		ghostItemId = itemId;
		ghostSlot = slot;
		shadowImage = null;

		final AsyncBufferedImage image = itemManager.getImage(itemId);
		ghostImage = image;
		if (image != null)
		{
			image.onLoaded(() ->
			{
				if (ghostItemId == itemId)
				{
					shadowImage = ImageUtil.fillImage(image, Color.BLACK);
				}
			});
		}
	}

	private void clearGhost()
	{
		ghostItemId = -1;
		ghostSlot = null;
		ghostImage = null;
		shadowImage = null;
	}
}
