package com.ekenya.echama.ui.register


//import kotlinx.android.synthetic.main.fragment_register_step_two.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.ekenya.echama.R
import com.ekenya.echama.inc.AppConstants
import com.ekenya.echama.inc.AppInfo
import com.ekenya.echama.model.Country
import com.ekenya.echama.util.CustomProgressBar
import com.ekenya.echama.util.ToastnDialog
import com.ekenya.echama.viewModel.MainViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import timber.log.Timber


/**
 * A simple [Fragment] subclass.
 */
class RegisterStepTwo : Fragment() {
   // lateinit var mainViewModel: MainViewModel
   // lateinit var spnNationality:Spinner
   var countryList:List<Country> = ArrayList<Country>()
    lateinit var tlNationality: TextInputEditText
    lateinit var tlPhoneNumber: TextInputLayout
    lateinit var tlIdNumber: TextInputLayout
//  lateinit var etSelectRegion:EditText
//  lateinit var edtNationality:EditText
    lateinit var etPhoneNumber:EditText
    lateinit var etIdNumber:EditText
    lateinit var regionsList:String
    lateinit var bundle: Bundle
    lateinit var bundleNationality:Bundle
    lateinit var btnContinue:Button
//  lateinit var tlSelectDepartment:EditText
//  lateinit var pbRegisterTwo:ProgressBar
    lateinit var tvCountryCode:TextView
    lateinit var tvLogin:TextView
    lateinit var ivCountryFlag: ImageView
    lateinit var mPhoneNumber: String
    private lateinit var register2VM:MainViewModel  // viewModels<MainViewModel>()
    //lateinit var val accountAdapter:ArrayAdapter<String>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        register2VM = ViewModelProvider(this).get(MainViewModel::class.java)
        // Inflate the layout for this fragment
        // _binding = FragmentRegisterStepTwoBinding.inflate(inflater,container,false)
        val view:View= inflater.inflate(R.layout.fragment_register_step_two, container, false)

        val ivBack:ImageView = view.findViewById(R.id.ivBack)
        tlNationality=view.findViewById(R.id.tlNationality)
        tlPhoneNumber=view.findViewById(R.id.tlPhoneNumber)
        tlIdNumber=view.findViewById(R.id.tlIdNumber)
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber)
        etIdNumber = view.findViewById(R.id.etIdNumber)
        tvCountryCode=view.findViewById(R.id.tvCountryCode)
        ivCountryFlag = view.findViewById(R.id.ivCountryFlag)
        btnContinue = view.findViewById(R.id.btnContinue)
        tvLogin = view.findViewById(R.id.tvLogin)
        bundleNationality = Bundle()

        ivBack.setOnClickListener {
            // Timber.v("bundle %s", bundleNationality.toString())
                findNavController().navigate(R.id.nav_register)
        }
        arguments?.getString("user")?.let {

            register2VM.currentUser = register2VM.currentUser.fromJson(it)
        }

        tvLogin.setOnClickListener {
            // Timber.v("bundle %s", bundleNationality.toString())
                findNavController().navigate(R.id.nav_login)
        }

       tlNationality.setOnClickListener {
            Timber.v("bundle %s", bundleNationality.getString("countryStr").toString())
            if (bundleNationality.getString("countryStr") != null) {
                findNavController().navigate(R.id.nav_selectnationality, bundleNationality)
            }
        }


        //TODO add id and department, countycode

//        etPhoneNumber.setText("719124626")
//        etIdNumber.setText("280280280")


        btnContinue.setOnClickListener {
            if (validatedFields()) {

            //TODO VALIDATE ALL THIS DATA
            val registerJson = HashMap<String, String>()
            register2VM.currentUser.firstname?.let { it1 -> registerJson.put("firstname", it1) }
            register2VM.currentUser.othernames?.let { it1 -> registerJson.put("othernames", it1) }
            register2VM.currentUser.gender?.let { it1 -> registerJson.put("gender", it1) }
            register2VM.currentUser.dateofbirth?.let { it1 -> registerJson.put("dateofbirth", it1) }
            register2VM.currentUser.email?.let { it1 -> registerJson.put("email", it1) }
            register2VM.currentUser.phonenumber =
                tvCountryCode.text.toString().replace("+", "") + etPhoneNumber.text.toString()
                    .trim()//?.let { it1 -> registerJson.put("email", it1) }
            register2VM.currentUser.identification = etIdNumber.text.toString().trim()
            register2VM.currentUser.nationality = nationalityName
                register2VM.currentUser.phonenumber =  tvCountryCode.text.toString().replace("+", "") + etPhoneNumber.text.toString()
                    .trim()

            registerJson.put("phonenumber",
                tvCountryCode.text.toString().replace("+", "") + etPhoneNumber.text.toString()
                    .trim()
            )
            registerJson.put("identification", etIdNumber.text.toString().trim())
            registerJson.put("nationality", nationalityName)

            Timber.v("These is the request: %s", registerJson.toString())
            CustomProgressBar.show(this.requireContext())
            register2VM.registerMember(registerJson)
//                val bundle = Bundle()
//                bundle.putString("user",register2VM.currentUser.toJson())
//                findNavController().navigate(R.id.nav_otp_Verification,bundle)
        }
        }
        initData()
       // val view = root
        return  view

    }
    private fun validatedFields(): Boolean {
        var isValid = true
        if (etPhoneNumber.text.toString().isNullOrEmpty()) {
            tlPhoneNumber.error = getString(R.string.error_no_phone_number)
            isValid = false
        }
        if (etIdNumber.text.toString().isNullOrEmpty()) {
            tlIdNumber.setError(getString(R.string.error_no_id_number))
            isValid = false
        }
        return isValid
    }


     fun initData() {
         register2VM.myApiResponseLD.observe(viewLifecycleOwner, Observer {
             Timber.v("myApiResponseLD ${it.requestName} ${it.code} ${it.message}")
            CustomProgressBar.hide()
             if(it.code == 200){
                 if(it.requestName.contentEquals("getCountriesRequest")){
                     register2VM.getCountries()
                     val type = Types.newParameterizedType(MutableList::class.java, Country::class.java)
                     val adapterRegion: JsonAdapter<List<Country>> = AppInfo.moshi.adapter(type)

                     if(countryList.isEmpty()){
                         countryList = it.responseObj as List<Country>
                         Timber.v("countryList %s",countryList.size)
                         populateCountries(countryList)
                     }
                 }
                 if(it.requestName.contentEquals("registerUserRequest")){
                     val bundle = Bundle()
                     bundle.putString("user",register2VM.currentUser.toJson())
                     findNavController().navigate(R.id.nav_otp_Verification,bundle)
                     if(it.message.isNotEmpty()){
                         ToastnDialog.toastSuccess(this.requireContext(),it.message+" success")
                     }
                 }
             }else{
                 if(it.message.isNotEmpty()) {
                     ToastnDialog.toastError(this.requireContext(), it.message)
                 }
             }
         })


        register2VM.getCountries().observe(viewLifecycleOwner, Observer {countries ->
            populateCountries(countries)
        })


    }

    private fun populateCountries(countries: List<Country>) {

        countryList =     countries.filter { it.countryCode.contentEquals("KE")
                || it.countryCode.contentEquals("TZ")
                || it.countryCode.contentEquals("UG")
        }
        Timber.v("getNationalities  ${countries.size}")
        //  countryList = countries

            Timber.v("getNationalities  ${countries.size}")
            val type = Types.newParameterizedType(MutableList::class.java, Country::class.java)
            val adapterRegion: JsonAdapter<List<Country>> = AppInfo.moshi.adapter(type)

            var  countryStr = adapterRegion.toJson(countryList)
            bundleNationality.putString("countryStr",countryStr)
            Timber.v(countryStr)

    }



    companion object{
        var regionObject: RegionCallback? =null
        var region:String = ""
        var departmentName:String = ""
        var nationalityName:String = "Kenya"
        var countrycode:String="KE"
        var dialcode:String="+254"

    }

    override fun onResume() {
        super.onResume()
//        etSelectRegion.setText(region)
//        tlSelectDepartment.setText(departmentName)
        tlNationality.setText(nationalityName)
        tvCountryCode.text = dialcode

        Glide.with(this.requireContext())
            .asBitmap()
            .load(AppConstants.base_url+"/chama/mobile/req/countries/"+countrycode)
            .into(ivCountryFlag)

    }
}
