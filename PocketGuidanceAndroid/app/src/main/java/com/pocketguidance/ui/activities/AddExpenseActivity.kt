package com.pocketguidance.ui.activities

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.pocketguidance.data.db.entities.TransactionEntity
import com.pocketguidance.databinding.ActivityAddExpenseBinding
import com.pocketguidance.utils.FormatUtils
import com.pocketguidance.utils.PhotoUtils
import com.pocketguidance.utils.ValidationUtils
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

class AddExpenseActivity : BaseActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private val TAG = "AddExpenseActivity"

    private var selectedDate: String = FormatUtils.todayIso()
    private var receiptPhotoPath: String? = null
    private var photoFile: File? = null

    // Camera launcher
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoFile != null) {
            receiptPhotoPath = photoFile!!.absolutePath
            showReceiptPreview(receiptPhotoPath!!)
            Log.d(TAG, "Photo captured: $receiptPhotoPath")
        }
    }

    // Gallery launcher
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            // Copy to app-private storage
            val destFile = PhotoUtils.createImageFile(this)
            contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            }
            receiptPhotoPath = destFile.absolutePath
            showReceiptPreview(receiptPhotoPath!!)
            Log.d(TAG, "Photo from gallery: $receiptPhotoPath")
        }
    }

    // Permission launcher
    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCamera() else Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requireLogin()

        supportActionBar?.apply {
            title = "Add Transaction"
            setDisplayHomeAsUpEnabled(true)
        }

        setupTypeToggle()
        setupDatePicker()
        setupPhotoButtons()
        loadCategories()
        setupSaveButton()

        Log.d(TAG, "AddExpenseActivity created")
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }

    private fun setupTypeToggle() {
        // Default: Expense selected
        binding.btnExpense.isSelected = true
        binding.btnExpense.setOnClickListener {
            binding.btnExpense.isSelected = true
            binding.btnIncome.isSelected = false
            loadCategories()
        }
        binding.btnIncome.setOnClickListener {
            binding.btnIncome.isSelected = true
            binding.btnExpense.isSelected = false
            loadIncomeCategories()
        }
    }

    private fun setupDatePicker() {
        binding.tvDate.text = selectedDate
        binding.tvDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                selectedDate = "%04d-%02d-%02d".format(y, m + 1, d)
                binding.tvDate.text = selectedDate
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setupPhotoButtons() {
        binding.btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
        binding.btnGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
        binding.btnRemovePhoto.setOnClickListener {
            receiptPhotoPath = null
            binding.ivReceiptPreview.visibility = View.GONE
            binding.btnRemovePhoto.visibility = View.GONE
            Log.d(TAG, "Receipt photo removed")
        }
    }

    private fun launchCamera() {
        photoFile = PhotoUtils.createImageFile(this)
        val uri = PhotoUtils.getUriForFile(this, photoFile!!)
        cameraLauncher.launch(uri)
    }

    private fun showReceiptPreview(path: String) {
        binding.ivReceiptPreview.visibility = View.VISIBLE
        binding.btnRemovePhoto.visibility = View.VISIBLE
        Glide.with(this).load(File(path)).centerCrop().into(binding.ivReceiptPreview)
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val categories = financeRepo.getCategoriesOnce(userId).map { it.name }
            val adapter = ArrayAdapter(this@AddExpenseActivity, android.R.layout.simple_spinner_dropdown_item, categories)
            binding.spinnerCategory.adapter = adapter
        }
    }

    private fun loadIncomeCategories() {
        val incomeCategories = listOf("Salary", "Freelance", "Investment", "Earnings", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, incomeCategories)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener { saveTransaction() }
    }

    private fun saveTransaction() {
        val amountStr   = binding.etAmount.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val category    = binding.spinnerCategory.selectedItem?.toString() ?: ""

        var valid = true

        val amount = ValidationUtils.parseAmount(amountStr)
        if (amount == null) {
            binding.tilAmount.error = "Enter a valid amount greater than 0"
            valid = false
        } else binding.tilAmount.error = null

        if (category.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            valid = false
        }

        if (!valid) return

        val isIncome = binding.btnIncome.isSelected
        val type = if (isIncome) "income" else "expense"

        lifecycleScope.launch {
            val tx = TransactionEntity(
                userId = userId,
                type = type,
                amount = amount!!,
                category = category,
                description = description,
                date = selectedDate,
                receiptPhotoPath = receiptPhotoPath
            )
            val id = financeRepo.addTransaction(tx)
            Log.i(TAG, "Transaction saved: id=$id, type=$type, amount=$amount, cat=$category")

            Toast.makeText(
                this@AddExpenseActivity,
                "${if (isIncome) "Income" else "Expense"} of R${amount} added!",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }
}
