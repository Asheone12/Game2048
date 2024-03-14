package com.muen.game2048.rxbus.event


/**
 * @author ry
 * @date 2019-07-31
 */

//改变模式或者改变imei时，需要重启activity
class AddScore(var score: Int)
class ClearScore
class GameOver
class Win
