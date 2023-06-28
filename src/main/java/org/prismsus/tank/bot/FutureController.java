package org.prismsus.tank.bot;

import org.prismsus.tank.elements.GameElement;
import org.prismsus.tank.game.ControllerRequest;
import org.prismsus.tank.game.TankWeaponInfo;
import org.prismsus.tank.game.OtherRequests;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;

import org.prismsus.tank.utils.collidable.ColPoly;
import org.prismsus.tank.utils.collidable.ColRect;
import org.prismsus.tank.utils.collidable.DPos2;

public class FutureController {
    public int curAPICallCnt = 114514;
    public long cid;
    PriorityBlockingQueue<ControllerRequest<Object>> requestsQ;

    public FutureController(long cid, PriorityBlockingQueue<ControllerRequest<Object>> requestQ) {
        this.cid = cid;
        this.requestsQ = requestQ;
    }

    Future<List<GameElement>> getVisibleElements() {
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, OtherRequests.GET_VISIBLE_ELEMENTS));
        return ret.thenApply((Object it) -> (List<GameElement>) it);
    }
    Future<List<GameElement>> getVisitedElements() {
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, OtherRequests.GET_VISITED_ELEMENTS));
        return ret.thenApply((Object it) -> (List<GameElement>) it);
    }

    void shoot() {
        requestsQ.add(new ControllerRequest<>(cid, null, OtherRequests.FIRE));
    }

    Future<ArrayList<?>> getTankAndWeaponInfos(TankWeaponInfo... type) {
        CompletableFuture<Object> ret = new CompletableFuture<>();
        for (TankWeaponInfo t : type) {
            requestsQ.add(new ControllerRequest<>(cid, ret, t));
        }
        ControllerRequest<?> test = requestsQ.peek();
        return ret.thenApply((Object it) -> (ArrayList<?>) it);
    }

    Future<Integer> getTankHp(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.TANK_HP));
        return ret.thenApply((Object it) -> (Integer) it);
    }


    Future<Integer> getTankMaxHp(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.TANK_MAX_HP));
        return ret.thenApply((Object it) -> (Integer) it);
    }

    Future<Double> getTankLeftTrackSpeed(){
        // convert using thenApply
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.TANK_LTRACK_SPEED));
        return ret.thenApply((Object it) -> (Double) it);
    }

    Future<Double> getTankRightTrackSpeed(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.TANK_RTRACK_SPEED));
        return ret.thenApply((Object it) -> (Double) it);
    }

    Future<Double> getTankTrackMaxSpeed(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.TANK_TRACK_MAX_SPEED));
        return ret.thenApply((Object it) -> (Double) it);
    }

    Future<ColRect> getTankColBox(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.TANK_COLBOX));
        return ret.thenApply((Object it) -> (ColRect) it);
    }

    Future<DPos2> getTankPos(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.TANK_POS));
        return ret.thenApply((Object it) -> (DPos2) it);
    }

    Future<Double> getTankAngle(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.TANK_ANGLE));
        return ret.thenApply((Object it) -> (Double) it);
    }

    Future<Double> getWeaponReloadTimePerSecond(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.WEAPON_RELOAD_RATE_PER_SEC));
        return ret.thenApply((Object it) -> (Double) it);
    }

    Future<Integer> getWeaponCurCapacity(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.WEAPON_CUR_CAPACITY));
        return ret.thenApply((Object it) -> (Integer) it);
    }

    Future<Integer> getWeaponMaxCapacity(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.WEAPON_MAX_CAPACITY));
        return ret.thenApply((Object it) -> (Integer) it);
    }

    // weapon colbox
    Future<ColRect> getWeaponColBox(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.WEAPON_COLBOX));
        return ret.thenApply((Object it) -> (ColRect) it);
    }

    // combined colbox
    Future<ColPoly> getCombinedColBox() {
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.COMBINED_COLBOX));
        return ret.thenApply((Object it) -> (ColPoly) it);
    }

    // bullet colbox
    Future<ColRect> getBulletColBox(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.BULLET_COLBOX));
        return ret.thenApply((Object it) -> (ColRect) it);
    }

    // bullet speed
    Future<Double> getBulletSpeed(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.BULLET_SPEED));
        return ret.thenApply((Object it) -> (Double) it);
    }


    void setLeftTrackSpeed(double speed) {
        requestsQ.add(new ControllerRequest<>(cid, null, OtherRequests.SET_LTRACK_SPEED, new Double[]{speed}));
    }

    void setRightTrackSpeed(double speed) {
        requestsQ.add(new ControllerRequest<>(cid, null, OtherRequests.SET_RTRACK_SPEED, new Double[]{speed}));
    }

}
