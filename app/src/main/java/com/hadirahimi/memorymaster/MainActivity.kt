package com.hadirahimi.memorymaster

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.forEach
import androidx.lifecycle.lifecycleScope
import com.hadirahimi.memorymaster.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var score = 0
    private lateinit var binding: ActivityMainBinding
    private var result: String = ""
    private var userAnswer: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //initViews
        binding.apply {
            panel1.setOnClickListener(this@MainActivity)
            panel2.setOnClickListener(this@MainActivity)
            panel3.setOnClickListener(this@MainActivity)
            panel4.setOnClickListener(this@MainActivity)
            startGame()
            tvScoreboard.setOnClickListener {
                // Navigate to ScoreboardActivity when scoreboard text is clicked
                startActivity(Intent(this@MainActivity, ScoreboardActivity::class.java))
            }
        }
    }

    private fun startGame() {
        result = ""
        userAnswer = ""
        disableButtons()
        lifecycleScope.launch {
            val round = (3..5).random()
            repeat(round) {
                delay(400)
                val randomPanel = (1..4).random()
                result += randomPanel

                val panel = when (randomPanel) {
                    1 -> binding.panel1
                    2 -> binding.panel2
                    3 -> binding.panel3
                    else -> binding.panel4
                }

                val drawableYellow = ActivityCompat.getDrawable(this@MainActivity, R.drawable.btn_yellow)
                val drawableDefault = ActivityCompat.getDrawable(this@MainActivity, R.drawable.btn_state)
                panel.background = drawableYellow
                delay(1000)
                panel.background = drawableDefault
            }
            runOnUiThread {
                enableButtons()
            }
        }
    }

    private fun loseAnimation() {
        binding.apply {
            // Store the score
            storeScore(score)
            score = 0
            tvScore.text = "0"
            disableButtons()
            val drawableLose = ActivityCompat.getDrawable(this@MainActivity, R.drawable.btn_lose)
            val drawableDefault = ActivityCompat.getDrawable(this@MainActivity, R.drawable.btn_state)
            lifecycleScope.launch {
                binding.root.forEach { view ->
                    if (view is Button) {
                        view.background = drawableLose
                        delay(200)
                        view.background = drawableDefault
                    }
                }
                delay(1000)
                startGame()
            }
        }
    }

    private fun enableButtons() {
        binding.root.forEach { view ->
            if (view is Button) {
                view.isEnabled = true
            }
        }
    }

    private fun disableButtons() {
        binding.root.forEach { view ->
            if (view is Button) {
                view.isEnabled = false
            }
        }
    }

    override fun onClick(view: View?) {
        view?.let {
            userAnswer += when (it.id) {
                R.id.panel1 -> "1"
                R.id.panel2 -> "2"
                R.id.panel3 -> "3"
                R.id.panel4 -> "4"
                else -> ""
            }

            if (userAnswer == result) {
                Toast.makeText(this@MainActivity, "W I N :)", Toast.LENGTH_SHORT).show()
                score++
                binding.tvScore.text = score.toString()
                startGame()
            } else if (userAnswer.length >= result.length) {
                loseAnimation()
            }
        }
    }

    private fun storeScore(score: Int) {
        val sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        val topScores = mutableListOf<Int>()

        // Load existing scores
        repeat(5) { index ->
            val highScore = sharedPrefs.getInt("score$index", 0)
            topScores.add(highScore)
        }

        // Add current score
        topScores.add(score)

        // Sort scores in descending order
        topScores.sortDescending()

        // Keep only the top 5 scores
        val updatedTopScores = topScores.take(5)

        // Save the updated top scores
        updatedTopScores.forEachIndexed { index, topScore ->
            editor.putInt("score$index", topScore)
        }

        editor.apply()
    }
}
