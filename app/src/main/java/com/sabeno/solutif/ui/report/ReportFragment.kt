package com.sabeno.solutif.ui.report

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.sabeno.solutif.data.source.Report
import com.sabeno.solutif.databinding.FragmentReportBinding
import com.sabeno.solutif.ui.detail.DetailActivity
import org.koin.android.ext.android.inject

class ReportFragment : Fragment() {

    private val reportViewModel: ReportViewModel by inject()

    private lateinit var reportAdapter: ReportAdapter

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        reportAdapter.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        reportAdapter.stopListening()
    }

    private fun setupRecyclerView() {
        reportAdapter = ReportAdapter(reportViewModel.getReports()!!)
        reportAdapter.setOnItemClickCallback(object : ReportAdapter.OnItemClickCallback {
            override fun onItemClicked(report: Report) {
                val intent = Intent(activity, DetailActivity::class.java)
                intent.putExtra(DetailActivity.EXTRA_ID, report.id)
                intent.putExtra(DetailActivity.EXTRA_REPORT, report)
                startActivity(intent)
            }
        })

        binding.rvReports.layoutManager = GridLayoutManager(context, 2)
        binding.rvReports.adapter = reportAdapter
    }
}