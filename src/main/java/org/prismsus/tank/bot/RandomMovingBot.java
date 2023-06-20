package org.prismsus.tank.bot;

import java.util.concurrent.ExecutionException;

public class RandomMovingBot implements GameBot {
    public void loop(FutureController c) throws ExecutionException, InterruptedException {
        try {
            c.setLeftTrackSpeed(-.5);
            c.setRightTrackSpeed(1);
            while (!Thread.interrupted()) {
                Thread.sleep(50);
//                double trackMaxSpeed = c.getTankTrackMaxSpeed().get();
//                double ltrackSpeed = (Math.random() - 1.0) * 2 * trackMaxSpeed;
//                double rtrackSpeed = (Math.random() - 1.0) * 2 * trackMaxSpeed;
                c.shoot();
            }
        } catch (InterruptedException e) {
            // when the thread is interrupted, stop the bot
            return;
        }
    }

    @Override
    public String getName() {
        return "RandomMovingBot";
    }

    @Override
    public boolean isUseFutureController() {
        return true;
    }
}
