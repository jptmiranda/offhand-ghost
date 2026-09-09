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

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

class OffhandGhostOverlay extends Overlay
{
	private static final int SHADOW_X_OFFSET = 1;
	private static final int SHADOW_Y_OFFSET = 2;

	private final Client client;
	private final OffhandGhostPlugin plugin;
	private final OffhandGhostConfig config;
	private final int weaponComponent;
	private final int shieldComponent;

	OffhandGhostOverlay(
		Client client,
		OffhandGhostPlugin plugin,
		OffhandGhostConfig config,
		int interfaceId,
		int weaponComponent,
		int shieldComponent)
	{
		super(plugin);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.weaponComponent = weaponComponent;
		this.shieldComponent = shieldComponent;

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.MANUAL);
		setPriority(PRIORITY_LOW);
		drawAfterInterface(interfaceId);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		final EquipmentInventorySlot slot = plugin.getGhostSlot();
		if (slot == null)
		{
			return null;
		}

		final BufferedImage image = plugin.getGhostImage();
		if (image == null)
		{
			return null;
		}

		final Widget target = client.getWidget(
			slot == EquipmentInventorySlot.SHIELD ? shieldComponent : weaponComponent);
		if (target == null || target.isHidden())
		{
			return null;
		}

		final Rectangle bounds = itemBounds(target);
		if (bounds == null)
		{
			return null;
		}

		final int x = bounds.x + (bounds.width - image.getWidth()) / 2;
		final int y = bounds.y + (bounds.height - image.getHeight()) / 2;

		final Composite original = graphics.getComposite();

		final BufferedImage shadow = plugin.getShadowImage();
		if (config.dropShadow() && shadow != null)
		{
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				percent(config.shadowOpacity())));
			graphics.drawImage(shadow, x + SHADOW_X_OFFSET, y + SHADOW_Y_OFFSET, null);
		}

		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
			percent(config.opacity())));
		graphics.drawImage(image, x, y, null);

		graphics.setComposite(original);
		return null;
	}

	// Offset from the slot's canvas bounds rather than read off the item child itself: canvas
	// coordinates are only filled in for widgets the client actually draws, and an empty slot's
	// item child reports (-1, -1).
	private static Rectangle itemBounds(Widget slot)
	{
		final Rectangle slotBounds = slot.getBounds();
		if (slotBounds.width <= 0 || slotBounds.height <= 0)
		{
			return null;
		}

		final Widget[] children = slot.getDynamicChildren();
		if (children != null)
		{
			for (Widget child : children)
			{
				if (child != null && child.getSpriteId() == -1
					&& child.getWidth() > 0 && child.getHeight() > 0)
				{
					return new Rectangle(
						slotBounds.x + child.getRelativeX(),
						slotBounds.y + child.getRelativeY(),
						child.getWidth(),
						child.getHeight());
				}
			}
		}

		return slotBounds;
	}

	private static float percent(int value)
	{
		return Math.min(1f, Math.max(0f, value / 100f));
	}
}
