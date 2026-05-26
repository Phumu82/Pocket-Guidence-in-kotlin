package com.pocketguidance.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pocketguidance.databinding.ItemCourseBinding
import com.pocketguidance.ui.activities.Course

class CourseAdapter(
    private val courses: List<Course>,
    private val onClick: (Course) -> Unit
) : RecyclerView.Adapter<CourseAdapter.ViewHolder>() {

    inner class ViewHolder(private val b: ItemCourseBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(course: Course) {
            b.tvCourseIcon.text       = course.icon
            b.tvCourseTitle.text      = course.title
            b.tvCourseDetails.text    = "${course.questions.size} questions  •  R${(course.questions.size * course.rewardPerCorrect).toInt()} max reward"
            b.root.setOnClickListener { onClick(course) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(courses[position])
    override fun getItemCount() = courses.size
}
