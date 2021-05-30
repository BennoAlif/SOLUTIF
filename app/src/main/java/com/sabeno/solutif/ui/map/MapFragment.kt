package com.sabeno.solutif.ui.map

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.sabeno.solutif.R
import com.sabeno.solutif.data.source.Report
import com.sabeno.solutif.databinding.FragmentMapBinding
import com.sabeno.solutif.ui.detail.DetailActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MapFragment : Fragment() {

    companion object {
        private const val ICON_ID = "ICON_ID"
    }

    private val mapViewModel: MapViewModel by inject()

    private lateinit var binding: FragmentMapBinding

    private lateinit var mapboxMap: MapboxMap
    private lateinit var locationComponent: LocationComponent
    private lateinit var myLocation: LatLng
    private lateinit var permissionsManager: PermissionsManager

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        binding.mapView.onCreate(savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        coroutineScope.launch {
            activity?.let { mapViewModel.setReports(it) }
        }

        mapViewModel.getReports().observe(viewLifecycleOwner, { report ->
            showReportMarker(report)
        })
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    private fun showReportMarker(dataReports: List<Report>?) {
        var mapboxStyle = Style.LIGHT

        when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> mapboxStyle = Style.DARK
            Configuration.UI_MODE_NIGHT_NO -> mapboxStyle = Style.LIGHT
            Configuration.UI_MODE_NIGHT_UNDEFINED -> mapboxStyle = Style.LIGHT
        }

        binding.mapView.getMapAsync { mapboxMap ->
            this.mapboxMap = mapboxMap
            mapboxMap.setStyle(mapboxStyle) { style ->
                showMyLocation(style)

                style.addImage(
                    ICON_ID,
                    BitmapFactory.decodeResource(resources, R.drawable.mapbox_marker_icon_default)
                )
                val latLngBoundsBuilder = LatLngBounds.Builder()

                val symbolManager = SymbolManager(binding.mapView, mapboxMap, style)
                symbolManager.iconAllowOverlap = true
                val options = ArrayList<SymbolOptions>()
                dataReports?.forEach { data ->
                    latLngBoundsBuilder.include(LatLng(data.latitude!!, data.longitude!!))
                    options.add(
                        SymbolOptions()
                            .withLatLng(LatLng(data.latitude!!, data.longitude!!))
                            .withIconImage(ICON_ID)
                            .withData(Gson().toJsonTree(data))
                    )
                }
                symbolManager.create(options)

                val latLngBounds = latLngBoundsBuilder.build()
                mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 50), 5000)

                symbolManager.addClickListener { symbol ->
                    val data = Gson().fromJson(symbol.data, Report::class.java)
                    val intent = Intent(context, DetailActivity::class.java)
                    intent.putExtra(DetailActivity.EXTRA_REPORT, data)
                    startActivity(intent)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun showMyLocation(style: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(context)) {
            val locationComponentOptions = context?.let {
                LocationComponentOptions.builder(it)
                    .pulseEnabled(true)
                    .pulseColor(Color.BLUE)
                    .pulseAlpha(.4f)
                    .pulseInterpolator(BounceInterpolator())
                    .build()
            }
            val locationComponentActivationOptions = context?.let {
                LocationComponentActivationOptions
                    .builder(it, style)
                    .locationComponentOptions(locationComponentOptions)
                    .build()
            }
            locationComponent = mapboxMap.locationComponent
            if (locationComponentActivationOptions != null) {
                locationComponent.activateLocationComponent(locationComponentActivationOptions)
            }
            locationComponent.isLocationComponentEnabled = true
            locationComponent.cameraMode = CameraMode.TRACKING
            locationComponent.renderMode = RenderMode.COMPASS

            myLocation = LatLng(
                locationComponent.lastKnownLocation?.latitude as Double,
                locationComponent.lastKnownLocation?.longitude as Double
            )
        } else {
            permissionsManager = PermissionsManager(object : PermissionsListener {
                override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.location_permission),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onPermissionResult(granted: Boolean) {
                    if (granted) {
                        mapboxMap.getStyle { style ->
                            showMyLocation(style)
                        }
                    } else {
                        activity?.finish()
                    }
                }
            })
            permissionsManager.requestLocationPermissions(activity)
        }
    }

}