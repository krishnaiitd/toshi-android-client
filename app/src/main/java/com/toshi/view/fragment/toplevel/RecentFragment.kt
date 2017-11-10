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

package com.toshi.view.fragment.toplevel

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toshi.R
import com.toshi.extensions.getPxSize
import com.toshi.extensions.startActivity
import com.toshi.model.local.Conversation
import com.toshi.view.activity.ChatActivity
import com.toshi.view.activity.NewConversationActivity
import com.toshi.view.adapter.RecentAdapter
import com.toshi.view.custom.HorizontalLineDivider
import com.toshi.view.fragment.DialogFragment.ConversationOptionsDialogFragment
import com.toshi.viewModel.RecentViewModel
import kotlinx.android.synthetic.main.fragment_recent.*

class RecentFragment: Fragment() {

    private lateinit var viewModel: RecentViewModel
    private lateinit var recentAdapter: RecentAdapter

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_recent, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) = init()

    private fun init() {
        initViewModel()
        initRecentAdapter()
        getRecentConversations()
        initClickListeners()
        initObservers()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(activity).get(RecentViewModel::class.java)
    }

    private fun initRecentAdapter() {
        recentAdapter = RecentAdapter()
                .setOnItemClickListener { startActivity<ChatActivity> { putExtra(ChatActivity.EXTRA__THREAD_ID, it.threadId) } }
                .setOnItemLongClickListener { viewModel.showConversationOptionsDialog(it.threadId) }

        recents.layoutManager = LinearLayoutManager(context)
        recents.itemAnimator = DefaultItemAnimator()
        recents.adapter = recentAdapter
        addSwipeToDeleteListener(recents)

        val dividerLeftPadding = getPxSize(R.dimen.avatar_size_small)
        + getPxSize(R.dimen.activity_horizontal_margin)
        + getPxSize(R.dimen.list_item_avatar_margin)
        val dividerRightPadding = getPxSize(R.dimen.activity_horizontal_margin)
        val lineDivider = HorizontalLineDivider(ContextCompat.getColor(context, R.color.divider))
                .setRightPadding(dividerRightPadding)
                .setLeftPadding(dividerLeftPadding)
        recents.addItemDecoration(lineDivider)
    }

    private fun getRecentConversations() = viewModel.getRecentConversations()

    private fun initClickListeners() {
        startChat.setOnClickListener { startActivity<NewConversationActivity>() }
        add.setOnClickListener { startActivity<NewConversationActivity>() }
    }

    private fun initObservers() {
        viewModel.conversations.observe(this, Observer {
            conversation -> conversation?.let { handleConversations(it) }
        })
        viewModel.updatedConversation.observe(this, Observer {
            conversation -> conversation?.let { handleConversation(it) }
        })
        viewModel.conversationInfo.observe(this, Observer {
            conversationInfo -> conversationInfo?.let { showConversationOptionsDialog(it.first, it.second) }
        })
        viewModel.deleteConversation.observe(this, Observer {
            deletedConversation -> deletedConversation?.let { removeConversation(it) }
        })
    }

    private fun handleConversations(conversations: List<Conversation>) {
        recentAdapter.setConversations(conversations)
        updateEmptyState()
    }

    private fun handleConversation(updatedConversation: Conversation) {
        recentAdapter.updateConversation(updatedConversation)
        updateEmptyState()
    }

    private fun showConversationOptionsDialog(conversation: Conversation, isBlocked: Boolean) {
        ConversationOptionsDialogFragment.newInstance(conversation.isMuted, isBlocked)
                .setItemClickListener({ viewModel.handleSelectedOption(conversation, it) })
                .show(fragmentManager, ConversationOptionsDialogFragment.TAG)
    }

    private fun removeConversation(conversation: Conversation) {
        recentAdapter.removeItem(conversation)
        updateEmptyState()
    }

    private fun addSwipeToDeleteListener(recyclerView: RecyclerView) {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView1: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                recentAdapter.removeItemAtWithUndo(viewHolder.adapterPosition, recyclerView)
                updateEmptyState()
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun updateEmptyState() {
        val showingEmptyState = emptyStateSwitcher.currentView.id == emptyState.id
        val shouldShowEmptyState = recentAdapter.itemCount == 0

        if (shouldShowEmptyState && !showingEmptyState) {
            emptyStateSwitcher.showPrevious()
        } else if (!shouldShowEmptyState && showingEmptyState) {
            emptyStateSwitcher.showNext()
        }
    }

    override fun onStop() {
        super.onStop()
        recentAdapter.doDelete()
    }
}