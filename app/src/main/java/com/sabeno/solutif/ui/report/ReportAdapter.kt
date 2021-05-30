package com.sabeno.solutif.ui.report

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.sabeno.solutif.data.source.Report
import com.sabeno.solutif.databinding.ItemReportBinding

class ReportAdapter(options: FirestoreRecyclerOptions<Report>) :
    FirestoreRecyclerAdapter<Report, ReportAdapter.ReportViewHolder>(options) {

    private lateinit var onItemClickCallback: OnItemClickCallback

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int, model: Report) {
        holder.bind(model)
        val data = snapshots.getSnapshot(position).toObject(Report::class.java)
        val id = snapshots.getSnapshot(position).id
        holder.itemView.setOnClickListener {
            if (data != null) {
                onItemClickCallback.onItemClicked(data, id)
            }
        }
    }

    inner class ReportViewHolder(private val binding: ItemReportBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(report: Report) {
            binding.tvDesc.text = report.description
            binding.tvTimestamp.text = report.createdAt?.toDate().toString().take(10)
            binding.chip.isGone = report.isDone != true
            Glide.with(itemView.context)
                .load(report.photoUrl)
                .into(binding.ivPhoto)
        }
    }

    interface OnItemClickCallback {
        fun onItemClicked(report: Report, id: String)
    }
}