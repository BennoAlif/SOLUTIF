package com.sabeno.solutif.ui.detail

import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.sabeno.solutif.R
import com.sabeno.solutif.data.source.Report
import com.sabeno.solutif.databinding.ActivityDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_REPORT = "extra_report"
        private const val ICON_ID = "ICON_ID"
    }

    private val detailViewModel: DetailViewModel by inject()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private lateinit var binding: ActivityDetailBinding

    private lateinit var mapboxMap: MapboxMap
    private lateinit var symbolManager: SymbolManager

    private var reportLocation = LatLng()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val reportId = intent.getStringExtra(EXTRA_ID)
        val reportData = intent.getParcelableExtra<Report>(EXTRA_REPORT)

        var statusReport = reportData?.isDone

        setIsDone(statusReport)
        binding.fab.setOnClickListener {
            statusReport = !statusReport!!
            setIsDone(statusReport)
            coroutineScope.launch {
                if (reportId != null) {
                    detailViewModel.updateReportStatus(
                        reportId,
                        statusReport!!,
                        this@DetailActivity
                    )
                }
            }
        }

        binding.content.mapView.onCreate(savedInstanceState)
        binding.content.mapView.getMapAsync { mapboxMap ->
            this.mapboxMap = mapboxMap
            mapboxMap.uiSettings.isScrollGesturesEnabled = false
            if (reportData != null) {
                getReportData(reportData)
            }
        }

        detailViewModel.spinner.observe(this, { value ->
            value.let { show ->
                binding.content.btnDelete.isEnabled = !show
            }
        })

        detailViewModel.toast.observe(this, { message ->
            message?.let {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                detailViewModel.onToastShown()
            }
        })

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = resources.getString(R.string.detail_laporan)

        binding.content.btnDelete.setOnClickListener {
            coroutineScope.launch {
                if (reportId != null) {
                    detailViewModel.deleteReport(reportId, this@DetailActivity)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.content.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.content.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.content.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.content.mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.content.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.content.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.content.mapView.onLowMemory()
    }

    private fun getReportData(report: Report) {
        binding.content.tvDesc.text = report.description
        binding.content.tvTimestamp.text = report.createdAt?.toDate().toString()
        Glide.with(this)
            .load(report.photoUrl)
            .into(binding.ivPhoto)

        reportLocation = LatLng(report.latitude!!, report.longitude!!)
        showMarker(reportLocation)
    }

    private fun showMarker(latLong: LatLng) {
        var mapboxStyle = Style.LIGHT

        when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> mapboxStyle = Style.DARK
            Configuration.UI_MODE_NIGHT_NO -> mapboxStyle = Style.LIGHT
            Configuration.UI_MODE_NIGHT_UNDEFINED -> mapboxStyle = Style.LIGHT
        }
        mapboxMap.setStyle(mapboxStyle) { style ->
            symbolManager = SymbolManager(binding.content.mapView, mapboxMap, style)
            symbolManager.iconAllowOverlap = true

            style.addImage(
                ICON_ID,
                BitmapFactory.decodeResource(resources, R.drawable.mapbox_marker_icon_default)
            )

            symbolManager.create(
                SymbolOptions()
                    .withLatLng(latLong)
                    .withIconImage(ICON_ID)
                    .withIconSize(1.5f)
                    .withIconOffset(arrayOf(0f, -1.5f))
            )

            mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(reportLocation, 15.0))

        }
    }

    private fun setIsDone(isDone: Boolean?) {
        if (isDone == true) {
            binding.fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_close))
        } else {
            binding.fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_done))
        }
    }

}