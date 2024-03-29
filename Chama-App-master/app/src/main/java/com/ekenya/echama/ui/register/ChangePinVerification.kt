package com.ekenya.echama.ui.register


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ekenya.echama.R
import com.ekenya.echama.databinding.FragmentChangePinVerificationBinding
import com.ekenya.echama.util.CustomProgressBar
import com.ekenya.echama.util.ToastnDialog
import com.ekenya.echama.viewModel.MainViewModel
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 */
class ChangePinVerification : Fragment() {
    //lateinit var  etPassword:EditText
    //lateinit var etconfirmpassword:EditText
   // lateinit var mainViewmodel:MainViewModel
    private lateinit var changePinVM:MainViewModel  // viewModels<MainViewModel>()
    private var _binding: FragmentChangePinVerificationBinding? = null
    private val binding get() = _binding!!
    var actionFrom = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        R.layout.fragment_change_pin_verification
        changePinVM = ViewModelProvider(this).get(MainViewModel::class.java)
        _binding = FragmentChangePinVerificationBinding.inflate(inflater, container, false)
        val view = binding.root
       // val root = inflater.inflate(R.layout.fragment_change_pin_verification, container, false) as View
      //  val ivBack = root.findViewById(R.id.ivBack) as ImageView
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
           // findNavController().navigate(R.id.nav_otp_Verification)
        }
      //  etPassword = root.findViewById(R.id.etPassword)
      //  etconfirmpassword=root.findViewById(R.id.etConfirmPassword)
       // mainViewmodel = ViewModelProvider(this).get(MainViewModel::class.java)
         arguments?.getString("phonenumber")?.let {
             changePinVM.currentUser.phonenumber = it
             binding.txtDesc.text = "Enter your new pin"
         }
        arguments?.getString("user")?.let {
            changePinVM.currentUser = changePinVM.currentUser.fromJson(it)
        }

                //  val btnContinue = root.findViewById(R.id.btncontinue) as Button
      binding.btncontinue.setOnClickListener {
           if(validatedData())
           {
               val password = binding.etPassword.text.toString()

               val jsonVerification = HashMap<String, String>()
              //jsonVerification.put("id",id.toString())

               jsonVerification.put("otp", changePinVM.currentUser.otp.toString())//otp.toString())
               jsonVerification.put("password",password)
               jsonVerification.put("phonenumber", changePinVM.currentUser.phonenumber) //phoneNumber.toString())
               changePinVM.changePassword(jsonVerification)
//               GlobalScope.launch(Dispatchers.Main) {
//                  changeMemberPassword(jsonVerification)
//               }
           }
       }
        arguments?.getString("from")?.let {
            actionFrom = it
        }
        initTextView()
        initData()
        return view
    }

    private fun initTextView() {
           if(actionFrom.contains("profile")){
              binding.txtDesc.text = getString(R.string.enter_new_password)
           }
    }

    private fun initData() {
            changePinVM.myApiResponseLD.observe(viewLifecycleOwner, Observer {
                Timber.v("myApiResponseLD  ${it.requestName} ${it.message}")
               CustomProgressBar.hide()
                if(it.code == 200){

                    if(it.requestName.contentEquals("updatePassRequest")){
                        if(!actionFrom.contains("profile")){
                            findNavController().navigate(R.id.nav_login)
                        }
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
    }

    suspend fun changeMemberPassword(jsonVerification: HashMap<String, String>) {
       // mainViewmodel.changePassword(jsonVerification)
//            .observe(this, Observer {
//            when(it){
//                is ViewModelWrapper.error -> Log.e("apierror",it.error)
//                is ViewModelWrapper.response ->processResponse(it.value)
//            }
//        })

    }

    private fun processResponse(jsonData: String) {
       // val gson = GsonBuilder().create()
        //val regionList= gson.fromJson(jsonData, JsonElement::class.java)
        //val name = regionList.asJsonObject.get("id")
            // Log.e("passchange",name.toString())
        Toast.makeText(this.activity,"Password updated successfully",Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.nav_login)
    }

    private fun validatedData(): Boolean {
        if(binding.etPassword.text.toString().length < 4){
            binding.tlPassword.error = "Pin should be 4 characters"
            return false
        }
        if (!binding.etConfirmPassword.text.toString().equals(binding.etPassword.text.toString())){
            binding.tlConfirmPassword.error = "Password does not match"
            return false
        }

        return  true
    }


}
