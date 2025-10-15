package com.s.k.starknight.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.s.k.starknight.R
import com.s.k.starknight.databinding.SkActivityApplicationBinding
import com.s.k.starknight.tools.Mode

class SkApplicationActivity : AppCompatActivity() {
    private val TAG = "SkApplicationActivity"
    private lateinit var mBinding: SkActivityApplicationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = SkActivityApplicationBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        viewInit()
        listenerInit()
    }

    private fun listenerInit(){
        mBinding.apply {
            backIv.setOnClickListener {
                finish()
            }
            globalLl.setOnClickListener {
                setMode(Mode.GLOBAL)
            }
            onlyBrowserLl.setOnClickListener {
                setMode(Mode.ONLY_BROWSER)
            }
            customLl.setOnClickListener {
                setMode(Mode.CUSTOM)
            }
        }
    }

    private fun viewInit(){
        setMode(Mode.GLOBAL)
        mBinding.apply {

        }
    }

    private fun setMode(mode: Mode){
        mBinding.apply {
            when(mode){
                Mode.GLOBAL -> {
                    globalCheckIv.setImageResource(R.drawable.sk_ic_selected)
                    onlyBrowserCheckIv.setImageResource(R.drawable.sk_ic_selected_no)
                    customCheckIv.setImageResource(R.drawable.sk_ic_selected_no)
                }
                Mode.ONLY_BROWSER -> {
                    globalCheckIv.setImageResource(R.drawable.sk_ic_selected_no)
                    onlyBrowserCheckIv.setImageResource(R.drawable.sk_ic_selected)
                    customCheckIv.setImageResource(R.drawable.sk_ic_selected_no)
                }
                Mode.CUSTOM -> {
                    globalCheckIv.setImageResource(R.drawable.sk_ic_selected_no)
                    onlyBrowserCheckIv.setImageResource(R.drawable.sk_ic_selected_no)
                    customCheckIv.setImageResource(R.drawable.sk_ic_selected)
                }
            }
        }
    }

}