package com.muen.game2048.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.animation.ScaleAnimation
import android.widget.GridLayout
import com.muen.game2048.R
import com.muen.game2048.entity.Point
import com.muen.game2048.rxbus.RxBus
import com.muen.game2048.rxbus.event.AddScore
import com.muen.game2048.rxbus.event.ClearScore
import com.muen.game2048.rxbus.event.GameOver
import com.muen.game2048.rxbus.event.Win
import java.util.Random
import kotlin.math.abs


class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : GridLayout(context, attrs, defStyleAttr) {
    companion object {
        const val CARD_COUNT = 4        //游戏界面的行数和列数，数字越高难度越小
        const val CARD_NUMBER = 2  //生成卡片的卡面数字大小,需要是2的阶级，且>=2，数字越高难度越小
    }

    private val cards = Array(CARD_COUNT) {
        arrayOfNulls<Card>(
            CARD_COUNT
        )
    }

    init {
        initGame()
    }

    private fun initGame() {
        setBackgroundColor(resources.getColor(R.color.gameViewBackgroundColor))
        columnCount = CARD_COUNT

        val cardWidth: Int = getCardWidth()
        addCards(cardWidth, cardWidth)

        //随机生成两个数字方块
        randomCreateCard(2)
        //监听滑动事件
        setListener()
    }

    fun replayGame() {
        RxBus.get().post(ClearScore())
        for (i in 0 until CARD_COUNT) {
            for (j in 0 until CARD_COUNT) {
                cards[i][j]?.setNumber(0)
            }
        }
        randomCreateCard(2)
    }

    /**
     * 监听Touch事件
     */
    private fun setListener() {
        setOnTouchListener(object : OnTouchListener {
            private var staX = 0f
            private var staY = 0f
            private var endX = 0f
            private var endY = 0f
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        //记录起始位置
                        staX = event.x
                        staY = event.y
                    }

                    MotionEvent.ACTION_UP -> {
                        //记录终点位置
                        endX = event.x
                        endY = event.y
                        var swiped = false //记录是否有效滑动了

                        //水平移动更多
                        if (abs(endX - staX) > abs(endY - staY)) {
                            if (endX - staX > 10) {
                                if (swipeRight()) {
                                    swiped = true
                                }
                            } else if (endX - staX < -10) {
                                if (swipeLeft()) {
                                    swiped = true
                                }
                            }
                        } else {
                            if (endY - staY < -10) {
                                if (swipeUp()) {
                                    swiped = true
                                }
                            } else if (endY - staY > 10) {
                                if (swipeDown()) {
                                    swiped = true
                                }
                            }
                        }
                        //滑动后创建新块，并检查当前状态是否能滑动
                        if (swiped) {
                            randomCreateCard(1)
                            if (!canSwipe()) {
                                gameOver()
                            }
                        }
                    }
                }
                return true
            }
        })
    }

    /**
     * 上划
     * flag 返回该次滑动是否有效（有卡片移动或合并）
     * addScore 该次滑动获取的积分
     * win 该次滑动是否达成胜利
     * j 列标 -- 第一层循环
     * i 行标 -- 第二层循环，cards[i][j]为等待移动的元素，如果cards[i][j]不为0，则进入第三层循环
     * ind -- 第三层循环的遍历终点，初始等于0，如果有数字合并了，才会改变终点位置，改为ii后的一个元素，因为有合并操作那ii和它前面的元素就不需要再比较了。
     * ii --第三层循环的遍历起点，初始为i的上一个元素，一直遍历到ind，通过cards[ii][j]与cards[i][j]的比较，来判断是否需要执行合并或移动操作。
     * 如果是合并操作，则可以直接结束第三层循环，并且ii和它前面的元素就不需要比较了；而如果是移动操作，则i跟随着向上移动1格，并重新开始比较，看下前面是否还可以继续移动或合并
     */
    private fun swipeUp(): Boolean {
        var flag = false
        var addScore = 0
        var win = false
        for (j in 0 until CARD_COUNT) {
            var ind = 0
            //从上往下依次处理
            var i = 1
            while (i < CARD_COUNT) {
                //如果是存在数字的，往上遍历
                if (cards[i][j]?.getNumber() != 0) {
                    for (ii in i - 1 downTo ind) {
                        //如果这块是空的，将数字上移
                        if (cards[ii][j]?.getNumber() == 0) {
                            cards[ii][j]?.setNumber(cards[i][j]?.getNumber()!!)
                            cards[i][j]?.setNumber(0)
                            i-- //上移
                            flag = true
                        } else if (cards[ii][j]?.getNumber() == cards[i][j]?.getNumber()) {
                            cards[ii][j]?.setNumber(cards[i][j]?.getNumber()!! * 2)
                            cards[i][j]?.setNumber(0)
                            flag = true
                            ind = ii + 1 //已经合过，该点不再合成

                            if (cards[ii][j]?.getNumber()!! >= 2048) {
                                win = true
                            }
                            addScore += cards[ii][j]?.getNumber()!!
                            //播放合并动画
                            playMergeAnimation(ii, j)
                            break
                        } else break
                    }
                }
                ++i
            }
        }

        if (addScore != 0) {
            RxBus.get().post(AddScore(addScore))
        }
        if (win) {
            RxBus.get().post(Win())
        }
        return flag
    }

    /**
     * 下划
     */
    private fun swipeDown(): Boolean {
        var flag = false
        var addScore = 0
        var win = false
        for (j in 0 until CARD_COUNT) {
            var ind = CARD_COUNT
            var i = CARD_COUNT - 2
            while (i >= 0) {
                if (cards[i][j]?.getNumber() != 0) {
                    for (ii in i + 1 until ind) {
                        if (cards[ii][j]?.getNumber() == 0) {
                            cards[ii][j]?.setNumber(cards[i][j]?.getNumber()!!)
                            cards[i][j]?.setNumber(0)
                            flag = true
                            i++
                        } else if (cards[ii][j]?.getNumber() == cards[i][j]?.getNumber()) {
                            cards[ii][j]?.setNumber(cards[i][j]?.getNumber()!! * 2)
                            cards[i][j]?.setNumber(0)
                            flag = true
                            ind = ii

                            if (cards[ii][j]?.getNumber()!! >= 2048) {
                                win = true
                            }
                            addScore += cards[ii][j]?.getNumber()!!
                            playMergeAnimation(ii, j)
                            break
                        } else break
                    }
                }
                --i
            }
        }

        if (addScore != 0) {
            RxBus.get().post(AddScore(addScore))
        }
        if (win) {
            RxBus.get().post(Win())
        }
        return flag
    }

    /**
     * 左划
     */
    private fun swipeLeft(): Boolean {
        var flag = false
        var addScore = 0
        var win = false
        for (i in 0 until CARD_COUNT) {
            var ind = 0
            var j = 1
            while (j < CARD_COUNT) {
                if (cards[i][j]?.getNumber() != 0) {
                    for (jj in j - 1 downTo ind) {
                        if (cards[i][jj]?.getNumber() == 0) {
                            cards[i][jj]?.setNumber(cards[i][j]?.getNumber()!!)
                            cards[i][j]?.setNumber(0)
                            flag = true
                            j--
                        } else if (cards[i][jj]?.getNumber() == cards[i][j]?.getNumber()) {
                            cards[i][jj]?.setNumber(cards[i][j]?.getNumber()!! * 2)
                            cards[i][j]?.setNumber(0)
                            flag = true
                            ind = jj + 1

                            if (cards[i][jj]?.getNumber()!! >= 2048) {
                                win = true
                            }
                            addScore += cards[i][jj]?.getNumber()!!
                            playMergeAnimation(i, jj)
                            break
                        } else break
                    }
                }
                ++j
            }
        }

        if (addScore != 0) {
            RxBus.get().post(AddScore(addScore))
        }
        if (win) {
            RxBus.get().post(Win())
        }
        return flag
    }

    /**
     * 右滑
     */
    private fun swipeRight(): Boolean {
        var flag = false
        var addScore = 0
        var win = false
        for (i in 0 until CARD_COUNT) {
            var ind = CARD_COUNT
            var j = CARD_COUNT - 2
            while (j >= 0) {
                if (cards[i][j]?.getNumber() != 0) {
                    for (jj in j + 1 until ind) {
                        if (cards[i][jj]?.getNumber() == 0) {
                            cards[i][jj]?.setNumber(cards[i][j]?.getNumber()!!)
                            cards[i][j]?.setNumber(0)
                            flag = true
                            j++
                        } else if (cards[i][jj]?.getNumber() == cards[i][j]?.getNumber()) {
                            cards[i][jj]?.setNumber(cards[i][j]?.getNumber()!! * 2)
                            cards[i][j]?.setNumber(0)
                            flag = true
                            ind = jj

                            if (cards[i][jj]?.getNumber()!! >= 2048) {
                                win = true
                            }
                            addScore += cards[i][jj]?.getNumber()!!
                            playMergeAnimation(i, jj)
                            break
                        } else break
                    }
                }
                --j
            }
        }
        if (addScore != 0) {
            RxBus.get().post(AddScore(addScore))
        }
        if (win) {
            RxBus.get().post(Win())
        }
        return flag
    }

    /**
     * 判断是否可以继续滑动
     * 如果存在空白块，或者相邻的数字相同的块，则可以继续滑动
     */
    private fun canSwipe(): Boolean {
        for (i in 0 until CARD_COUNT) {
            for (j in 0 until CARD_COUNT) {
                if (cards[i][j]?.getNumber() == 0) {
                    return true
                } else if (i != CARD_COUNT - 1 && cards[i][j]?.getNumber() == cards[i + 1][j]?.getNumber()) {
                    return true
                } else if (j != CARD_COUNT - 1 && cards[i][j]?.getNumber() === cards[i][j + 1]?.getNumber()) {
                    return true
                }
            }
        }
        return false
    }

    private fun addCards(width: Int, height: Int) {
        var c: Card
        for (i in 0 until CARD_COUNT) {
            for (j in 0 until CARD_COUNT) {
                c = Card(context)
                addView(c, width, height)
                cards[i][j] = c
            }
        }
    }

    private fun gameOver() {
        RxBus.get().post(GameOver())
    }

    private fun getCardWidth(): Int {
        //获取屏幕信息
        val displayMetrics = resources.displayMetrics
        //根据布局，GameView是占屏幕宽度的90%，除以4就是卡片边长
        return (displayMetrics.widthPixels * 0.9f / CARD_COUNT).toInt()
    }

    /**
     * 递归随机，玄学复杂度，期望递归次数小于 16 次，偷了个懒
     * 最好是把可用方块加入到一个列表中，然后在列表中随机
     * cnt是随机次数
     * 注:该方法已弃用
     */
    private fun randomRecursion(cnt: Int) {
        val random = Random()
        val r: Int = random.nextInt(CARD_COUNT)
        val c: Int = random.nextInt(CARD_COUNT)

        //该处已经存在数字，重新随机r, c
        if (cards[r][c]?.getNumber() != 0) {
            randomCreateCard(cnt)
            return
        }
        var rand: Int = random.nextInt(10)
        rand = if (rand >= 2) CARD_NUMBER else CARD_NUMBER * 2
        cards[r][c]?.setNumber(rand)

        //播放创建动画
        playCreateAnimation(r, c)
        if (cnt >= 2) {
            randomCreateCard(cnt - 1)
        }
    }

    /**
     * 从剩余的空卡片列表中随机一个位置创建卡片
     * cnt是随机次数
     */
    private fun randomCreateCard(cnt: Int) {
        val random = Random()
        //在空卡片列表中随机选择一个位置
        val emptyCards = getEmptyCards()
        val point = emptyCards[random.nextInt(emptyCards.size)]
        val row = point.row
        val colum = point.column

        //随机生成卡片上的数字
        var rand: Int = random.nextInt(10)
        rand = if (rand >= 2) CARD_NUMBER else CARD_NUMBER * 2
        cards[row][colum]?.setNumber(rand)

        //播放创建动画
        playCreateAnimation(row, colum)
        if (cnt >= 2) {
            randomCreateCard(cnt - 1)
        }
    }

    /**
     * 获取当前空卡片的列表
     */
    private fun getEmptyCards(): List<Point> {
        val emptyCards = arrayListOf<Point>()
        for (i in 0 until CARD_COUNT) {
            for (j in 0 until CARD_COUNT) {
                if (cards[i][j]?.getNumber() == 0) {
                    emptyCards.add(Point(i, j))
                }
            }
        }
        return emptyCards
    }

    /**
     * 播放创建新方块动画
     */
    private fun playCreateAnimation(r: Int, c: Int) {
        val animationSet = AnimationSet(true)

        //旋转
        val anim = RotateAnimation(
            0f,
            360f,
            RotateAnimation.RELATIVE_TO_SELF,
            0.5f,
            RotateAnimation.RELATIVE_TO_SELF,
            0.5f
        )
        anim.duration = 250
        anim.repeatCount = 0
        anim.interpolator = LinearInterpolator()

        //缩放
        val anim2 = ScaleAnimation(
            0f, 1f, 0f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        anim2.duration = 250
        anim2.repeatCount = 0
        animationSet.addAnimation(anim)
        animationSet.addAnimation(anim2)
        cards[r][c]?.startAnimation(animationSet)
    }

    /**
     * 播放合并动画
     */
    private fun playMergeAnimation(r: Int, c: Int) {
        val anim = ScaleAnimation(
            1f, 1.2f, 1f, 1.2f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        anim.duration = 150
        anim.repeatCount = 0
        anim.repeatMode = Animation.REVERSE
        cards[r][c]?.startAnimation(anim)
    }
}