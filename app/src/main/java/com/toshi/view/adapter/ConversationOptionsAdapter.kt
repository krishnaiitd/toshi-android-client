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

package com.toshi.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.toshi.R
import com.toshi.view.adapter.viewholder.ConversationOptionsViewHolder

class ConversationOptionsAdapter(
        private val options: List<String>,
        private val listener: (String) -> Unit):
        RecyclerView.Adapter<ConversationOptionsViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ConversationOptionsViewHolder {
        val layoutInflater = LayoutInflater.from(parent?.context)
        return ConversationOptionsViewHolder(layoutInflater.inflate(R.layout.list_item__conversation_option, parent, false))
    }

    override fun onBindViewHolder(holder: ConversationOptionsViewHolder?, position: Int) {
        val option = options[position]
        holder?.setOption(option)
                ?.setOnItemClickListener(option, listener)
    }

    override fun getItemCount() = options.size

}