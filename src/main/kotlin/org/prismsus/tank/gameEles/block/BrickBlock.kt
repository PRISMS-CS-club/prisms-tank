package org.prismsus.tank.gameEles.block

class BrickBlock(x: Int, y: Int): Block(x, y) {
    override fun canPass() = false
    override val bulletAction = BulletAction.DESTROY
    override fun canSee() = false
}