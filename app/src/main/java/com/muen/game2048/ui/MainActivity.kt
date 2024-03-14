package com.muen.game2048.ui

import android.view.View
import com.muen.game2048.databinding.ActivityMainBinding
import com.muen.game2048.rxbus.event.AddScore
import com.muen.game2048.rxbus.event.ClearScore
import com.muen.game2048.rxbus.event.GameOver
import com.muen.game2048.rxbus.event.Win
import com.muen.game2048.rxbus.rxBus
import com.muen.game2048.util.BaseActivity
import com.muen.game2048.util.MMKVManage
import com.muen.game2048.util.WindowUtils


class MainActivity : BaseActivity<ActivityMainBinding>() {
    private var currentScore = 0
    private var highestScore = 0

    override fun onCreateViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        WindowUtils.setLightStatusBar(window)
        //读取最高分
        highestScore = MMKVManage.highestScore
        viewBinding.textHighestScore.text = "最佳分数 : $highestScore"
    }

    override fun initListener() {
        super.initListener()
        viewBinding.buttonReplay.setOnClickListener {
            restartGame()
        }

        viewBinding.btnRestart.setOnClickListener {
            /*val lp = window.attributes
            lp.alpha = 1.0f
            window.attributes = lp*/
            restartGame()
        }
    }

    override fun observerData() {
        super.observerData()
        rxBus<AddScore> {
            addScore(it.score)
        }

        rxBus<ClearScore> {
            clearScore()
        }

        rxBus<GameOver> {
            gameOver()
        }

        rxBus<Win> {
            gameWin()
        }
    }

    private fun addScore(score: Int) {
        currentScore += score
        viewBinding.textScore.text = "分数 : $currentScore"
        //更新最高分
        updateHighestScore(currentScore)
    }

    private fun updateHighestScore(score: Int) {
        if (score > highestScore) {
            highestScore = score
            viewBinding.textHighestScore.text = "最佳分数 : $highestScore"
            //存储最高分
            MMKVManage.highestScore = highestScore
        }
    }

    private fun clearScore() {
        currentScore = 0
        viewBinding.textScore.text = "分数 : " + 0
    }

    private fun restartGame() {
        viewBinding.gameView.alpha = 1.0f
        viewBinding.gameOver.visibility = View.GONE
        viewBinding.gameView.replayGame()
        viewBinding.gameView.isEnabled = true
    }

    private fun gameOver() {
        /*val lp = window.attributes
        lp.alpha = 0.7f
        window.attributes = lp*/
        viewBinding.txtGameOver.text = "Game Over!"
        viewBinding.btnRestart.text = "重试"
        viewBinding.gameView.alpha = 0.7f
        viewBinding.gameView.isEnabled = false
        viewBinding.gameOver.visibility = View.VISIBLE

    }

    private fun gameWin() {
        viewBinding.txtGameOver.text = "You Win!"
        viewBinding.btnRestart.text = "再玩一次"
        viewBinding.gameView.alpha = 0.7f
        viewBinding.gameView.isEnabled = false
        viewBinding.gameOver.visibility = View.VISIBLE
    }
}