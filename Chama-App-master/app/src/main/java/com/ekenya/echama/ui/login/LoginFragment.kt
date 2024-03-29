package com.ekenya.echama.ui.login


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ekenya.echama.MainActivity
import com.ekenya.echama.R
import com.ekenya.echama.util.CustomProgressBar
import com.ekenya.echama.util.ToastnDialog
import com.ekenya.echama.viewModel.MainViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : Fragment() {
lateinit var tlPassword:TextInputLayout
lateinit var tlPhoneEmail:TextInputLayout
lateinit var edtUsername:EditText
    lateinit var edtPassword:EditText
    lateinit var granttype:EditText
    lateinit var btnGo2SignUp: MaterialButton
    val mainViewModel by viewModels<MainViewModel>()
    var username = ""
    var password = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView:View = inflater.inflate(R.layout.fragment_login, container, false)
        edtUsername = rootView.findViewById(R.id.etEmailPhoneNumber)
        edtPassword = rootView.findViewById(R.id.etPassword)
        tlPhoneEmail = rootView.findViewById(R.id.tlPhoneEmail)
        tlPassword = rootView.findViewById(R.id.tlPassword)
        btnGo2SignUp = rootView.findViewById(R.id.btnGo2SignUp)
//      edtUsername.setText("254715702887") //254719124626
//      edtPassword.setText("1122")  //1122
//      granttype.setText("password")
        val btnLogin = rootView.findViewById(R.id.btnLogin) as Button
        val forgotPin:TextView = rootView.findViewById(R.id.tvForgotPassword1)
        btnLogin.setOnClickListener {
            if (validDetails()) {
                val jsonVerification = HashMap<String, String>()
                jsonVerification["username"] = username
                jsonVerification["password"] = password
                jsonVerification["grant_type"] = "password"
                CustomProgressBar.show(this.requireContext(),"Loading")
                mainViewModel.userlogin1(jsonVerification)
            }
        }
        initData()

        forgotPin.setOnClickListener {
            findNavController().navigate(R.id.nav_forgotpassword)
        }
        btnGo2SignUp.setOnClickListener {
            findNavController().navigate(R.id.nav_register)
//            findNavController().navigateUp()
        }
        return rootView
    }

    private fun initData() {
         mainViewModel.getCountries()

        mainViewModel.userLD.observe(viewLifecycleOwner, Observer { userList ->
           CustomProgressBar.hide()
            Timber.v("User list count %s",userList.size)
            if(userList.isNotEmpty()){
            //    findNavController().navigate(R.id.nav_home)
            }
        })

        mainViewModel.myApiResponseLD.observe(viewLifecycleOwner, Observer { myApiResponse ->
           CustomProgressBar.hide()
            if(myApiResponse.code == 200){
                if (myApiResponse.requestName.contentEquals("loginUserRequest")){
                    ToastnDialog.toastSuccess(this.requireContext(),"Success:"+myApiResponse.message)
                    val intent = Intent(this.requireContext(), MainActivity::class.java).apply {
                       // putExtra(EXTRA_MESSAGE, message)
                    }
                    //startActivity(intent)
                  // findNavController().navigate(R.id.nav_home)
                    mainViewModel.currentUser.pass = myApiResponse.responseObj.toString()
                   findNavController().navigate(R.id.nav_mygroup)
                }
            }else{
                Timber.v("Error: "+myApiResponse.requestName+" "+myApiResponse.message)
                ToastnDialog.toastError(this.context!!,"Error:"+myApiResponse.message)
            }
        })
    }

    private fun validDetails(): Boolean {
         username = edtUsername.text.toString()
         password = edtPassword.text.toString()
        if(username.isNullOrBlank()){
            tlPhoneEmail.error = "Invalid username"
            //ToastnDialog.toastError(this.requireContext(),"Invalid username")
            return false
        }
        if(password.isNullOrBlank()){
            tlPassword.error = "Invalid password"
            //ToastnDialog.toastError(this.requireContext(),"Invalid password")
            return false
        }

        return true
    }


}
