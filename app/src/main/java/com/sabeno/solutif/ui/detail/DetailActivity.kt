package com.sabeno.solutif.ui.detail

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import com.sabeno.solutif.R
import com.sabeno.solutif.core.data.source.Report
import com.sabeno.solutif.databinding.ActivityDetailBinding
import com.sabeno.solutif.ui.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import retrofit2.Call
import retrofit2.Response

class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_REPORT = "extra_report"
        private const val ICON_ID = "ICON_ID"
    }

    private val detailViewModel: DetailViewModel by inject()
    private val authViewModel: AuthViewModel by inject()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private lateinit var binding: ActivityDetailBinding

    private lateinit var mapboxMap: MapboxMap
    private lateinit var symbolManager: SymbolManager
    private lateinit var navigationMapRoute: NavigationMapRoute
    private var currentRoute: DirectionsRoute? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private var reportLocation = LatLng()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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

            binding.content.btnRoute.setOnClickListener {
                val destination = Point.fromLngLat(reportData?.longitude!!, reportData.latitude!!)
                val origin = Point.fromLngLat(longitude, latitude)
                requestRoute(origin, destination)
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

        authViewModel.currentUserLD.observe(this, { currentUser ->
            binding.fab.isGone = currentUser.isPetugas == false
            binding.content.btnRoute.isGone = currentUser.isPetugas == false
        })

        generateCurrentLocation()
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

            navigationMapRoute = NavigationMapRoute(
                null,
                binding.content.mapView,
                mapboxMap,
                R.style.NavigationMapRoute
            )
        }
    }

    private fun setIsDone(isDone: Boolean?) {
        if (isDone == true) {
            binding.fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_close))
        } else {
            binding.fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_done))
        }
    }

    private fun requestRoute(origin: Point, destination: Point) {
        navigationMapRoute.updateRouteVisibilityTo(false)
        NavigationRoute.builder(this)
            .accessToken(getString(R.string.mapbox_access_token))
            .origin(origin)
            .destination(destination)
            .build()
            .getRoute(object : retrofit2.Callback<DirectionsResponse> {
                override fun onResponse(
                    call: Call<DirectionsResponse>,
                    response: Response<DirectionsResponse>
                ) {
                    if (response.body() == null) {
                        Toast.makeText(
                            this@DetailActivity,
                            "No routes found, make sure you set the right user and access token.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    } else if (response.body()?.routes()?.size == 0) {
                        Toast.makeText(this@DetailActivity, "No routes found.", Toast.LENGTH_SHORT)
                            .show()
                        return
                    }
                    currentRoute = response.body()?.routes()?.get(0)

                    navigationMapRoute.addRoute(currentRoute)

                    showNavigation()
                }

                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                    Toast.makeText(this@DetailActivity, "Error : $t", Toast.LENGTH_SHORT).show()
                }

            })
    }

    @SuppressLint("MissingPermission")
    private fun generateCurrentLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                }
            }
    }

    private fun showNavigation() {
        val simulateRoute = true

        val options = NavigationLauncherOptions.builder()
            .directionsRoute(currentRoute)
            .shouldSimulateRoute(simulateRoute)
            .build()

        NavigationLauncher.startNavigation(this, options)

    }

}