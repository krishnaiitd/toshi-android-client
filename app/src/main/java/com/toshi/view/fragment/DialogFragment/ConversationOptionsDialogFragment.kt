/*
 * 	Copyright (c) 2017. Toshi Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.toshi.view.fragment.DialogFragment

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.toshi.R
import com.toshi.view.adapter.ConversationOptionsAdapter

class ConversationOptionsDialogFragment: DialogFragment() {

    companion object {
        const val TAG = "ConversationOptionsDialogFragment"
        private const val IS_MUTED = "isMuted"
        private const val IS_BLOCKED = "isBlocked"

        fun newInstance(isMuted: Boolean, isBlocked: Boolean): ConversationOptionsDialogFragment {
            val bundle = Bundle()
            bundle.apply {
                putBoolean(IS_MUTED, isMuted)
                putBoolean(IS_BLOCKED, isBlocked)
            }
            return ConversationOptionsDialogFragment().apply { arguments = bundle }
        }
    }

    private var listener: ((Option) -> Unit)? = null
    private var isMuted = false
    private var isBlocked = false
    private lateinit var optionList: List<String>

    fun setItemClickListener(listener: (Option) -> Unit): ConversationOptionsDialogFragment {
        this.listener = listener
        return this
    }

    override fun onCreateDialog(state: Bundle?): Dialog {
        val dialog = super.onCreateDialog(state)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater?.inflate(R.layout.fragment_conversation_options, container)
        getOptionList()
        initView(v)
        return v
    }

    private fun getOptionList() {
        optionList = createOptionListFromBundle()
    }

    private fun createOptionListFromBundle(): List<String> {
        isMuted = arguments?.getBoolean(IS_MUTED) ?: false
        isBlocked = arguments?.getBoolean(IS_BLOCKED) ?: false
        val options = context.resources.getStringArray(R.array.conversation_options).toMutableList()
        if (isMuted) options[0] = context.getString(R.string.unmute)
        if (isBlocked) options[1] = context.getString(R.string.unblock)
        return options.toList()
    }

    private fun initView(v: View?) {
        val optionsList = v?.findViewById<RecyclerView>(R.id.options)
        optionsList?.adapter = ConversationOptionsAdapter(optionList, { handleOptionClicked(it) })
        optionsList?.layoutManager = LinearLayoutManager(context)
    }

    private fun handleOptionClicked(option: String) {
        val index = optionList.indexOf(option)
        val chosenOption = when {
            index == 0 && isMuted -> Option.UNMUTE
            index == 0 && !isMuted -> Option.MUTE
            index == 1 && isBlocked -> Option.UNBLOCK
            index == 1 && !isBlocked -> Option.BLOCK
            index == 2 -> Option.DELETE
            else -> null
        }
        chosenOption?.let { listener?.invoke(chosenOption) }
        dismiss()
    }

    override fun onPause() {
        super.onPause()
        dismissAllowingStateLoss()
    }
}

enum class Option {
    MUTE, UNMUTE, BLOCK, UNBLOCK, DELETE
}
