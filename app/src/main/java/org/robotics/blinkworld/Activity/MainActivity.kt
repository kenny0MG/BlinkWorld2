package org.robotics.blinkworld.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.PointF
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import android.os.*
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point.fromLngLat
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.expressions.dsl.generated.zoom
import com.mapbox.maps.plugin.animation.CameraAnimatorOptions.Companion.cameraAnimatorOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMoveListener
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.robotics.blinkworld.Activity.NoPremissionActivity.NoGpsPremissionActivity
import org.robotics.blinkworld.Activity.RegistrationPhone.LoginRegistrationActivity
import org.robotics.blinkworld.BottomFragmaent.AddFriendsFragment.Fragment.AddFriendsFragment
import org.robotics.blinkworld.BottomFragmaent.EditProfile.Fragment.EditProfileFragmentFragment
import org.robotics.blinkworld.BottomFragmaent.Messages.Fragment.ChatsFragment
import org.robotics.blinkworld.BottomFragmaent.OtherUserProfile.Fragment.OtherUserProfileFragment
import org.robotics.blinkworld.BottomFragmaent.Search.Fragment.SearchFragment
import org.robotics.blinkworld.BottomFragmaent.UserProfile.Fragment.UserProfileFragment
import org.robotics.blinkworld.R
import org.robotics.blinkworld.Utils.*
import org.robotics.blinkworld.databinding.ViewMarkerBinding
import org.robotics.blinkworld.models.Chat
import org.robotics.blinkworld.models.Message
import org.robotics.blinkworld.models.MyMarker
import org.robotics.blinkworld.models.User
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*


class MainActivity : AppCompatActivity() {


    var mapView: MapView? = null
    private lateinit var map: MapboxMap
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var lastLocation: Location? = null
    private lateinit var mUser: User
    lateinit var settingsClient: SettingsClient

    lateinit var locationRequest: LocationRequest

    lateinit var locationCallback: LocationCallback


    private lateinit var point: com.mapbox.geojson.Point
    private val markerList = mutableListOf<MyMarker>()
    private var mListItems = listOf<Chat>()
    private var count = 0
    private var position: com.mapbox.geojson.Point? = null

    private var startX: Float = 0f
    private var startY: Float = 0f


    //val viewAnnotationManager = mapView!!.viewAnnotationManager


    private lateinit var searchButton: ImageView
    private lateinit var messageButton: ImageView
    private lateinit var profileButton: ImageView
    private lateinit var currentButton: AppCompatButton
    private lateinit var gpsMainText: TextView
    private lateinit var settingsImage: ImageView


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView = MapView(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mapView = findViewById(R.id.mapView)
        gpsMainText = findViewById(R.id.textView_GPS_Name)
        searchButton = findViewById(R.id.search_button)
        messageButton = findViewById(R.id.message_button)
        profileButton = findViewById(R.id.profile_button)
        currentButton = findViewById(R.id.current_position_button)
        settingsImage = findViewById(R.id.image_main_settings)





        mapView!!.getMapboxMap().loadStyleUri(
            "mapbox://styles/blinkboss/clekjhq5t006o01qgxmwozfee"

        ) {
            mapView!!.camera.apply {
                val zoom = createZoomAnimator(
                    cameraAnimatorOptions(14.0) {
                        startValue(3.0)
                    }
                ) {
                    duration = 4000
                    interpolator = AccelerateDecelerateInterpolator()
                }

                playAnimatorsSequentially(zoom)


            }


        }


        //Подчеркивание главного текста GPS
        gpsMainText.paintFlags = gpsMainText.paintFlags or Paint.UNDERLINE_TEXT_FLAG







        gpsMainText.text = "The world is yours!"





        //бработчик нажатий
        searchButton.setOnClickListener {
            val bottomfrSheets = SearchFragment()
            val fragmentManager = supportFragmentManager
            fragmentManager.let {
                bottomfrSheets.show(it, "")
            }

           // startActivity(Intent(this , MainActivity2::class.java))

        }

        image_main_add_friends.setOnClickListener {
            val bottomfrSheets = AddFriendsFragment()
            val fragmentManager = supportFragmentManager
            fragmentManager.let {
                bottomfrSheets.show(it, "")
            }
        }

        settingsImage.setOnClickListener {
            val bottomfrSheets = EditProfileFragmentFragment()
            val fragmentManager = supportFragmentManager
            fragmentManager.let {
                bottomfrSheets.show(it, "")
            }
        }


        currentButton.setOnClickListener {
            database.child(NODE_USERS).child(currentUid()!!)
                .addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter {
                    mUser = it.getValue(User::class.java)!!
                    mapView!!.getMapboxMap().setCamera(

                        CameraOptions.Builder()
                            .zoom(15.0).center(fromLngLat(mUser.longitude, mUser.latitude))
                            .build(),
                        )

                })
            vibratePhone()
        }


        messageButton.setOnClickListener {
            val bottomfrSheets = ChatsFragment()
            val fragmentManager = supportFragmentManager
            fragmentManager.let {
                bottomfrSheets.show(it, "")
            }
        }

        profileButton.setOnClickListener {
            val bottomfrSheets = UserProfileFragment()
            val fragmentManager = supportFragmentManager
            fragmentManager.let {
                bottomfrSheets.show(it, "")
            }
        }



        //Проверка зарегестрирован пользователь или н
        //основные функции
        if(auth.currentUser == null){
            finish()
            val intent = Intent(this, LoginRegistrationActivity::class.java)
            startActivities(arrayOf(intent))
        }else{
            if (!checkPermissions()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions()
                    val intent = Intent(this, NoGpsPremissionActivity::class.java)
                    startActivities(arrayOf(intent))
                    finish()
                }
            }
            else {
                CoroutineScope(Dispatchers.Main).launch {
                    onMapReady()
                    showNotificationUser()
                    val mainHandler = Handler(Looper.getMainLooper())

                    mainHandler.post(object : Runnable {
                        override fun run() {

                            showNotification()
                            mainHandler.postDelayed(this, 1000)
                        }
                    })
                    mainHandler.post(object : Runnable {
                        override fun run() {
                            createLocationRequest()

                            createLocationCallBack()

                            mainHandler.postDelayed(this, 1000)
                        }
                    })
                    mainHandler.post(object : Runnable {
                        override fun run() {

                            userLocation()
                            mainHandler.postDelayed(this, 100000)
                        }
                    })
                    loadUser()



                }
        }




        }

        //Стиль карты



        mapView?.logo?.updateSettings {
            enabled = false
        }
        mapView?.attribution?.updateSettings {
            enabled = false
        }
        mapView?.compass?.updateSettings {
            enabled = false
        }
        mapView?.scalebar?.updateSettings {
            enabled = false
        }

    //mapView?.getMapboxMap()!!.getMapOptions().glyphsRasterizationOptions!!.fontFamily!!.get(R.font.alata)




    }


    override fun onResume() {
        super.onResume()


    }



    override fun onStart() {
        super.onStart()
        StateUser.updateState(StateUser.ONLINE)


    }

    override fun onStop() {
        super.onStop()
        StateUser.updateState(StateUser.OFFLINE)
        //mapView!!.getMapboxMap().removeOnCameraChangeListener(listener)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }










    //Получение текущего местоположение
    private fun showSnackbar(
        mainTextStringId: String, actionStringId: String,
        listener: View.OnClickListener,
    ) {
        Toast.makeText(this@MainActivity, mainTextStringId, Toast.LENGTH_LONG).show()
    }
    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    }
    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }
    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) && ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            showSnackbar("Location permission is needed for core functionality", "Okay",
                View.OnClickListener {
                    startLocationPermissionRequest()
                })
        }
        else {
            Log.i(TAG, "Requesting permission")
            startLocationPermissionRequest()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> {
                    // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.")
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    // Permission granted.
                    createLocationRequest()

                    createLocationCallBack()
                }
                else -> {
                    showSnackbar("Permission was denied", "Settings",
                        View.OnClickListener {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()

                            val uri = Uri.fromParts(
                                "package",
                                Build.DISPLAY, null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }








































    @SuppressLint("MissingPermission")
    fun createLocationRequest() {

        locationRequest = LocationRequest().apply {
            interval = 5000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        settingsClient = LocationServices.getSettingsClient(this)

        val task: Task<LocationSettingsResponse> = settingsClient.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            fusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        }

        task.addOnFailureListener {exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    //exception.startResolutionForResult(this@MainActivity,
                    //REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    fun createLocationCallBack() {

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val locationLatitude =(locationResult.lastLocation!!.latitude)
                val locationLongitude = (locationResult.lastLocation!!.longitude)
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                val currentDate = sdf.format(Date()).toString()
                database.child(NODE_USERS).child(currentUid()!!)
                    .addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter {
                        mUser = it.getValue(User::class.java)!!
                        if((locationLatitude.toString()).take(6) != (mUser.latitude.toString()).take(6) && (locationResult.lastLocation!!.longitude.toString()).take(6) != (mUser.longitude.toString()).take(6)){
                            database.child(NODE_USERS).child(currentUid()!!).child("Time").setValue(currentDate)
                        }
                        database.child(NODE_USERS).child(currentUid()!!).child(CHILD_LATITUDE).setValue(locationLatitude)
                        database.child(NODE_USERS).child(currentUid()!!).child(CHILD_LONGITUDE).setValue(locationLongitude)


                    })





            }
        }

    }




//

    companion object {
        private val TAG = "LocationProvider"
        val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }



    private fun onMapReady() {
        database.child(NODE_USERS).child(currentUid()!!)
            .addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter {
                mUser = it.getValue(User::class.java)!!
                point = fromLngLat(mUser.longitude, mUser.latitude )
                initMarkerList(mUser.uid,point,
                    mUser.username,
                    mUser.photo!!,
                    mUser.state,
                    mUser.Time

                )
                markerList.forEach {
                    addViewAnnotation(it)
                    Log.d("MAP",markerList.toString())
                }

        mapView!!.getMapboxMap().setCamera(

            CameraOptions.Builder()
                .zoom(15.0).center(fromLngLat(mUser.longitude, mUser.latitude))
                .build(),




        )

            })





    }






//Перебор друзей пользователя для их вывода


    private fun loadUser() {
            val rootRef = database.child("friends").child(currentUid()!!)
            rootRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (singleSnapshot in snapshot.getChildren()) {
                        val followers =
                            singleSnapshot.getValue(User::class.java)!!
                        database.child(NODE_USERS).child(followers.uid).addValueEventListener(Utils.ValueEventListenerAdapter { dataSnapShot1 ->
                            val coordinate = dataSnapShot1.getUserModel()
                            point = fromLngLat(coordinate.longitude, coordinate.latitude )
                            initMarkerList(coordinate.uid,point,
                                coordinate.username,
                                coordinate.photo!!,
                                coordinate.state,
                                coordinate.Time

                            )
                            markerList.forEach {
                                addViewAnnotation(it)
                            }

                        })


                        }


                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("OtherProfileFragment",error.message)
                    gpsMainText.text = "No internet connection((("
                }


            })

        }
    private fun initMarkerList(uid:String,point:com.mapbox.geojson.Point,name:String,image:String,state:String,time:String) {
        markerList.clear()
        markerList.add(
            MyMarker(
                uid,
                name,
                image,
               point,
                state,
                time

            )
        )
    }



    //Вывод друзей пользователя

    @SuppressLint("NewApi")
    private fun addViewAnnotation(myMarker: MyMarker) {
        val viewAnnotation =
            mapView!!.viewAnnotationManager.addViewAnnotation(resId = R.layout.view_marker,
                options = viewAnnotationOptions {
                    this.allowOverlap(true)
                    geometry(myMarker.point)
                    offsetX(10)
                })

        ViewMarkerBinding.bind(viewAnnotation).apply {
           imageMarker.loadUserPhoto(myMarker.imageUrl)
            imageMarker.clipToOutline = true
            textStateMarker.text = myMarker.state
            timeMarker.visibility = View.GONE
            database.child(NODE_USERS).child(myMarker.uid)
                .addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter {
                    mUser = it.getValue(User::class.java)!!
                    if(mUser.Time.isNotEmpty()) {

                        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
                        val date = LocalDate.parse(mUser.Time.take(10), dateFormatter)
                        val time = LocalTime.parse(mUser.Time.takeLast(8), timeFormatter)
                        val then = date.atTime(time)
                        val now = LocalDateTime.now()
                        val duration = Duration.between(then, now)
                        val min = duration.toMinutes()
                        val hours = duration.toHours()
                        val days = duration.toDays()
                        if (min >= 5) {
                            timeMarker.visibility = View.VISIBLE
                            if (min >= 5 && min <60){
                                timeMarker.text = min.toString() + "m"
                            } else if (min >= 60) {
                                timeMarker.text = hours.toString() + "h"
                            } else if (hours >= 24 && hours <25) {
                                timeMarker.text = days.toString() + "d"
                            } else if (days >= 7) {
                                timeMarker.text = "1w"
                            } else if (days >= 14) {
                                timeMarker.text = "2w"
                            } else if (days >= 21) {
                                timeMarker.text = "3w"
                            } else if (days >= 28) {
                                timeMarker.text = "1M"
                            } else if (days >= 56) {
                                timeMarker.text = "1M+"
                            }
                            Log.d("Tag", hours.toString())
                            }

                    }else{
                        timeMarker.visibility = View.GONE
                    }








                })



//            if(myMarker.point == position){
//                textStateMarker.text = "Ok"
//            }
//            position = myMarker.point
            root.setOnClickListener {
                vibratePhone()

                    if(myMarker.uid == currentUid()!!){

                        val bottomfrSheets = UserProfileFragment()
                        val fragmentManager = supportFragmentManager
                        fragmentManager.let {
                            bottomfrSheets.show(it, "")
                        }
                    }else{
                        mapView!!.getMapboxMap().setCamera(

                            CameraOptions.Builder()
                                .zoom(16.0).center(myMarker.point)
                                .build(),



                            )
                        val geocoder = Geocoder(applicationContext, Locale.ENGLISH)
                        val latitude = myMarker.point.latitude()
                        val longitude = myMarker.point.longitude()
                        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                        Handler().postDelayed(Runnable {
                            userLocation()
                        }, 10000)
                        if(addresses!!.isNotEmpty()){
                            val address = addresses!!.first()
                            textView_GPS_location.text = myMarker.name + " location"
                            if(address.locality != null){
                                gpsMainText.text = address.locality.toString()
                                textView_GPS_location.visibility = View.VISIBLE
                            }else if(address.subLocality != null){
                                gpsMainText.text = address.subLocality.toString()
                                textView_GPS_location.visibility = View.VISIBLE
                            }else{
                                gpsMainText.text = address.countryName.toString()
                                textView_GPS_location.visibility = View.VISIBLE
                            }


                        }


                        val bottomfrSheets = OtherUserProfileFragment(myMarker.uid)
                        val fragmentManager = supportFragmentManager
                        fragmentManager.let {
                            bottomfrSheets.show(it, "")
                        }
                    }
                }
            }

        }



    //Вывод локации текущего пользователя
    private fun userLocation(){
        database.child(NODE_USERS).child(currentUid()!!)
            .addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter {
                mUser = it.getValue(User::class.java)!!



                val geocoder = Geocoder(this, Locale.ENGLISH)
                val latitude = mUser.latitude
                val longitude = mUser.longitude
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                Handler().postDelayed(Runnable {
                    if(addresses!!.isNotEmpty()){
                        val address = addresses!!.first()
                        if(address.locality != null){
                            gpsMainText.text = address.locality.toString()
                            textView_GPS_location.visibility = View.VISIBLE
                            textView_GPS_location.text = "Your location"
                        }else if(address.subLocality != null){
                            gpsMainText.text = address.subLocality.toString()
                            textView_GPS_location.visibility = View.VISIBLE
                            textView_GPS_location.text = "Your location"
                        }else{
                            gpsMainText.text = address.countryName.toString()
                            textView_GPS_location.visibility = View.VISIBLE
                            textView_GPS_location.text = "Your location"
                        }


                    }


                }, 3000)






            })

    }




    //Вывод уведомлений о новых сообщениях
    private fun showNotification() {
        database.child(NODE_MAIN_LIST).child(currentUid()!!).addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter { dataSnapshot ->
            mListItems = dataSnapshot.children.map { it.getChatModel() }

                        // 3 запрос
                val newNotifications = mListItems.filter { !it.read }
                val newNotificationsMap = newNotifications
                    .groupBy { it }
                    .mapValues { (_, values) -> values.size }


                fun setCount(text: TextView) {
                    val count = newNotificationsMap.size ?: 0
                    if (count == 0) {

                        text.visibility = View.GONE
                    } else {
                        text.visibility = View.VISIBLE
                        if(count >= 99){
                            text.text = " "
                        }
                        else{
                            text.text = " "
                        }


                    }
                }

                setCount(circle_new_message_main)


        })
    }




//Вывод уведомления о новых друзьях
    private fun showNotificationUser() {
        database.child(NODE_FOLLOWERS).child(currentUid()!!).addValueEventListener(Utils.ValueEventListenerAdapter { dataSnapshot ->
            val mListItemsUser = dataSnapshot.children.map { it.getUserModel() }
            mListItemsUser.forEach { model ->

                    database.child(NODE_USERS).child(
                        currentUid()!!).child("friends")
                        .child(model.uid).get()
                        .addOnSuccessListener {
                            if(!it.exists()){
                                circle_new_user_main.visibility = View.VISIBLE



                            }else{
                                circle_new_user_main.visibility = View.GONE
                            }
                        }




            }
        })
    }


    fun vibratePhone() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.EFFECT_TICK))
        } else {
            vibrator.vibrate(20)
        }
    }



}




