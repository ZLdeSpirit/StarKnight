package com.s.k.starknight.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.s.k.starknight.databinding.SkActivitySettingsBinding

class SkSettingsActivity : AppCompatActivity() {
    private val TAG = "SkSettingsActivity"
    private lateinit var mBinding: SkActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = SkActivitySettingsBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
    }
}