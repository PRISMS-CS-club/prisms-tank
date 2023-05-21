package org.prismsus.tank.bot;

import org.prismsus.tank.elements.Block;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Controller {
    FutureController controller;

    void setLeftTrackSpeed(double speed) throws ExecutionException, InterruptedException {
        controller.setLeftTrackSpeed(speed);
    }


    /**
     * Get all bot's visible blocks excluding empty blocks. The function returns a list of `Block` objects.
     * Bot's vision may be blocked by other opaque blocks.
     * @return List of all visible blocks.
     */
    List<Block> getVisibleBlocks() throws ExecutionException, InterruptedException {
        return controller.getVisibleBlocks().get();
    }

    /**
     * Get all bot's visited blocks. The function returns a list of `Block` objects.
     * Note: The list of visited blocks is not updated in real time. Only the currently visited blocks are
     * updated in time. Other blocks will always remain same as the last time the bot visited them unless
     * these blocks enter the bot's vision again.
     * @return List of all visited blocks.
     */
    List<Block> getVisitedBlocks() throws ExecutionException, InterruptedException {
        return controller.getVisitedBlocks().get();
    }
}
