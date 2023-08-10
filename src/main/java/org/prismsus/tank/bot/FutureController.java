package org.prismsus.tank.bot;

import org.prismsus.tank.elements.Block;
import org.prismsus.tank.elements.Bullet;
import org.prismsus.tank.elements.GameElement;
import org.prismsus.tank.elements.Tank;
import org.prismsus.tank.game.ControllerRequest;
import org.prismsus.tank.game.TankWeaponInfo;
import org.prismsus.tank.game.OtherRequests;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;

import org.prismsus.tank.markets.AuctionProcessor;
import org.prismsus.tank.markets.AuctionUserInterface;
import org.prismsus.tank.utils.collidable.ColPoly;
import org.prismsus.tank.utils.collidable.ColRect;
import org.prismsus.tank.utils.collidable.DPos2;
import org.prismsus.tank.utils.IVec2;

public class FutureController {
    private int curAPICallCnt = 114514;
    private long cid;
    public final AuctionUserInterface market;
    PriorityBlockingQueue<ControllerRequest<Object>> requestsQ;
    private int threadCount = 0;
    public int getThreadCount() {
        return threadCount;
    }

    public long getCid() {
        return cid;
    }

    public final static int MAX_THREAD = 4;

    public Thread createThread(Runnable th){
        if (threadCount >= MAX_THREAD) return null;
        threadCount++;
        return new Thread(th);
    }


    public FutureController(long cid, PriorityBlockingQueue<ControllerRequest<Object>> requestQ, AuctionUserInterface market) {
        this.cid = cid;
        this.requestsQ = requestQ;
        this.market = market;
    }

    public Future<List<GameElement>> getVisibleElements() {
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, OtherRequests.GET_VISIBLE_ELEMENTS));
        return ret.thenApply((Object it) -> (List<GameElement>) it);
    }

    public Future<List<Tank>> getVisibleTanks() {
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, OtherRequests.GET_VISIBLE_TANKS));
        return ret.thenApply((Object it) -> (List<Tank>) it);
    }

    public Future<List<Bullet>> getVisibleBullets(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, OtherRequests.GET_VISIBLE_BULLETS));
        return ret.thenApply((Object it) -> (List<Bullet>) it);
    }

    public Future<List<GameElement>> getVisitedElements() {
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, OtherRequests.GET_VISITED_ELEMENTS));
        return ret.thenApply((Object it) -> (List<GameElement>) it);
    }

    public Future<Block> checkBlockAt(IVec2 pos){
        CompletableFuture<Object> ret = new CompletableFuture<>();

        requestsQ.add(new ControllerRequest<>(cid, ret, OtherRequests.CHECK_BLOCK_AT, new IVec2[]{pos} ));
        // convert type if non-null
        return ret.thenApply((Object it) -> {
            if (it == null) return null;
            else return (Block) it;
        });
    }

    public Future<ArrayList<GameElement>> checkCollidingGameEles(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, OtherRequests.CHECK_COLLIDING_GAME_ELES));
        return ret.thenApply((Object it) -> {
            if (it == null) return null;
            else return (ArrayList<GameElement>) it;
        });
    }

    public void fire() {
        requestsQ.add(new ControllerRequest<>(cid, null, OtherRequests.FIRE));
    }

    public Future<ArrayList<?>> getTankAndWeaponInfos(TankWeaponInfo... type) {
        CompletableFuture<Object> ret = new CompletableFuture<>();
        for (TankWeaponInfo t : type) {
            requestsQ.add(new ControllerRequest<>(cid, ret, t));
        }
        ControllerRequest<?> test = requestsQ.peek();
        return ret.thenApply((Object it) -> (ArrayList<?>) it);
    }

    public Future<Integer> getTankHp(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.TANK_HP));
        return ret.thenApply((Object it) -> (Integer) it);
    }


    public Future<Integer> getTankMaxHp(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.TANK_MAX_HP));
        return ret.thenApply((Object it) -> (Integer) it);
    }

    public Future<Double> getTankLeftTrackSpeed(){
        // convert using thenApply
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.TANK_LTRACK_SPEED));
        return ret.thenApply((Object it) -> (Double) it);
    }

    public Future<Double> getTankRightTrackSpeed(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.TANK_RTRACK_SPEED));
        return ret.thenApply((Object it) -> (Double) it);
    }

    public Future<Double> getTankTrackMaxSpeed(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.TANK_TRACK_MAX_SPEED));
        return ret.thenApply((Object it) -> (Double) it);
    }

    public Future<ColRect> getTankColBox(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.TANK_COLBOX));
        return ret.thenApply((Object it) -> (ColRect) it);
    }

    public Future<DPos2> getTankPos(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.TANK_POS));
        return ret.thenApply((Object it) -> (DPos2) it);
    }

    public Future<Double> getTankAngle(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.TANK_ANGLE));
        return ret.thenApply((Object it) -> (Double) it);
    }

    public Future<Double> getWeaponReloadTimePerSecond(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.WEAPON_RELOAD_RATE_PER_SEC));
        return ret.thenApply((Object it) -> (Double) it);
    }

    public Future<Integer> getWeaponCurCapacity(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.WEAPON_CUR_CAPACITY));
        return ret.thenApply((Object it) -> (Integer) it);
    }

    public Future<Integer> getWeaponMaxCapacity(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.WEAPON_MAX_CAPACITY));
        return ret.thenApply((Object it) -> (Integer) it);
    }

    // weapon colbox
    public Future<ColRect> getWeaponColBox(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.WEAPON_COLBOX));
        return ret.thenApply((Object it) -> (ColRect) it);
    }

    // combined colbox
    public Future<ColPoly> getCombinedColBox() {
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.COMBINED_COLBOX));
        return ret.thenApply((Object it) -> (ColPoly) it);
    }

    // bullet colbox
    public Future<ColRect> getBulletColBox(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.BULLET_COLBOX));
        return ret.thenApply((Object it) -> (ColRect) it);
    }

    // bullet speed
    public Future<Double> getBulletSpeed(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.BULLET_SPEED));
        return ret.thenApply((Object it) -> (Double) it);
    }

    public Future<Double> getTankVisibleRange(){
        CompletableFuture<Object> ret = new CompletableFuture<>();
        requestsQ.add(new ControllerRequest<>(cid, ret, TankWeaponInfo.TANK_VIS_RANGE));
        return ret.thenApply((Object it) -> (Double) it);
    }


    public void setLeftTrackSpeed(double speed) {
        requestsQ.add(new ControllerRequest<>(cid, null, OtherRequests.SET_LTRACK_SPEED, new Double[]{speed}));
    }

    public void setRightTrackSpeed(double speed) {
        requestsQ.add(new ControllerRequest<>(cid, null, OtherRequests.SET_RTRACK_SPEED, new Double[]{speed}));
    }

}
