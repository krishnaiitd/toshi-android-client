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

package com.toshi.viewModel

import android.arch.lifecycle.ViewModel
import android.util.Pair
import com.toshi.model.local.Conversation
import com.toshi.util.LogUtil
import com.toshi.util.SingleLiveEvent
import com.toshi.view.BaseApplication
import com.toshi.view.fragment.DialogFragment.Option
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class RecentViewModel: ViewModel() {

    private val subscriptions by lazy { CompositeSubscription() }

    val conversations by lazy { SingleLiveEvent<MutableList<Conversation>>() }
    val updatedConversation by lazy { SingleLiveEvent<Conversation>() }
    val conversationInfo by lazy { SingleLiveEvent<Pair<Conversation, Boolean>>() }
    val deleteConversation by lazy { SingleLiveEvent<Conversation>() }

    init {
        attachSubscriber()
    }

    private fun attachSubscriber() {
        val sub = getSofaMessageManager()
                .registerForAllConversationChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { updatedConversation.value = it },
                        { LogUtil.e(javaClass, "Error fetching conversations $it") }
                )

        this.subscriptions.add(sub)
    }

    fun getRecentConversations() {
        val sub = getSofaMessageManager()
                .loadAllConversations()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { conversations.value = it },
                        { LogUtil.e(javaClass, "Error fetching conversations $it") }
                )

        this.subscriptions.add(sub)
    }

    fun showConversationOptionsDialog(threadId: String) {
        val sub = Single.zip(
                getConversation(threadId),
                isBlocked(threadId),
                { first, second -> Pair(first, second) }
        )
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
                { conversationInfo.value = it },
                { LogUtil.e(javaClass, "Error: $it") }
        )

        this.subscriptions.add(sub)
    }

    private fun isBlocked(threadId: String): Single<Boolean> {
        return BaseApplication
                .get()
                .recipientManager
                .isUserBlocked(threadId)
    }

    private fun getConversation(threadId: String): Single<Conversation> {
        return getSofaMessageManager()
                .loadConversation(threadId)
    }

    fun handleSelectedOption(conversation: Conversation, option: Option) {
        when (option) {
            Option.UNMUTE -> unmuteConversation(conversation)
            Option.MUTE -> muteConversation(conversation)
            Option.UNBLOCK -> unblockConversation(conversation)
            Option.BLOCK -> blockConversation(conversation)
            Option.DELETE -> deleteConversation(conversation)
        }
    }

    private fun unmuteConversation(conversation: Conversation) {
        val sub = getSofaMessageManager()
                .unmuteConversation(conversation.threadId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { },
                        { LogUtil.e(javaClass, "Error while unmuting conversation $it") }
                )

        this.subscriptions.add(sub)
    }

    private fun muteConversation(conversation: Conversation) {
        val sub = getSofaMessageManager()
                .muteConveration(conversation.threadId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { },
                        { LogUtil.e(javaClass, "Error while muting conversation $it") }
                )

        this.subscriptions.add(sub)
    }

    private fun unblockConversation(conversation: Conversation) {
        val sub = getRecipientManager()
                .unblockUser(conversation.threadId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { },
                        { LogUtil.e(javaClass, "Error while blocking user $it") }
                )

        this.subscriptions.add(sub)
    }

    private fun blockConversation(conversation: Conversation) {
        val sub = getRecipientManager()
                .blockUser(conversation.threadId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { },
                        { LogUtil.e(javaClass, "Error while blocking user $it") }
                )

        this.subscriptions.add(sub)
    }

    private fun deleteConversation(conversation: Conversation) {
        val sub = getSofaMessageManager()
                .deleteConversation(conversation)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { deleteConversation.value = conversation },
                        { LogUtil.e(javaClass, "Error while blocking user $it") }
                )

        this.subscriptions.add(sub)
    }

    private fun getSofaMessageManager() = BaseApplication.get().sofaMessageManager

    private fun getRecipientManager() = BaseApplication.get().recipientManager

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }
}