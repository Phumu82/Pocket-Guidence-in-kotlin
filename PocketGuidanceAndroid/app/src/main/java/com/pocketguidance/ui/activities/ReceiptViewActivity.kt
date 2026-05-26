package com.pocketguidance.ui.activities

import android.os.Bundle
import com.bumptech.glide.Glide
import com.pocketguidance.databinding.ActivityReceiptViewBinding
import java.io.File

class ReceiptViewActivity : BaseActivity() {

    companion object {
        const val EXTRA_PHOTO_PATH = "extra_photo_path"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityReceiptViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            title = "Receipt"
            setDisplayHomeAsUpEnabled(true)
        }

        val path = intent.getStringExtra(EXTRA_PHOTO_PATH)
        if (path != null) {
            Glide.with(this).load(File(path)).into(binding.ivReceipt)
        }
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
