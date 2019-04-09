package org.wordpress.android.ui.stats.refresh.lists.sections.insights.management

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.insights_management_fragment.*
import javax.inject.Inject
import android.animation.LayoutTransition
import org.wordpress.android.R
import org.wordpress.android.ui.stats.refresh.lists.sections.insights.management.InsightsManagementViewModel.InsightModel
import org.wordpress.android.util.DisplayUtils
import org.wordpress.android.widgets.RecyclerItemDecoration

class InsightsManagementFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: InsightsManagementViewModel
    private lateinit var addedInsightsTouchHelper: ItemTouchHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.insights_management_fragment, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater?.inflate(R.menu.menu_insights_management, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save_insights) {
            viewModel.onSaveInsights()
        }
        return true
    }

    private fun initializeViews() {
        removedInsights.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        addedInsights.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)

        removedInsights.addItemDecoration(RecyclerItemDecoration(0, DisplayUtils.dpToPx(activity, 1)))
        addedInsights.addItemDecoration(RecyclerItemDecoration(0, DisplayUtils.dpToPx(activity, 1)))

        val transition = LayoutTransition()
        transition.disableTransitionType(LayoutTransition.DISAPPEARING)
        transition.enableTransitionType(LayoutTransition.CHANGING)
        insightsManagementContainer.layoutTransition = transition
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews()
        initializeViewModels(requireActivity())
    }

    private fun initializeViewModels(activity: FragmentActivity) {
        viewModel = ViewModelProviders.of(activity, viewModelFactory).get(InsightsManagementViewModel::class.java)
        setupObservers()
        viewModel.start()
    }

    private fun setupObservers() {
        viewModel.removedInsights.observe(this, Observer {
            it?.let { items ->
                updateRemovedInsights(items)

                if (items.isEmpty()) {
                    addInsightsHeader.visibility = View.GONE
                } else {
                    addInsightsHeader.visibility = View.VISIBLE
                }
            }
        })

        viewModel.addedInsights.observe(this, Observer {
            it?.let { items ->
                updateAddedInsights(items)

                if (items.isEmpty()) {
                    addedInsightsInfo.visibility = View.GONE
                } else {
                    addedInsightsInfo.visibility = View.VISIBLE
                }
            }
        })

        viewModel.closeInsightsManagement.observe(this, Observer {
            requireActivity().finish()
        })
    }

    private fun updateRemovedInsights(insights: List<InsightModel>) {
        if (removedInsights.adapter == null) {
            removedInsights.adapter = InsightsManagementAdapter(
                    { item -> viewModel.onItemButtonClicked(item) },
                    { viewHolder -> addedInsightsTouchHelper.startDrag(viewHolder) },
                    { list -> viewModel.onAddedInsightsReordered(list) }
            )
        }
        val adapter = removedInsights.adapter as InsightsManagementAdapter
        adapter.update(insights)
    }

    private fun updateAddedInsights(insights: List<InsightModel>) {
        var adapter = addedInsights.adapter as? InsightsManagementAdapter
        if (adapter == null) {
            adapter = InsightsManagementAdapter(
                    { item -> viewModel.onItemButtonClicked(item) },
                    { viewHolder -> addedInsightsTouchHelper.startDrag(viewHolder) },
                    { list -> viewModel.onAddedInsightsReordered(list) }
            )
            addedInsights.adapter = adapter

            val callback = ItemTouchHelperCallback(adapter)
            addedInsightsTouchHelper = ItemTouchHelper(callback)
            addedInsightsTouchHelper.attachToRecyclerView(addedInsights)
        }
        adapter.update(insights)
    }
}