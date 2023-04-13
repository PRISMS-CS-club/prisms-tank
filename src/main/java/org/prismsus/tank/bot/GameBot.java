package org.prismsus.tank.bot;

public interface GameBot {
    /**
     * Run the bot.
     *
     * This function will execute when the game starts and bots loaded. This function will only be called
     * once, and users should implement all code inside this function.
     */
    void run();
}
