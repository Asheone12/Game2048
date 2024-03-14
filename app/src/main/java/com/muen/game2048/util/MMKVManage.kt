package com.muen.game2048.util

import com.tencent.mmkv.MMKV

object MMKVManage {
    private val mmkv = MMKV.defaultMMKV()

    //缓存变量
    private const val KEY_HIGHEST_SCORE = "highest_score"

    /**
     * 记录最高分
     */
    var highestScore: Int
        set(value) {
            mmkv.encode(KEY_HIGHEST_SCORE, value)
        }
        get() = mmkv.decodeInt(KEY_HIGHEST_SCORE)

}