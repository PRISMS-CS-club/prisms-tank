package org.prismsus.tank.elements

import kotlinx.serialization.json.*
import org.prismsus.tank.utils.*
import org.prismsus.tank.utils.collidable.ColAARect
import org.prismsus.tank.utils.collidable.ColTreeSet
import org.prismsus.tank.utils.collidable.Collidable
import org.prismsus.tank.utils.collidable.DPos2
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.full.*

/**
 * The game map containing all blocks.
 */
class GameMap(val fileName: String) {
    val blocks: Array<Array<Block?>>
    val width: Int
    val height: Int
    val gameEles: ArrayList<GameElement> = ArrayList()
    val tanks: ArrayList<Tank> = ArrayList()
    val movables: ArrayList<MovableElement> = ArrayList() // TODO: use a better data structure, such as mutable set
    val timeUpdatables: ArrayList<TimeUpdatable> = ArrayList()
    val bullets: ArrayList<Bullet> = ArrayList()
    val quadTree: ColTreeSet
    val collidableToEle = mutableMapOf<Collidable, GameElement>()
    val lastUid: Long
        get() {
            if (gameEles.size == 0)
                return -1
            return gameEles.last().uid
        }

    val randPos: DPos2
        get() = DPos2(Random().nextDouble() * width, Random().nextDouble() * height)


    val emptyBlkPoses: Array<IPos2>
        get() {
            val ret = ArrayList<IPos2>()
            for (x in 0 until width) {
                for (y in 0 until height) {
                    if (blocks[x][y] == null) {
                        ret.add(IPos2(x, y))
                    }
                }
            }
            return ret.toTypedArray()
        }

    val emptyBlkCenters  :Array<DPos2>
        get() {
            val ret = ArrayList<DPos2>()
            for (blkPos in emptyBlkPoses){
                ret.add((blkPos.toDVec2() + DEF_BLOCK_COLBOX.size / 2.0).toPt())
            }
            return ret.toTypedArray()
        }

    fun getUnoccupiedRandPos(box: Collidable): DPos2? {
        // randome shuffle the value from emptyBlkCenters
        val randPoses = emptyBlkCenters ; randPoses.shuffle()
        for (pos in randPoses){
            val boxAtpos = box.copy()
            boxAtpos.rotationCenter = pos
            if (quadTree collide boxAtpos)
                continue
            return pos
        }
        return null
    }

    fun addEle(ele: GameElement): GameElement {
        collidableToEle[ele.colPoly] = ele
        if (!gameEles.add(ele)) {
            throw Exception("failed to add game element")
        }
        if (!quadTree.insert(ele.colPoly)){
            throw Exception("failed to insert into quad tree")
        }
        if (ele is Tank) {
            if (!tanks.add(ele)) {
                throw Exception("failed to add tank")
            }
        }
        if (ele is Bullet) {
            if (!bullets.add(ele)) {
                throw Exception("failed to add bullet")
            }
        }
        if (ele is TimeUpdatable) {
            if (!timeUpdatables.add(ele)) {
                throw Exception("failed to add time updatable")
            }
        }
        if (ele is MovableElement) {
            if (!movables.add(ele)) {
                throw Exception("failed to add movable element")
            }
        }

        if (ele is Block){
            assert(blocks[ele.pos.x][ele.pos.y] == null, {"block already exists at ${ele.pos}"})
            blocks[ele.pos.x][ele.pos.y] = ele
        }

        return ele
    }

    fun remEle(ele: GameElement): GameElement {
        if (quadTree.size != gameEles.size)
            assert(quadTree.size == gameEles.size, {"quad tree size: ${quadTree.size}, gameEles size: ${gameEles.size}"})
        collidableToEle.remove(ele.colPoly)
        if (!gameEles.remove(ele)) {
            throw Exception("failed to remove game element")
        }
        quadTree.remove(ele.colPoly)
        if (ele is Tank) {
            if (!tanks.remove(ele)) {
                throw Exception("failed to remove tank")
            }
        }
        if (ele is Bullet) {
            if (!bullets.remove(ele)) {
                throw Exception("failed to remove bullet")
            }
        }
        if (ele is TimeUpdatable) {
            if (!timeUpdatables.remove(ele)) {
                throw Exception("failed to remove time updatable")
            }
        }
        if (ele is MovableElement) {
            if (!movables.remove(ele)) {
                throw Exception("failed to remove movable element")
            }
        }

        if (ele is Block){
            assert(blocks[ele.pos.x][ele.pos.y] == ele, {"block does not exist at ${ele.pos}"})
            blocks[ele.pos.x][ele.pos.y] = null
        }

        return ele
    }

    fun scanAndRemEle(){
        val toRemove = gameEles.filter { it.removeStat == GameElement.RemoveStat.TO_REMOVE }
        for (ele in toRemove){
            remEle(ele)
        }
    }

    init {
        val fileText = GameMap::class.java.getResource(fileName).readText()
        val jsonEle: JsonElement = Json.parseToJsonElement(fileText)
        width = jsonEle.jsonObject["x"]!!.jsonPrimitive.int
        height = jsonEle.jsonObject["y"]!!.jsonPrimitive.int
        val blPt = DPos2(-DOUBLE_PRECISION * 100, -DOUBLE_PRECISION * 100)
        quadTree =
            ColTreeSet(0, ColAARect.byBottomLeft(blPt, DDim2(width.toDouble(), height.toDouble()) + (blPt.toVec() * 2.0).abs()))
        blocks = Array(width) { Array(height) { null } }
        var tmpArr: JsonArray = jsonEle.jsonObject["map"]!!.jsonArray
        for (x in 0 until height) {
            for (y in 0 until width) {
                // we want to store blocks array in x, y order
                // but the json file is in i, j order
                // meaning that with greater j index, y index is smaller
                val j = height - 1 - y
                val serialName = tmpArr[j * height + x].jsonPrimitive.content
                if (serialName.isEmpty())
                    continue
                val eleType = ELE_SERIAL_NAME_TO_CLASS[serialName]!!
                // use primary constructor to create a new instance
                val pos = IVec2(x, y)
                var ele: Block? = null
                if (eleType.isSubclassOf(Block::class)) {
                    ele = eleType.constructors.first().call(nextUid, pos)
                }
                
                ele?.run {
                    addEle(ele)
                }
            }
        }
        gameMap = this
    }

    val serialized: ByteArray
        get() {
            val origFileText = GameMap::class.java.getResource(fileName)!!.readText()
            return origFileText.toByteArray()
        }

}