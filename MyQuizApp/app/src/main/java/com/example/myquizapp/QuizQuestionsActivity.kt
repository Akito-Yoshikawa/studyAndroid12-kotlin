package com.example.myquizapp

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat

class QuizQuestionsActivity : AppCompatActivity(), View.OnClickListener {

    // 何番目のクイズか
    private var mCurrentPosition: Int = 1
    // クイズ情報
    private var mQuestionsList: ArrayList<Question>? = null
    // 何番目の答えを選択したか
    private var mSelectedOptionPosition: Int = 0

    private var mUserName: String? = null
    private var mCorrectAnswers: Int = 0

    private var progressBar: ProgressBar? = null
    private var tvProgress: TextView? = null
    private var tvQuestion: TextView? = null
    private var ivImage: ImageView? = null

    private var tvQuestionOne: TextView? = null
    private var tvQuestionTwo: TextView? = null
    private var tvQuestionThree: TextView? = null
    private var tvQuestionFour: TextView? = null

    private var btnSubmit: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_questions)

        mUserName = intent.getStringExtra(Constants.USER_NAME)

        // layoutから部品の取得
        progressBar = findViewById(R.id.tv_progressBar)
        tvProgress = findViewById(R.id.tv_progress)
        tvQuestion = findViewById(R.id.tv_question)
        ivImage = findViewById(R.id.iv_image)

        tvQuestionOne = findViewById(R.id.tv_option_one)
        tvQuestionTwo = findViewById(R.id.tv_option_two)
        tvQuestionThree = findViewById(R.id.tv_option_three)
        tvQuestionFour = findViewById(R.id.tv_option_four)
        btnSubmit = findViewById(R.id.btn_submit)

        tvQuestionOne?.setOnClickListener(this)
        tvQuestionTwo?.setOnClickListener(this)
        tvQuestionThree?.setOnClickListener(this)
        tvQuestionFour?.setOnClickListener(this)
        btnSubmit?.setOnClickListener(this)

        mQuestionsList = Constants.getQuestions()
        setQuestion()
    }

    // 問題の情報を取得、反映
    private fun setQuestion() {
        defaultOptionsView()

        val question: Question = mQuestionsList!![mCurrentPosition - 1]

        ivImage?.setImageResource(question.image)
        progressBar?.progress = mCurrentPosition
        tvProgress?.text = "$mCurrentPosition/${progressBar?.max}"
        tvQuestion?.text = question.question
        tvQuestionOne?.text = question.optionOne
        tvQuestionTwo?.text = question.optionTwo
        tvQuestionThree?.text = question.optionThree
        tvQuestionFour?.text = question.optionFour

        btnSubmit?.text = "SUBMIT"
    }

    // 初期状態に戻す
    private fun defaultOptionsView() {
        val options = ArrayList<TextView>()

        tvQuestionOne?.let {
            options.add(0, it)
        }
        tvQuestionTwo?.let {
            options.add(1, it)
        }
        tvQuestionThree?.let {
            options.add(2, it)
        }
        tvQuestionFour?.let {
            options.add(3, it)
        }

        for(option in options) {
            option.setTextColor(Color.parseColor("#7A8089"))
            option.typeface = Typeface.DEFAULT
            option.background = ContextCompat.getDrawable(
                this,
                R.drawable.default_option_border_bg
            )
        }
    }

    private fun selectedOptionView(tv: TextView, selectedOptionNum: Int) {
        defaultOptionsView()

        mSelectedOptionPosition = selectedOptionNum

        tv.setTextColor(Color.parseColor("#363A43"))
        tv.setTypeface(tv.typeface, Typeface.BOLD)
        tv.background = ContextCompat.getDrawable(
            this,
            R.drawable.selected_option_border_bg
        )
    }

    override fun onClick(view: View?) {
       when(view?.id) {
           R.id.tv_option_one -> {
               tvQuestionOne?.let {
                   selectedOptionView(it, 1)
               }
           }
           R.id.tv_option_two -> {
               tvQuestionTwo?.let {
                   selectedOptionView(it, 2)
               }
           }
           R.id.tv_option_three -> {
               tvQuestionThree?.let {
                   selectedOptionView(it, 3)
               }
           }
           R.id.tv_option_four -> {
               tvQuestionFour?.let {
                   selectedOptionView(it, 4)
               }
           }

           R.id.btn_submit -> {
                if (mSelectedOptionPosition == 0) {
                    mCurrentPosition++
                    when {
                        mCurrentPosition <= mQuestionsList!!.size -> {
                            setQuestion()
                        }
                        else -> {
                            // 遷移前の値セット
                            val intent = Intent(this, ResultActivity::class.java)
                            intent.putExtra(Constants.USER_NAME, mUserName)
                            intent.putExtra(Constants.CORRECT_ANSWERS, mCorrectAnswers)
                            intent.putExtra(Constants.TOTAL_QUESTIONS, mQuestionsList?.size)
                            startActivity(intent)

                            // 画面の破棄
                            finish()
                        }
                    }
                } else {
                    val question = mQuestionsList?.get(mCurrentPosition - 1)
                    if (question!!.correctAnswer != mSelectedOptionPosition) {
                        answerView(mSelectedOptionPosition, R.drawable.wrong_option_border_bg)
                    } else {
                        mCorrectAnswers++
                    }
                    answerView(question.correctAnswer, R.drawable.correct_option_border_bg)

                    if (mCurrentPosition == mQuestionsList!!.size) {
                        btnSubmit?.text = "FINISH"
                    } else {
                        btnSubmit?.text = "GO TO NEXT QUESTION"
                    }

                    mSelectedOptionPosition = 0
                }
           }
       }
    }


    private fun answerView(answer: Int, drawableView: Int) {
        when(answer) {
            1 -> {
                tvQuestionOne?.background = ContextCompat.getDrawable(
                    this,
                    drawableView
                )
            }
            2 -> {
                tvQuestionTwo?.background = ContextCompat.getDrawable(
                    this,
                    drawableView
                )
            }
            3 -> {
                tvQuestionThree?.background = ContextCompat.getDrawable(
                    this,
                    drawableView
                )
            }
            4 -> {
                tvQuestionFour?.background = ContextCompat.getDrawable(
                    this,
                    drawableView
                )
            }
        }
    }
}