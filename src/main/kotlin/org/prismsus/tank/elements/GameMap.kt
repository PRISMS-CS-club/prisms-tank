package org.prismsus.tank.elements
import kotlinx.serialization.json.*
import org.prismsus.tank.utils.*
import org.prismsus.tank.utils.collidable.ColAArect
import org.prismsus.tank.utils.collidable.ColTreeSet
import org.prismsus.tank.utils.collidable.Collidable
import org.prismsus.tank.utils.collidable.DPos2
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.full.*
/**
 * The game map containing all blocks.
 */
class GameMap(val FileName : String) {
    val blocks : Array<Array<Block?>>
    val width : Int
    val height: Int
    val gameEles : ArrayList<GameElement> = ArrayList()
    val tks : ArrayList<Tank> = ArrayList()
    val movables : ArrayList<MovableElement> = ArrayList() // TODO: use a better data structure, such as mutable set
    val timeUpdatables : ArrayList<TimeUpdatable> = ArrayList()
    val bullets : ArrayList<Bullet> = ArrayList()
    val quadTree : ColTreeSet
    val collidableToEle = mutableMapOf<Collidable, GameElement>()
    val lastUid : Long
        get() {
            if (gameEles.size == 0)
                return -1
            return gameEles.last().uid
        }

    fun getRandPos() : DPos2{
        return DPos2(Random().nextDouble() * width, Random().nextDouble() * height)
    }


    fun getUnoccupiedRandPos(box : Collidable) : DPos2{
        var pos = getRandPos()
        while(true){
            box.rotationCenter = pos
            if (!quadTree.collide(box))
                break
        }
        return pos

    }

    fun addEle(ele : GameElement) : GameElement{
        collidableToEle[ele.colPoly] = ele
        if (!gameEles.add(ele)){
            throw Exception("failed to add game element")
        }
        quadTree.insert(ele.colPoly)
        if (ele is Tank){
            if (!tks.add(ele)){
                throw Exception("failed to add tank")
            }
        }
        if (ele is Bullet){
            if (!bullets.add(ele)){
                throw Exception("failed to add bullet")
            }
        }
        if (ele is TimeUpdatable){
            if (!timeUpdatables.add(ele)){
                throw Exception("failed to add time updatable")
            }
        }
        if (ele is MovableElement){
            if (!movables.add(ele)){
                throw Exception("failed to add movable element")
            }
        }
        return ele
    }

    fun remEle(ele: GameElement) : GameElement{
        collidableToEle.remove(ele.colPoly)
        if (!gameEles.remove(ele)){
            throw Exception("failed to remove game element")
        }
        quadTree.remove(ele.colPoly)
        if (ele is Tank){
            if (!tks.remove(ele)){
                throw Exception("failed to remove tank")
            }
        }
        if (ele is Bullet){
            if (!bullets.remove(ele)){
                throw Exception("failed to remove bullet")
            }
        }
        if (ele is TimeUpdatable){
            if (!timeUpdatables.remove(ele)){
                throw Exception("failed to remove time updatable")
            }
        }
        if (ele is MovableElement){
            if (!movables.remove(ele)){
                throw Exception("failed to remove movable element")
            }
        }
        return ele
    }

    init{
        val fileText = GameMap::class.java.getResource(FileName).readText()
        val jsonEle : JsonElement = Json.parseToJsonElement(fileText)
        width = jsonEle.jsonObject["x"]!!.jsonPrimitive.int
        height = jsonEle.jsonObject["y"]!!.jsonPrimitive.int
        val blPt = DPos2(-DOUBLE_PRECISION * 100, -DOUBLE_PRECISION * 100)
        quadTree = ColTreeSet(5, ColAArect.byBottomLeft(blPt, DDim2(width.toDouble(), height.toDouble()) - blPt.toVec() * 2.0))
        blocks = Array(width){Array(height){null}}
        var tmpArr : JsonArray = jsonEle.jsonObject["map"]!!.jsonArray
        for (x in 0 until height){
            for (y in 0 until width){
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
                var ele : Block? = null
                if (eleType.isSubclassOf(Block::class)){
                    ele = eleType.constructors.first().call(nextUid, pos)
                }

                blocks[x][y] = ele
                ele?.run {
                    quadTree.insert(ele.colPoly)
                }
            }
        }
    }

    val serialized : ByteArray
        get() {
            val origFileText = GameMap::class.java.getResource(FileName).readText()
            return origFileText.toByteArray()
        }

}