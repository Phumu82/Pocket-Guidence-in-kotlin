package com.pocketguidance.ui.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pocketguidance.data.db.entities.CategoryEntity
import com.pocketguidance.databinding.ActivityCategoryBinding
import com.pocketguidance.ui.adapters.CategoryAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CategoryActivity : BaseActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private lateinit var adapter: CategoryAdapter
    private val TAG = "CategoryActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requireLogin()

        supportActionBar?.apply {
            title = "Categories"
            setDisplayHomeAsUpEnabled(true)
        }

        setupRecyclerView()
        setupAddButton()
        observeCategories()

        Log.d(TAG, "CategoryActivity created for userId=$userId")
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter(
            onDelete = { category -> deleteCategory(category) }
        )
        binding.rvCategories.layoutManager = LinearLayoutManager(this)
        binding.rvCategories.adapter = adapter
    }

    private fun setupAddButton() {
        binding.btnAddCategory.setOnClickListener {
            val name = binding.etCategoryName.text.toString().trim()

            if (name.length < 2) {
                binding.tilCategoryName.error = "Category name must be at least 2 characters"
                return@setOnClickListener
            }
            binding.tilCategoryName.error = null

            lifecycleScope.launch {
                val added = financeRepo.addCustomCategory(userId, name)
                if (added) {
                    binding.etCategoryName.text?.clear()
                    Toast.makeText(this@CategoryActivity, "Category \"$name\" added", Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "Added category: $name")
                } else {
                    binding.tilCategoryName.error = "Category already exists"
                }
            }
        }
    }

    private fun observeCategories() {
        lifecycleScope.launch {
            financeRepo.getCategories(userId).collectLatest { categories ->
                adapter.submitList(categories)
                Log.d(TAG, "Categories updated: ${categories.size} total")
            }
        }
    }

    private fun deleteCategory(category: CategoryEntity) {
        if (!category.isCustom) {
            Toast.makeText(this, "Default categories cannot be deleted", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            financeRepo.deleteCustomCategory(userId, category.name)
            Toast.makeText(this@CategoryActivity, "\"${category.name}\" deleted", Toast.LENGTH_SHORT).show()
            Log.i(TAG, "Deleted category: ${category.name}")
        }
    }
}
