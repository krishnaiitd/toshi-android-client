package com.toshi.view.fragment.toplevel

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.toshi.R
import com.toshi.extensions.getPxSize
import com.toshi.extensions.startActivity
import com.toshi.extensions.startExternalActivity
import com.toshi.util.OnSingleClickListener
import com.toshi.view.activity.ContactSearchActivity
import com.toshi.view.activity.ScannerActivity
import com.toshi.view.activity.ViewUserActivity
import com.toshi.view.adapter.UserAdapter
import com.toshi.view.custom.HorizontalLineDivider
import com.toshi.viewModel.FavoritesViewModel
import kotlinx.android.synthetic.main.fragment_contacts.*



class FavoritesFragment: Fragment() {

    companion object {
        private const val PLAIN_TEXT = "text/plain"
    }

    private lateinit var viewModel: FavoritesViewModel
    private lateinit var adapter: UserAdapter

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, inState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_contacts, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) = init()

    private fun init() {
        initMenu()
        initViewModel()
        initClickListeners()
        initRecyclerView()
        initObservers()
        getContacts()
    }

    private fun initMenu() {
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
        setHasOptionsMenu(true)
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(activity).get(FavoritesViewModel::class.java)
    }

    private fun initClickListeners() {
        userSearch.setOnClickListener { handleUserSearchClicked.onClick(it) }
        inviteFriends.setOnClickListener { handleInviteFriendsClicked.onClick(it) }
    }

    private val handleUserSearchClicked = object : OnSingleClickListener() {
        override fun onSingleClick(v: View?) {
            startActivity<ContactSearchActivity>()
        }
    }

    private val handleInviteFriendsClicked = object : OnSingleClickListener() {
        override fun onSingleClick(v: View?) {
            handleInviteFriends()
        }
    }

    private fun initRecyclerView() {
        this.adapter = UserAdapter()
                .setOnItemClickListener({
                    startActivity<ViewUserActivity> { putExtra(ViewUserActivity.EXTRA__USER_ADDRESS, it.toshiId) }
                })

        favorites.layoutManager = LinearLayoutManager(context)
        favorites.itemAnimator = DefaultItemAnimator()
        favorites.adapter = this.adapter

        val dividerLeftPadding = getPxSize(R.dimen.avatar_size_small)
                + getPxSize(R.dimen.activity_horizontal_margin)
                + getPxSize(R.dimen.list_item_avatar_margin)
        val dividerRightPadding = getPxSize(R.dimen.activity_horizontal_margin)
        val lineDivider = HorizontalLineDivider(ContextCompat.getColor(context, R.color.divider))
                .setRightPadding(dividerRightPadding)
                .setLeftPadding(dividerLeftPadding)
        favorites.addItemDecoration(lineDivider)
    }

    private fun handleInviteFriends() = startExternalActivity {
        action = Intent.ACTION_SEND
        type = PLAIN_TEXT
        putExtra(Intent.EXTRA_TEXT, getString(R.string.invite_friends_intent_message))
    }

    private fun initObservers() {
        viewModel.contacts.observe(this, Observer {
            adapter.mapContactsToUsers(it)
            updateEmptyState()
        })
    }

    private fun updateEmptyState() {
        if (adapter.itemCount == 0) {
            emptyState.visibility = View.VISIBLE
            favorites.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            favorites.visibility = View.VISIBLE
        }
    }

    private fun getContacts() = viewModel.loadContacts()

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.contacts, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.scan_qr -> startActivity<ScannerActivity>()
            R.id.invite_friends -> handleInviteFriends()
            R.id.search_people -> startActivity<ContactSearchActivity>()
        }
        return super.onOptionsItemSelected(item)
    }
}