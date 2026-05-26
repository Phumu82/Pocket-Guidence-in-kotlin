package com.pocketguidance.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pocketguidance.databinding.ItemQuizAnswerBinding

class QuizAnswerAdapter(
    private val onSelect: (Int) -> Unit
) : RecyclerView.Adapter<QuizAnswerAdapter.ViewHolder>() {

    private var options: List<String>   = emptyList()
    private var selectedAnswer: Int?    = null
    private var correctIndex: Int?      = null

    fun setAnswers(options: List<String>, selectedAnswer: Int?, correctIndex: Int?) {
        this.options        = options
        this.selectedAnswer = selectedAnswer
        this.correctIndex   = correctIndex
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val b: ItemQuizAnswerBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(index: Int, text: String) {
            b.tvAnswerText.text  = text
            b.tvAnswerLabel.text = ('A' + index).toString()

            // Determine state
            val revealed = selectedAnswer != null
            val isSelected = selectedAnswer == index
            val isCorrect  = correctIndex  == index

            val bgColor: Int
            val textColor: Int
            val labelBg: Int

            when {
                !revealed -> {
                    bgColor   = Color.parseColor("#F8FAFC")
                    textColor = Color.parseColor("#0F172A")
                    labelBg   = Color.parseColor("#EEF2FF")
                }
                isCorrect -> {
                    bgColor   = Color.parseColor("#DCFCE7")
                    textColor = Color.parseColor("#166534")
                    labelBg   = Color.parseColor("#22C55E")
                }
                isSelected -> {
                    bgColor   = Color.parseColor("#FEE2E2")
                    textColor = Color.parseColor("#991B1B")
                    labelBg   = Color.parseColor("#EF4444")
                }
                else -> {
                    bgColor   = Color.parseColor("#F1F5F9")
                    textColor = Color.parseColor("#94A3B8")
                    labelBg   = Color.parseColor("#E2E8F0")
                }
            }

            b.root.setBackgroundColor(bgColor)
            b.tvAnswerText.setTextColor(textColor)
            b.tvAnswerLabel.setBackgroundColor(labelBg)
            b.tvAnswerLabel.setTextColor(if (revealed && (isCorrect || isSelected)) Color.WHITE else Color.parseColor("#4F46E5"))

            // Result icon
            b.tvResultIcon.text = when {
                !revealed   -> ""
                isCorrect   -> "✅"
                isSelected  -> "❌"
                else        -> ""
            }

            b.root.setOnClickListener {
                if (selectedAnswer == null) onSelect(index)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemQuizAnswerBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(position, options[position])

    override fun getItemCount() = options.size
}
