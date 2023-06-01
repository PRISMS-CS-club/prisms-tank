package org.prismsus.tank.bot;

import java.util.concurrent.ExecutionException;

public class RandomMovingBot implements GameBot<FutureController> {
    public void loop(FutureController c) throws ExecutionException, InterruptedException {
        while(true){
            Thread.sleep(50);
            double trackMaxSpeed = c.getTankTrackMaxSpeed().get();
            double ltrackSpeed = (Math.random() - 1.0) * 2 * trackMaxSpeed;
            double rtrackSpeed = (Math.random() - 1.0) * 2 * trackMaxSpeed;
            c.setLeftTrackSpeed(ltrackSpeed);
            c.setRightTrackSpeed(rtrackSpeed);
        }
    }
}
