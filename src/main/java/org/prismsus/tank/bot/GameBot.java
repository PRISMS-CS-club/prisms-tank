package org.prismsus.tank.bot;

/**
 * GameBot is the interface for all bots. Your code should implement this interface.
 * @param <T> Type of controller you want to use. This should only be either {@link org.prismsus.tank.bot.Controller}
 *           or {@link org.prismsus.tank.bot.FutureController}.
 */
public interface GameBot<T> {
    /**
     * Run the bot.
     *
     * This function will execute when the game starts and bots loaded. This function will only be called
     * once, and users should implement all code inside this function.
     * @param controller Controller for your bot. All the information about the game map can be accessed
     *                   through this controller, and all the commands for your bot can be sent through
     *                   this controller.
     */
    public void loop(T controller);
}
