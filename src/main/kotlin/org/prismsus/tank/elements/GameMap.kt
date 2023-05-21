package org.prismsus.tank.elements
import kotlinx.serialization.json.*
import org.prismsus.tank.utils.ELE_SERIAL_NAME_TO_CLASS
import org.prismsus.tank.utils.IVec2
import org.prismsus.tank.utils.nextUid
import kotlin.reflect.full.*
/**
 * The game map containing all blocks.
 */
class GameMap(val FileName : String) {
    var blocks : Array<Array<Block?>>
    var width: Int = 0
    var height: Int = 0
    val gameEles : ArrayList<GameElement> = ArrayList()
    val tks : ArrayList<Tank> = ArrayList()
    val movables : ArrayList<MovableElement> = ArrayList()

    fun addEle(ele : GameElement){
        gameEles.add(ele)
        if (ele is Tank){
            tks.add(ele)
        }
        if (ele is MovableElement){
            movables.add(ele)
        }
    }

    init{
        val fileText = GameMap::class.java.getResource(FileName).readText()
        val jsonEle : JsonElement = Json.parseToJsonElement(fileText)
        width = jsonEle.jsonObject["x"]!!.jsonPrimitive.int
        height = jsonEle.jsonObject["y"]!!.jsonPrimitive.int
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
            }
        }
    }

    val serialized : ByteArray
        get() {
            val origFileText = GameMap::class.java.getResource(FileName).readText()
            return origFileText.toByteArray()
        }

}