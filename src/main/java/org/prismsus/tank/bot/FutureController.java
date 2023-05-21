package org.prismsus.tank.bot;

import org.prismsus.tank.elements.GameElement;
import org.prismsus.tank.game.ControllerRequest;
import org.prismsus.tank.game.ControllerRequestTypes;
import org.prismsus.tank.game.TankWeaponInfo;
import org.prismsus.tank.game.OtherRequests;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class FutureController {
    int curAPICallCnt = 114514;
    long cid;
    PriorityQueue<ControllerRequest<?>> requests = new PriorityQueue<>();
    Future<List<GameElement>> getVisibleElements() {
        CompletableFuture<List<GameElement>> ret = new CompletableFuture<>();
        requests.add(new ControllerRequest<>(cid, ret, OtherRequests.GET_VISIBLE_ELEMENTS));
        return ret;
    }
    Future<List<GameElement>> getVisitedElements() {
        CompletableFuture<List<GameElement>> ret = new CompletableFuture<>();
        requests.add(new ControllerRequest<>(cid, ret, OtherRequests.GET_VISITED_ELEMENTS));
        return ret;
    }

    void shoot(int cnt) {
        requests.add(new ControllerRequest<>(cid, null, OtherRequests.SHOOT));
    }

    Future<ArrayList<?>> getTankAndWeaponInfos(TankWeaponInfo... type) {
        CompletableFuture<ArrayList<?>> ret = new CompletableFuture<>();
        for (TankWeaponInfo t : type) {
            requests.add(new ControllerRequest<>(cid, ret, t));
        }
        return ret;
    }

    void setLeftTrackSpeed(double speed) {
        requests.add(new ControllerRequest<>(cid, null, OtherRequests.SET_LTRACK_SPEED, new Double[]{speed}));
    }

    void setRightTrackSpeed(double speed) {
        requests.add(new ControllerRequest<>(cid, null, OtherRequests.SET_RTRACK_SPEED, new Double[]{speed}));
    }

}
