package com.ekenya.echama

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import com.bumptech.glide.Glide
import com.ekenya.echama.databinding.ActivityMainBinding
import com.ekenya.echama.inc.AppConstants
import com.ekenya.echama.util.DataUtil
import com.ekenya.echama.util.showInDevoplopmentDialogue
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import timber.log.Timber
import java.util.Locale
import com.ekenya.echama.viewModel.MainViewModel as ViewModelMainViewModel


class MainActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    NavController.OnDestinationChangedListener,

    BottomNavigationView.OnNavigationItemSelectedListener {

    private val mainViewModel by viewModels<ViewModelMainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
//         val mainViewModel by viewModels<ViewModelMainViewModel>()

        super.onCreate(savedInstanceState)
        initCheckRooted()
        binding = ActivityMainBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)
        mainVM = mainViewModel
        initToolbar()
        initDrawer()
        initData()
    }

    private  var notitificationMenuItem: MenuItem? = null
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var imgChamaLogo: ImageView
    private var currentDestination = ""

//val mainViewModel by viewModels<ViewModelMainViewModel>()
    private lateinit var binding: ActivityMainBinding

    private var revealed: Boolean = false

    companion object {
        lateinit var mainVM: ViewModelMainViewModel
    }
//    val mainViewModel by viewModels<ViewModelMainViewModel>()


    private fun initCheckRooted() {

        if (DataUtil.isRooted(this)) {
            Timber.v("Phone is rooted")
            finish()
            //showInDevelopmentDialogue("Phone is rooted")
        }else{
            Timber.v("Phone is not rooted")
            // showInDevoplopmentDialogue("Phone is real device")
        }
    }
    private fun getMyPermission(){
        mainViewModel.getMemberPermission()
    }

    private fun initDrawer() {
        //  val navView: NavigationView = findViewById(R.id.nav_view)
//        navController = findNavController(R.id.navHostFragment)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
//        NavigationUI.setupWithNavController(navigationView, navController)


        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_mygroup),binding.drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        // bottom_nav.setupWithNavController (navController)
        navController.addOnDestinationChangedListener(this)
        binding.navView.setNavigationItemSelectedListener(this)
        binding.mainConstraint.bottomNav.setOnNavigationItemSelectedListener(this)
        //  navViewng.setupWithNavController(navController)
    }

    private fun initToolbar() {
        setSupportActionBar( binding.mainConstraint.toolbar )
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        imgChamaLogo =   binding.mainConstraint.toolbar.findViewById(R.id.imgChamaLogo)
        title = "Welcome UserName"
    }


    private fun initData() {
        var userPhoto: ImageView? = findViewById(R.id.imgUserPhoto)

        Glide.with(this)
            .asBitmap()
            .load(AppConstants.base_url+"/chama/mobile/req/countries/"+"KE")
            .into( binding.navView.getHeaderView(0).findViewById(R.id.imgUserPhoto))

        binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.imgUserPhoto).setOnClickListener {
            findNavController(R.id.navHostFragment).navigate(R.id.nav_userupdate)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        mainViewModel.getUserDetails()
        mainViewModel.myApiResponseLD.observe(this) {

            Timber.v(it.requestName)

        }
        mainViewModel.userLD.observe(this) { users ->
            Timber.v("user %s", users.size)
            if (users.isNotEmpty()) {
                mainViewModel.currentUser = users[0]
                binding.navView.getHeaderView(0)
                    .findViewById<TextView>(R.id.tvNavHeaderUsername).text = users[0].firstname
                binding.navView.getHeaderView(0)
                    .findViewById<TextView>(R.id.tvNavHeaderPhoneNumber).text = users[0].phonenumber
                //Timber.v("usertoken "+users[0].access_token)
                //DataUtil.authToken = users[0].access_token
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        notitificationMenuItem = menu.findItem(R.id.action_notification)
        return true
    }


    /**
     * listens to menu item click event
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Timber.v("onOptionsItemSelected $currentDestination")
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if(currentDestination.contentEquals("add account")
            || currentDestination.contentEquals("group accounts")
            || currentDestination.contentEquals("create contribution")
        ){
            return false
        }



        return when (item.itemId) {
            android.R.id.home -> {
                if(currentDestination.contentEquals("home")
                    || currentDestination.contentEquals("my groups")
                    || currentDestination.contentEquals("recent activities")
                    || currentDestination.contentEquals("my wallet")){
                    binding.drawerLayout.openDrawer(GravityCompat.START)
                    title = ""
                }else{
                    navController.navigateUp()
                }

                //
                true
            }
            R.id.action_notification -> {
                showInDevoplopmentDialogue()
                true
            }
            R.id.action_logout -> {
                logOut()
                true
            }
            else -> {
                return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
                //super.onOptionsItemSelected(item)
            }
        }
    }

//    override fun onSupportNavigateUp(): Boolean {
//        onBackPressed()
//        return true
//    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Timber.v("onNavigationItemSelected $currentDestination")
        when (item.itemId) {
            R.id.nav_home -> {
                if(!(currentDestination.contentEquals("home")
                            || currentDestination.contentEquals("my groups")
                            || currentDestination.contentEquals("recent activities")
                            || currentDestination.contentEquals("my wallet"))
                ){
                    findNavController(R.id.navHostFragment).navigate(R.id.nav_mygroup)
                }
            }
            R.id.nav_helpfaq -> {
                findNavController(R.id.navHostFragment).navigate(R.id.nav_helpfaq)
            }
            R.id.nav_settings -> {
                findNavController(R.id.navHostFragment).navigate(R.id.nav_settings)
            }
            R.id.nav_share -> {
                shareApp()
            }
            R.id.nav_privatepolicy -> {
                findNavController(R.id.navHostFragment).navigate(R.id.nav_private_policy)
            }
            R.id.nav_about_us -> {
                findNavController(R.id.navHostFragment).navigate(R.id.nav_aboutus)
            }
            R.id.contact_us -> {
                findNavController(R.id.navHostFragment).navigate(R.id.nav_contactus)
            }
            R.id.log_out -> {
                logOut()
            }
            //BOTTOM NAVIGATION MENU
            R.id.my_group ->{
                findNavController(R.id.navHostFragment).navigate(R.id.nav_mygroup)
            }
            R.id.my_wallet ->{
                findNavController(R.id.navHostFragment).navigate(R.id.nav_mywallet)
            }
            R.id.action_more ->{
                showInDevoplopmentDialogue()
            }

        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    private fun shareApp(){
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Echama is a great app. Download this application  https://www.ekenya.co.ke/")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun logOut(){
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle("Log Out")
        //set message for alert dialog
        builder.setMessage("Do you want to log out ")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton("Yes"){dialogInterface, _ ->
            mainViewModel.logout()
            findNavController(R.id.navHostFragment).navigate(R.id.nav_user_login)
            dialogInterface.dismiss()
        }
        //performing cancel action
//        builder.setNeutralButton("Cancel"){dialogInterface , which ->
//            Toast.makeText(applicationContext,"clicked cancel\n operation cancel",Toast.LENGTH_LONG).show()
//        }
        //performing negative action
        builder.setNegativeButton("No"){dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        currentDestination =  destination.label.toString().toLowerCase(Locale.ROOT)
        Timber.v(destination.navigatorName)
        Timber.v(destination.id.toString())
        Timber.v( destination.label.toString())
        val set = ConstraintSet()
        set.clone( binding.mainConstraint.mainConstraint)
        // binding.mainConstraint.mainConstraint2
        if(destination.label.toString().contentEquals("Group Details")) {
            mainViewModel.getMemberPermission()
        }

        if(
            destination.label.toString().contentEquals("Welcome")
            || destination.label.toString().contentEquals("Onboarding")
            || destination.label.toString().contentEquals("Register")
            || destination.label.toString().contentEquals("Login")
            || destination.label.toString().contentEquals("Enter otp")
            //|| destination.label.toString().contentEquals("Change Pin")
            || destination.label.toString().contentEquals("Forgot Pin")
            || destination.label.toString().contentEquals("Signup")
        ){
            set.connect( binding.mainConstraint.contentConstraint.id, ConstraintSet.TOP, binding.mainConstraint.mainConstraint.id, ConstraintSet.TOP, 0)
            set.applyTo( binding.mainConstraint.mainConstraint)
            Timber.v("Main activity GONE 0")
            binding.mainConstraint.bottomNav.visibility = View.GONE
            binding.mainConstraint. appBarLayout.visibility = View.GONE
            binding.mainConstraint.toolbar.visibility = View.GONE
        } else {
            set.connect( binding.mainConstraint.contentConstraint.id, ConstraintSet.TOP,  binding.mainConstraint.mainConstraint.id, ConstraintSet.TOP, resources.getDimension(R.dimen.app_bar_size).toInt())
            set.connect(binding.mainConstraint.contentConstraint.id, ConstraintSet.BOTTOM, binding.mainConstraint.bottomNav.id, ConstraintSet.TOP, 0)
            set.applyTo( binding.mainConstraint.mainConstraint)
            Timber.v("Main activity VISIBLE 60")

            binding.mainConstraint.bottomNav.visibility = View.VISIBLE
            binding.mainConstraint.appBarLayout.visibility = View.VISIBLE
            binding.mainConstraint.toolbar.visibility = View.VISIBLE
        }
        if(
            destination.label.toString().contentEquals("Home")
            || destination.label.toString().contentEquals("Recent Activities")
            || destination.label.toString().contentEquals("My Wallet")
            || destination.label.toString().contains("My Groups")
        ){
            supportActionBar?.setDisplayShowTitleEnabled(false)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.drawer_icon)
            binding.mainConstraint.appBarLayout.visibility = View.VISIBLE
            binding.mainConstraint.toolbar.visibility = View.VISIBLE
            binding.mainConstraint.bottomNav.visibility = View.VISIBLE
            imgChamaLogo.visibility = View.VISIBLE
            // notitificationMenuItem?.isVisible = true
            //set.connect(contentConstraint.id, ConstraintSet.TOP, mainConstraint.id, ConstraintSet.TOP, 60);
            //set.connect(contentConstraint.id, ConstraintSet.BOTTOM, bottom_nav.id, ConstraintSet.TOP, 0);
        }else{
            supportActionBar?.setDisplayShowTitleEnabled(true)
            imgChamaLogo.visibility = View.GONE
            binding.mainConstraint.bottomNav.visibility = View.GONE
            notitificationMenuItem?.isVisible = false
        }
    }

//    override fun onRequestPermissionsResult(requestCode: Int,
//                                            permissions: Array<String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    }

}
