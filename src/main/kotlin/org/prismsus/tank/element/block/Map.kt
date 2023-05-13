package org.prismsus.tank.element.block
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.prismsus.tank.utils.ELE_SERIAL_NAME_TO_CLASS

/**
 * The game map containing all blocks.
 */
class Map(val FileName : String) {
    var blocks : Array<Array<Block>> = arrayOf()
    var width: Int = 0
    var height: Int = 0
    init{
        val jsonEle : JsonElement = Json.parseToJsonElement(FileName)
        width = jsonEle.jsonObject["x"]!!.jsonPrimitive.int
        height = jsonEle.jsonObject["y"]!!.jsonPrimitive.int
        var tmpArr : JsonArray = jsonEle.jsonObject["map"]!!.jsonArray
        for (x in 0 until height){
            for (y in 0 until width){
                // we want to store blocks array in x, y order
                // but the json file is in i, j order
                // meaning that with greater j index, y index is smaller
                var j = height - 1 - x
                var eleType = ELE_SERIAL_NAME_TO_CLASS[tmpArr[j * height + x].jsonObject["type"]!!.jsonPrimitive.content]!!
                // use primary constructor to create a new instance
                var ele = eleType.constructors.first().call()
                blocks[x][y] = ele as Block
            }
        }
    }

}