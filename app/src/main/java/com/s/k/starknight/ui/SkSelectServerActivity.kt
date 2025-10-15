package com.s.k.starknight.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.s.k.starknight.databinding.SkActivitySelectServerBinding
import com.s.k.starknight.ui.adapter.SkSelectServerAdapter

class SkSelectServerActivity: AppCompatActivity() {
    private val TAG = "SkSelectServerActivity"
    private lateinit var mBinding: SkActivitySelectServerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = SkActivitySelectServerBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        viewInit()
        listenerInit()
    }

    private fun listenerInit(){
        mBinding.apply {
            backIv.setOnClickListener {
                finish()
            }
        }
    }

    private fun viewInit(){
        mBinding.apply {
            recyclerView.adapter = SkSelectServerAdapter().apply {
                setSelectListener {
                    Log.d(TAG, "setSelectListener: $it")
                }
            }
        }
    }
}