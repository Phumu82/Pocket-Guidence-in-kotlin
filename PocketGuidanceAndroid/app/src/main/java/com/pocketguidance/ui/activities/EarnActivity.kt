package com.pocketguidance.ui.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pocketguidance.data.db.entities.TransactionEntity
import com.pocketguidance.databinding.ActivityEarnBinding
import com.pocketguidance.ui.adapters.CourseAdapter
import com.pocketguidance.ui.adapters.QuizAnswerAdapter
import com.pocketguidance.utils.FormatUtils
import kotlinx.coroutines.launch

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)

data class Course(
    val id: String,
    val title: String,
    val icon: String,
    val questions: List<QuizQuestion>,
    val rewardPerCorrect: Double = 10.0
)

class EarnActivity : BaseActivity() {

    private lateinit var binding: ActivityEarnBinding
    private val TAG = "EarnActivity"

    private var currentCourse: Course? = null
    private var currentQuestionIndex = 0
    private var score = 0
    private var selectedAnswer: Int? = null
    private lateinit var answerAdapter: QuizAnswerAdapter
    private var currency = "R"

    // ── All courses / quiz data (translated from quizData.ts) ────────────────
    private val allCourses = listOf(
        Course(
            id = "budgeting",
            title = "Budgeting Basics",
            icon = "💰",
            questions = listOf(
                QuizQuestion("What is a budget?", listOf("A list of bank accounts", "A plan for managing income and expenses", "A type of loan", "A credit card limit"), 1),
                QuizQuestion("Which of the following is a fixed expense?", listOf("Groceries", "Entertainment", "Rent", "Clothing"), 2),
                QuizQuestion("What is the main purpose of budgeting?", listOf("To increase spending", "To manage money effectively", "To avoid banks", "To reduce income"), 1),
                QuizQuestion("Which budgeting rule divides money into needs, wants, and savings?", listOf("70/20/10 rule", "50/30/20 rule", "80/10/10 rule", "60/20/20 rule"), 1),
                QuizQuestion("What is an emergency fund?", listOf("Money used for shopping", "Money saved for unexpected expenses", "Money invested in stocks", "Money borrowed from a bank"), 1),
                QuizQuestion("Which of the following is a saving habit?", listOf("Spending all income", "Saving before spending", "Borrowing frequently", "Ignoring expenses"), 1),
                QuizQuestion("Why should people track their expenses?", listOf("To reduce income", "To understand where money goes", "To increase debt", "To avoid banks"), 1),
                QuizQuestion("What type of goal is saving for a vacation?", listOf("Long-term goal", "Short-term goal", "Emergency goal", "Retirement goal"), 1),
                QuizQuestion("Which of the following helps increase savings?", listOf("Impulse buying", "Planning purchases", "Ignoring expenses", "Taking loans"), 1),
                QuizQuestion("What is a variable expense?", listOf("Rent", "Salary", "Groceries", "Insurance"), 2)
            )
        ),
        Course(
            id = "banking",
            title = "Banking Essentials",
            icon = "🏦",
            questions = listOf(
                QuizQuestion("What is a bank?", listOf("A store", "A financial institution that manages money", "A government office", "A school"), 1),
                QuizQuestion("Which account is mainly used for daily transactions?", listOf("Savings account", "Current/checking account", "Investment account", "Retirement account"), 1),
                QuizQuestion("What is interest?", listOf("Money earned on savings", "Money spent on food", "Money paid as tax", "Money donated"), 0),
                QuizQuestion("Compound interest means:", listOf("Interest on original money only", "Interest on money and previous interest", "No interest", "Fixed payment"), 1),
                QuizQuestion("What does ATM stand for?", listOf("Automatic Transfer Machine", "Automated Teller Machine", "Account Transfer Manager", "Automatic Transaction Money"), 1),
                QuizQuestion("Online banking allows customers to:", listOf("Open stores", "Manage accounts online", "Print money", "Avoid banks"), 1),
                QuizQuestion("Which card uses money directly from your bank account?", listOf("Credit card", "Debit card", "Gift card", "Loyalty card"), 1),
                QuizQuestion("What is a bank statement?", listOf("A list of bank employees", "A record of transactions", "A loan agreement", "A savings plan"), 1),
                QuizQuestion("What is overdraft?", listOf("Extra savings", "Spending more than available balance", "Interest on savings", "A loan payment"), 1),
                QuizQuestion("Which account usually earns interest?", listOf("Savings account", "Current account", "Credit card", "Loan account"), 0)
            )
        ),
        Course(
            id = "credit",
            title = "Credit & Loans",
            icon = "💳",
            questions = listOf(
                QuizQuestion("A credit card allows you to:", listOf("Spend borrowed money", "Spend only saved money", "Avoid banks", "Avoid payments"), 0),
                QuizQuestion("What is a loan?", listOf("Money borrowed that must be repaid", "Free money", "Investment money", "Tax money"), 0),
                QuizQuestion("What is a credit limit?", listOf("Maximum amount you can borrow on a card", "Minimum savings", "Bank fee", "Tax rate"), 0),
                QuizQuestion("A credit score measures:", listOf("Spending habits", "Creditworthiness", "Bank balance", "Income level"), 1),
                QuizQuestion("Paying credit card bills on time helps:", listOf("Reduce credit score", "Improve credit score", "Avoid banks", "Increase taxes"), 1),
                QuizQuestion("Missing loan payments can:", listOf("Improve credit", "Damage credit score", "Increase savings", "Reduce debt"), 1),
                QuizQuestion("Minimum payment means:", listOf("Full balance", "Smallest required payment", "Total loan", "Savings payment"), 1),
                QuizQuestion("Which loan type requires collateral?", listOf("Secured loan", "Unsecured loan", "Credit card", "Payday loan"), 0),
                QuizQuestion("Payday loans usually have:", listOf("Low interest", "High interest", "No interest", "Free money"), 1),
                QuizQuestion("Debt consolidation means:", listOf("Ignoring debt", "Combining multiple debts", "Borrowing more money", "Paying taxes"), 1)
            )
        ),
        Course(
            id = "investing",
            title = "Investing 101",
            icon = "📈",
            questions = listOf(
                QuizQuestion("What is investing?", listOf("Spending money on wants", "Putting money to work for future growth", "Borrowing money", "Avoiding banks"), 1),
                QuizQuestion("What is a stock?", listOf("A bond payment", "A share of ownership in a company", "A type of loan", "A savings account"), 1),
                QuizQuestion("What is diversification?", listOf("Putting all money in one stock", "Spreading investments to reduce risk", "Borrowing to invest", "Avoiding all investments"), 1),
                QuizQuestion("What does ROI stand for?", listOf("Rate of Income", "Return on Investment", "Risk of Inflation", "Revenue of Interest"), 1),
                QuizQuestion("Which investment is generally considered lowest risk?", listOf("Cryptocurrency", "Individual stocks", "Government bonds", "Startup equity"), 2),
                QuizQuestion("What is a dividend?", listOf("A type of loan", "A share of company profits paid to shareholders", "A stock price", "An investment fee"), 1),
                QuizQuestion("What is compound growth?", listOf("Growth on original investment only", "Growth on investment and previous returns", "Fixed annual return", "Tax-free growth"), 1),
                QuizQuestion("Which is a characteristic of cryptocurrency?", listOf("Government-backed", "High volatility", "Fixed returns", "No risk"), 1),
                QuizQuestion("An ETF is:", listOf("A type of bank account", "A basket of investments traded like a stock", "A government bond", "A savings plan"), 1),
                QuizQuestion("Long-term investing generally:", listOf("Reduces all risk", "Smooths out short-term market fluctuations", "Guarantees profits", "Avoids market exposure"), 1)
            )
        )
    )

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEarnBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requireLogin()

        supportActionBar?.apply {
            title = "Earn & Learn"
            setDisplayHomeAsUpEnabled(true)
        }

        lifecycleScope.launch {
            currency = financeRepo.getUserPrefsOnce(userId)?.currency ?: "R"
        }

        setupAnswerRecyclerView()
        showCourseList()
        Log.d(TAG, "EarnActivity created for userId=$userId")
    }

    override fun onSupportNavigateUp(): Boolean {
        if (currentCourse != null) {
            showCourseList()   // back = exit quiz → course list
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
        return true
    }

    // ── Course list ───────────────────────────────────────────────────────────

    private fun showCourseList() {
        currentCourse = null
        currentQuestionIndex = 0
        score = 0
        selectedAnswer = null

        binding.layoutCourseList.visibility = View.VISIBLE
        binding.layoutQuiz.visibility       = View.GONE

        val courseAdapter = CourseAdapter(allCourses) { course ->
            startQuiz(course)
        }
        binding.rvCourses.layoutManager = LinearLayoutManager(this)
        binding.rvCourses.adapter = courseAdapter

        Log.d(TAG, "Course list shown")
    }

    // ── Quiz flow ─────────────────────────────────────────────────────────────

    private fun startQuiz(course: Course) {
        currentCourse = course
        currentQuestionIndex = 0
        score = 0
        selectedAnswer = null

        binding.layoutCourseList.visibility = View.GONE
        binding.layoutQuiz.visibility       = View.VISIBLE
        binding.tvCourseTitle.text          = "${course.icon}  ${course.title}"

        showQuestion()
        Log.d(TAG, "Quiz started: ${course.title}")
    }

    private fun showQuestion() {
        val course   = currentCourse ?: return
        val question = course.questions[currentQuestionIndex]
        val total    = course.questions.size
        val progress = ((currentQuestionIndex + 1).toFloat() / total * 100).toInt()

        binding.tvQuestionNumber.text = "Question ${currentQuestionIndex + 1} of $total"
        binding.tvQuestion.text       = question.question
        binding.progressQuiz.progress = progress
        selectedAnswer                = null

        binding.btnNext.visibility    = View.GONE
        binding.btnNext.text          = if (currentQuestionIndex + 1 >= total) "Finish Quiz" else "Next Question"

        answerAdapter.setAnswers(question.options, selectedAnswer = null, correctIndex = null)
    }

    private fun setupAnswerRecyclerView() {
        answerAdapter = QuizAnswerAdapter { selectedIndex ->
            if (selectedAnswer != null) return@QuizAnswerAdapter  // already answered

            val question = currentCourse?.questions?.get(currentQuestionIndex) ?: return@QuizAnswerAdapter
            selectedAnswer = selectedIndex
            val isCorrect = selectedIndex == question.correctIndex

            if (isCorrect) {
                score++
                Log.d(TAG, "Correct! score=$score")
            }

            // Reveal correct answer highlight
            answerAdapter.setAnswers(question.options, selectedAnswer = selectedIndex, correctIndex = question.correctIndex)

            binding.btnNext.visibility = View.VISIBLE
        }

        binding.rvAnswers.layoutManager = LinearLayoutManager(this)
        binding.rvAnswers.adapter = answerAdapter

        binding.btnNext.setOnClickListener { advanceQuiz() }
        binding.btnExitQuiz.setOnClickListener { showCourseList() }
    }

    private fun advanceQuiz() {
        val course = currentCourse ?: return

        if (currentQuestionIndex + 1 >= course.questions.size) {
            finishQuiz(course)
        } else {
            currentQuestionIndex++
            showQuestion()
        }
    }

    private fun finishQuiz(course: Course) {
        val earned = score * course.rewardPerCorrect
        Log.i(TAG, "Quiz finished: ${course.title}, score=$score/${course.questions.size}, earned=$earned")

        if (earned > 0) {
            lifecycleScope.launch {
                financeRepo.addTransaction(
                    TransactionEntity(
                        userId      = userId,
                        type        = "income",
                        amount      = earned,
                        category    = "Earnings",
                        description = "Quiz reward: ${course.title}",
                        date        = FormatUtils.todayIso()
                    )
                )
                Log.i(TAG, "Earnings recorded: ${FormatUtils.formatCurrency(earned, currency)}")
            }
        }

        val pct = (score.toFloat() / course.questions.size * 100).toInt()
        val msg = buildString {
            append("🎉 Quiz Complete!\n\n")
            append("Score: $score / ${course.questions.size}  ($pct%)\n")
            if (earned > 0) {
                append("Earned: ${FormatUtils.formatCurrency(earned, currency)} 💰")
            } else {
                append("No reward this time — keep practising!")
            }
        }

        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        showCourseList()
    }
}
