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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(OffhandGhostConfig.GROUP)
public interface OffhandGhostConfig extends Config
{
	String GROUP = "offhandghost";

	@ConfigItem(
		keyName = "opacity",
		name = "Opacity",
		description = "How strongly the ghosted item is drawn into the blocked slot",
		position = 1
	)
	@Range(min = 5, max = 100)
	@Units(Units.PERCENT)
	default int opacity()
	{
		return 50;
	}

	@ConfigItem(
		keyName = "dropShadow",
		name = "Drop shadow",
		description = "Draw a shadow behind the ghosted item so it separates from the empty slot",
		position = 2
	)
	default boolean dropShadow()
	{
		return false;
	}

	@ConfigItem(
		keyName = "shadowOpacity",
		name = "Shadow opacity",
		description = "How dark the drop shadow is",
		position = 3
	)
	@Range(min = 0, max = 100)
	@Units(Units.PERCENT)
	default int shadowOpacity()
	{
		return 25;
	}
}
