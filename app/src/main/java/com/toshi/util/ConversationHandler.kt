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

package com.toshi.util

import android.support.v4.app.Fragment
import android.util.Pair
import com.toshi.model.local.Conversation
import com.toshi.view.BaseApplication
import com.toshi.view.fragment.DialogFragment.ConversationOptionsDialogFragment
import com.toshi.view.fragment.DialogFragment.Option
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class ConversationHandler(
        private val fragment: Fragment,
        private val deleteListener:
        (Conversation) -> Unit) {

    private val subscriptions by lazy { CompositeSubscription() }

    fun showConversationOptionsDialog(conversation: Conversation) {
        val sub = Single.zip(
                isMuted(conversation),
                isBlocked(conversation),
                { first, second -> Pair(first, second) }
        )
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
                { pair -> showConversationOptionsDialog(conversation, pair.first, pair.second) },
                { throwable -> LogUtil.e(javaClass, "Error: " + throwable) }
        )

        this.subscriptions.add(sub)
    }

    private fun isBlocked(conversation: Conversation): Single<Boolean> {
        return BaseApplication
                .get()
                .recipientManager
                .isUserBlocked(conversation.threadId)
    }

    private fun isMuted(conversation: Conversation): Single<Boolean> {
        return BaseApplication
                .get()
                .sofaMessageManager
                .isConversationMuted(conversation.threadId)
    }

    private fun showConversationOptionsDialog(conversation: Conversation, isMuted: Boolean, isBlocked: Boolean) {
        val fragment = ConversationOptionsDialogFragment.newInstance(isMuted, isBlocked)
                .setItemClickListener({ handleSelectedOption(conversation, it) })
        fragment.show(this.fragment.fragmentManager, ConversationOptionsDialogFragment.TAG)
    }

    private fun handleSelectedOption(conversation: Conversation, option: Option) {
        when (option) {
            Option.UNMUTE -> unmuteConversation(conversation)
            Option.MUTE -> muteConversation(conversation)
            Option.UNBLOCK -> unblockConversation(conversation)
            Option.BLOCK -> blockConversation(conversation)
            Option.DELETE -> deleteConversation(conversation)
        }
    }

    private fun unmuteConversation(conversation: Conversation) {
        val sub = BaseApplication
                .get()
                .sofaMessageManager
                .unmuteConversation(conversation.threadId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { },
                        { throwable -> LogUtil.e(javaClass, "Error while unmuting conversation " + throwable) }
                )

        this.subscriptions.add(sub)
    }

    private fun muteConversation(conversation: Conversation) {
        val sub = BaseApplication
                .get()
                .sofaMessageManager
                .muteConveration(conversation.threadId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { },
                        { throwable -> LogUtil.e(javaClass, "Error while muting conversation " + throwable) }
                )

        this.subscriptions.add(sub)
    }

    private fun unblockConversation(conversation: Conversation) {
        val sub = BaseApplication
                .get()
                .recipientManager
                .unblockUser(conversation.threadId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { },
                        { throwable -> LogUtil.e(javaClass, "Error while blocking user " + throwable) }
                )

        this.subscriptions.add(sub)
    }

    private fun blockConversation(conversation: Conversation) {
        val sub = BaseApplication
                .get()
                .recipientManager
                .blockUser(conversation.threadId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { },
                        { throwable -> LogUtil.e(javaClass, "Error while blocking user " + throwable) }
                )

        this.subscriptions.add(sub)
    }

    private fun deleteConversation(conversation: Conversation) {
        val sub = BaseApplication
                .get()
                .sofaMessageManager
                .deleteConversation(conversation)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { deleteListener(conversation) },
                        { throwable -> LogUtil.e(javaClass, "Error while blocking user " + throwable) }
                )

        this.subscriptions.add(sub)
    }

    fun clear() = subscriptions.clear()
}