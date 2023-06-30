package org.prismsus.tank.bot;

import java.util.concurrent.ExecutionException;

/**
 * GameBot is the interface for all bots. Your code should implement this interface.
 */
public interface GameBot {
    /**
     * Run the bot.
     *
     * This function will execute when the game starts and bots loaded. This function will only be called
     * once, and users should implement all code inside this function.
     * @param controller Controller for your bot. All the information about the game map can be accessed
     *                   through this controller, and all the commands for your bot can be sent through
     *                   this controller.
     */
    default void loop(FutureController controller) throws ExecutionException, InterruptedException {}
    // whenever there is an interruptedException, this function should return immediately.
    default void loop(Controller controller) throws ExecutionException, InterruptedException {}
    String getName();
    boolean isFutureController();
}
