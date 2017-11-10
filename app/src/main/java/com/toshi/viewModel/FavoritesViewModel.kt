package com.toshi.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.toshi.model.local.Contact
import com.toshi.util.LogUtil
import com.toshi.view.BaseApplication
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription

class FavoritesViewModel: ViewModel() {

    private val subscriptions by lazy { CompositeSubscription() }
    val contacts by lazy { MutableLiveData<List<Contact>>() }

    fun loadContacts() {
        val sub = getRecipientManager()
                .loadAllContacts()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { contacts.value = it },
                        { LogUtil.exception(javaClass, "Error while fetching contacts $it") }
                )

        this.subscriptions.add(sub)
    }

    private fun getRecipientManager() = BaseApplication.get().recipientManager

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }
}