package org.prismsus.tank.bot;

import org.prismsus.tank.element.Block;
import org.prismsus.tank.utils.DVec2;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Controller {
    FutureController controller;

    /**
     * Get the position of this bot's tank.
     * The function returns a double vector for its position. Vector (0, 0) is the top left corner of the
     * game map. The x-axis is horizontal and the y-axis is vertical. The x-axis increases from left to
     * right, and the y-axis increases from top to bottom. One unit of the vector is equal to the size of
     * a block.
     * @return Position of this bot's tank as a double vector.
     */
    DVec2 getPos() throws ExecutionException, InterruptedException {
        return controller.getPos().get();
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

    /**
     * Perform some action.
     * @param action Action to perform.
     */
    boolean act(ControllerAction action) throws ExecutionException, InterruptedException {
        return controller.act(action).get();
    }
}
