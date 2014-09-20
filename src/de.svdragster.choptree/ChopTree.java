package de.svdragster.choptree;

import net.canarymod.Canary;
import net.canarymod.plugin.Plugin;

public class ChopTree extends Plugin {

	@Override
	public void disable() {

	}

	@Override
	public boolean enable() {
		Canary.hooks().registerListener(new ChopTreeListener(), this);
		return true;
	}

}
