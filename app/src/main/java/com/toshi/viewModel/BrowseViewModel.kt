package com.toshi.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.toshi.model.local.ToshiEntity
import com.toshi.model.local.User
import com.toshi.model.network.App
import com.toshi.util.LogUtil
import com.toshi.util.SingleLiveEvent
import com.toshi.view.BaseApplication
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription
import java.util.*

class BrowseViewModel: ViewModel() {

    private val subscriptions by lazy { CompositeSubscription() }

    val search by lazy { SingleLiveEvent<List<ToshiEntity>>() }
    val topRatedApps by lazy { MutableLiveData<List<App>>() }
    val featuredApps by lazy { MutableLiveData<List<App>>() }
    val topRatedPublicUsers by lazy { MutableLiveData<List<User>>() }
    val latestPublicUsers by lazy { MutableLiveData<List<User>>() }

    init {
        fetchTopRatedApps()
        fetchFeaturedApps()
        fetchTopRatedPublicUsers()
        fetchLatestPublicUsers()
    }

    private fun fetchTopRatedApps() {
        val sub = getAppManager()
                .getTopRatedApps(10)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { topRatedApps.value = it },
                        { LogUtil.exception(javaClass, "Error while fetching top rated apps $it") }
                )

        this.subscriptions.add(sub)
    }

    private fun fetchFeaturedApps() {
        val sub = getAppManager()
                .getLatestApps(10)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { featuredApps.value = it },
                        { LogUtil.exception(javaClass, "Error while fetching top rated apps $it") }
                )

        this.subscriptions.add(sub)
    }

    private fun getAppManager() = BaseApplication.get().appsManager

    private fun fetchTopRatedPublicUsers() {
        val sub = getUserManager()
                .getTopRatedPublicUsers(10)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { topRatedPublicUsers.value = it },
                        { LogUtil.exception(javaClass, "Error while fetching public users $it") }
                )

        this.subscriptions.add(sub)
    }

    private fun fetchLatestPublicUsers() {
        val sub = getUserManager()
                .getLatestPublicUsers(10)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { latestPublicUsers.value = it },
                        { LogUtil.exception(javaClass, "Error while fetching public users $it") }
                )

        this.subscriptions.add(sub)
    }

    private fun getUserManager() = BaseApplication.get().userManager

    fun runSearchQuery(query: String) {
        if (query.isEmpty()) return
        val sub = getRecipientManager()
                .searchOnlineUsers(query)
                .observeOn(AndroidSchedulers.mainThread())
                .map { users -> ArrayList<ToshiEntity>(users) }
                .subscribe(
                        { search.value = it },
                        { LogUtil.exception(javaClass, "Error while searching for app $it") }
                )

        this.subscriptions.add(sub)
    }

    private fun getRecipientManager() = BaseApplication.get().recipientManager

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }
}