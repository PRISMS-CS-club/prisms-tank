package org.prismsus.tank.bot;

import java.util.concurrent.ExecutionException;

public class RandomMovingBot implements GameBot<FutureController> {
    public void loop(FutureController c) throws ExecutionException, InterruptedException {
        try {
            while (!Thread.interrupted()) {
                Thread.sleep(100);
//                double trackMaxSpeed = c.getTankTrackMaxSpeed().get();
//                double ltrackSpeed = (Math.random() - 1.0) * 2 * trackMaxSpeed;
//                double rtrackSpeed = (Math.random() - 1.0) * 2 * trackMaxSpeed;
                c.setLeftTrackSpeed(-1);
                c.setRightTrackSpeed(1);
            }
        } catch (InterruptedException e) {
            // when the thread is interrupted, stop the bot
            return;
        }
    }
}
