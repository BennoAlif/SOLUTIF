package com.sabeno.solutif.ui.detail

import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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

class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_REPORT = "extra_report"
        private const val ICON_ID = "ICON_ID"
    }


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

        binding.content.mapView.onCreate(savedInstanceState)
        binding.content.mapView.getMapAsync { mapboxMap ->
            this.mapboxMap = mapboxMap
            mapboxMap.uiSettings.isScrollGesturesEnabled = false
            if (reportData != null) {
                getReportData(reportData)
            }
        }

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = resources.getString(R.string.detail_laporan)
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

}