package com.s.k.starknight.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.s.k.starknight.R
import com.s.k.starknight.ad.display.NativeAdViewWrapper
import com.s.k.starknight.databinding.SkActivityLanguageBinding
import com.s.k.starknight.sk

class SkLanguageActivity : BaseActivity() {
    private val isSetLanguage = sk.preferences.isSetAppLanguage
    private val mBinding by lazy { SkActivityLanguageBinding.inflate(layoutInflater) }

    override fun onDisplayNativeInfo(): Pair<String, NativeAdViewWrapper> {
        return sk.ad.languageNative to mBinding.nativeAdWrapper
    }

    override fun needShowNative(): Boolean {
        return true
    }

    override fun onRootView(): View {
        return mBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.apply {
            backIv.setOnClickListener {
                onReturnActivity()
            }
        }

        viewInit()
    }

    private fun viewInit(){
        mBinding.apply {
            backIv.isVisible = isSetLanguage
            okBtn.isVisible = isSetLanguage || !sk.user.isVip()
            val adapter = SkLanguageAdapter()
            okBtn.setOnClickListener {
//                ad.displayFullScreenAd(true) {
                    sk.preferences.isSetAppLanguage = true
                    sk.language.setLanguageCode(adapter.selectCode)
                    if (isSetLanguage) {
                        finish()
                        return@setOnClickListener
                    }
                    startActivity(Intent(this@SkLanguageActivity, MainActivity::class.java).apply {
                        intent.extras?.let {
                            putExtras(it)
                        }
                    })
                    finish()
//                }
            }
            recyclerview.adapter = adapter
        }

    }

    inner class SkLanguageAdapter : RecyclerView.Adapter<SkLanguageAdapter.ViewHolder>() {

        private val languageList = sk.language.appLanguageList
        var selectCode = if (sk.user.isVip() && !isSetLanguage) {
            ""
        } else {
            sk.preferences.quickLanguageCode
        }
            private set

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.sk_language_item, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            languageList[position].run {
                val isSelect = selectCode == first
                holder.languageTv.text = second
                if (isSelect){
                    holder.languageTv.setTextColor(getColor(R.color.sk_connected_state))
                }else{
                    holder.languageTv.setTextColor(getColor(R.color.white))
                }
                holder.languageCheckIv.setImageResource(if (isSelect) R.drawable.sk_ic_selected else R.drawable.sk_ic_selected_no)
                holder.itemView.setBackgroundResource(if (isSelect) R.drawable.sk_ic_selected_bg else R.drawable.sk_ic_select_normal)
                holder.itemView.setOnClickListener {
                    if (isSelect) return@setOnClickListener
                    selectCode = first
                    notifyDataSetChanged()
                    mBinding.okBtn.isVisible = true
                }
            }
        }

        override fun getItemCount(): Int {
            return languageList.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val languageTv = itemView.findViewById<TextView>(R.id.languageTv)
            val languageCheckIv = itemView.findViewById<ImageView>(R.id.languageCheckIv)
        }
    }
}