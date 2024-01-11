package org.prismsus.tank.elements

import org.prismsus.tank.event.PlayerUpdateEvent
import org.prismsus.tank.event.UpdateEventMask
import org.prismsus.tank.markets.UpgradeEntry
import org.prismsus.tank.markets.UpgradeEntry.*
import org.prismsus.tank.markets.UpgradeRecord
import org.prismsus.tank.utils.*
import org.prismsus.tank.utils.collidable.*
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign

class Tank(
    uid: Long,
    val playerName: String,
    weaponProps: WeaponProps,
    var trackMaxSpeed: Double = INIT_TANK_TRACK_SPEED,
    hp: Int = INIT_TANK_HP,
    val tankRectBox: ColRect = INIT_TANK_COLBOX,
    var visibleRange : Double = INIT_TANK_VIS_RANGE,
    var money : Int = INIT_TANK_MONEY
) : MovableElement(
        uid, hp, ColMultiPart((tankRectBox), (weaponProps.colPoly))
    ) {
    var maxHp = hp
    var weapon: Weapon = weaponProps.toWeapon(this)
    constructor() : this(0, "", INIT_RECT_WEAPON_RPOPS) // for serialization
    var leftTrackVelo: Double = .0
        set(value) {
            field = sign(value) * min(abs(value), trackMaxSpeed)
        }
    var rightTrackVelo: Double = .0
        set(value) {
            field = sign(value) * min(abs(value), trackMaxSpeed)
        }


    /*
    * The turning of tank can be modeled as two concentric circles with different radius
    * The inner circle is the track with smaller speed
    * The outer circle is the track with larger speed, hence the tank will turn to the direction of the track with smaller speed
    * This function returns the radius of the inner circle
    * If the return value is negative, means the center of rotation is in the body of tank
    * Assume the distance between two tracks is 1
    * */
    val innerTurningRad: Double
        get() {
            val trackDis = 1.0
            return trackDis * inVelo / (outVelo - inVelo)
        }
    val outerTurningRad: Double
        get() {
            val trackDis = 1.0
            return trackDis * outVelo / (outVelo - inVelo)
        }

    fun isInnerCircLeft(): Boolean {
        return abs(leftTrackVelo) < abs(rightTrackVelo)
    }

    val inVelo: Double
        get() = if (isInnerCircLeft()) leftTrackVelo else rightTrackVelo
    val outVelo: Double
        get() = if (isInnerCircLeft()) rightTrackVelo else leftTrackVelo
    override fun updateByTime(dt: Long) {
        val ddt = dt / 1000.0 // convert to second
        if (leftTrackVelo errEQ 0.0 && rightTrackVelo errEQ 0.0) return
        if (leftTrackVelo errEQ rightTrackVelo) {
            // the tank is moving straight
            val dirVec = (tankRectBox.topMidPt - tankRectBox.bottomMidPt).norm()
            velocity = dirVec * leftTrackVelo
            val disp = velocity * ddt
            colPoly += disp
            return
        }
        if (abs(leftTrackVelo) errEQ abs(rightTrackVelo)) {
            // the tank is rotating in place, left and right track speed must have different sign
            // meaning that the rotation center is the rotation center of the tank
            val angSign = if (rightTrackVelo > 0) 1 else -1
            angVelocity = abs(leftTrackVelo / .5)
            val angDisp = angVelocity * ddt
            colPoly.rotateAssign(angDisp * angSign, tankRectBox.rotationCenter)
            return
        }

        val pivotBaseLine = if (isInnerCircLeft()) Line(tankRectBox.rightMidPt, tankRectBox.leftMidPt)
        else Line(tankRectBox.leftMidPt, tankRectBox.rightMidPt)
        val pivotPt =
            pivotBaseLine.atT(outerTurningRad)
        // change to outerTurningRad - based, this will prevent the situation where inner velocity is zero
        val angSign = if (leftTrackVelo - rightTrackVelo > 0) -1 else 1
        angVelocity = abs(outVelo / outerTurningRad)
        val angDisp = angVelocity * ddt
        colPoly.rotateAssign(angDisp * angSign, pivotPt)
    }

    override val serialName: String
        get() = "Tk"

    override fun willMove(dt: Long): Boolean {
        if (dt == 0.toLong()) return false
        return leftTrackVelo errNE 0.0 || rightTrackVelo errNE 0.0
    }

    override fun colPolyAfterMove(dt: Long): ColMultiPart {
        val before = colPoly.copy() as ColMultiPart
        updateByTime(dt)
        val after = colPoly.copy()
        colPoly.becomeNonCopy(before)
        if (willMove(dt))
            assert(after != before)
        else assert(after == before)
        return after as ColMultiPart
    }

    override fun processCollision(other: GameElement): UpdateEventMask {
        val ret = super.processCollision(other)
        if (removeStat != RemoveStat.TO_REMOVE && hp > maxHp)
            hp = maxHp
        return ret
    }

    fun processUpgrade(upg : UpgradeRecord<out Number>) : PlayerUpdateEvent{
        var finalVal : Number = 0
        var origVal : Number = 0
        var propSetter : (Number) -> Unit = {}
        when (upg.type){
            UpgradeType.MONEY -> {
                origVal = money
                propSetter = {money = it.toInt()}
            }
            UpgradeType.MAX_HP -> {
                origVal = maxHp
                propSetter = {maxHp = it.toInt()}
            }
            UpgradeType.VIS_RADIUS -> {
                origVal = visibleRange
                propSetter = {visibleRange = it.toDouble()}
            }
            UpgradeType.TANK_BODY_AREA -> {
                TODO("impl")
            }
            UpgradeType.TANK_BODY_EDGE_CNT -> {
                TODO("impl")
            }
            UpgradeType.TANK_SPEED -> {
                origVal = trackMaxSpeed
                propSetter = {trackMaxSpeed = it.toDouble()}
            }
            UpgradeType.API_TOKEN_CNT -> {
                TODO()
            }
            UpgradeType.WEAPON_DAMAGE -> {
                origVal = weapon.damage
                propSetter = {weapon.damage = it.toInt()}
            }
            UpgradeType.WEAPON_LAUNCH_MIN_INTERV -> {
                origVal = weapon.minInterv
                propSetter = {weapon.minInterv = it.toInt()}
            }
            UpgradeType.WEAPON_CAPACITY -> {
                origVal = weapon.maxCapacity
                propSetter = {weapon.maxCapacity = it.toInt()}
            }
            UpgradeType.WEAPON_RELOAD_RATE -> {
                origVal = weapon.reloadRate
                propSetter = {weapon.reloadRate = it.toDouble()}
            }
            UpgradeType.WEAPON_BULLET_SPEED -> {
                origVal = weapon.bulletProps.speed
                propSetter = {weapon.bulletProps.speed = it.toDouble()}
            }
            UpgradeType.WEAPON_BULLET_WIDTH -> {
                TODO()
            }
        }
        if (upg.type.isIntValue)
            finalVal = upg.value.toInt() + if (upg.isInc) origVal.toInt() else 0
        else
            finalVal = upg.value.toDouble() + if (upg.isInc) origVal.toDouble() else 0.0
        val newUpg =  UpgradeRecord(upg.type, upg.isInc, finalVal, upg.cid, upg.timeStamp)
        // don't know why can't use upg.copy(value=finalVal)
        propSetter(finalVal)
        return PlayerUpdateEvent(uid, game!!.elapsedGameMs, newUpg)
    }

    companion object {
        fun byInitPos(
            uid: Long,
            initPos: DPos2,
            playerName: String,
            weaponProps: WeaponProps = INIT_RECT_WEAPON_RPOPS.copy(),
            trackMaxSpeed: Double = INIT_TANK_TRACK_SPEED,
            hp: Int = INIT_TANK_HP,
            rectBox: ColRect = INIT_TANK_COLBOX.copy()
        ): Tank {
            rectBox.rotationCenter = initPos.copy()
            weaponProps.colPoly.rotationCenter =
                initPos + weaponProps.offsetFromParentCenter.xVec * rectBox.size.x / 2.0 + weaponProps.offsetFromParentCenter.yVec * rectBox.size.y / 2.0
            return Tank(uid, playerName, weaponProps, trackMaxSpeed, hp, rectBox)
        }
    }
}
