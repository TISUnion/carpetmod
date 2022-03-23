package redstone.multimeter.interfaces;

import carpet.settings.CarpetSettings;

import redstone.multimeter.common.TickTask;
import redstone.multimeter.server.MultimeterServer;

public interface MultimeterServerProvider extends TickTaskExecutor {
	
	@Override
	default void startTickTask(boolean updateTree, TickTask task, String... args) {
		if (CarpetSettings.modRedstoneMultimeter) {
		    getMultimeterServer().startTickTask(updateTree, task, args);
		}
	}
	
	@Override
	default void endTickTask(boolean updateTree) {
	    if (CarpetSettings.modRedstoneMultimeter) {
	        getMultimeterServer().endTickTask(updateTree);
	    }
	}
	
	@Override
	default void swapTickTask(boolean updateTree, TickTask task, String... args) {
	    if (CarpetSettings.modRedstoneMultimeter) {
	        getMultimeterServer().swapTickTask(updateTree, task, args);
	    }
	}
	
	public MultimeterServer getMultimeterServer();
	
}
