package com.ekenya.echama.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ekenya.echama.R
import com.ekenya.echama.util.CustomProgressBar
import com.ekenya.echama.util.ToastnDialog
import com.ekenya.echama.viewModel.MainViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import timber.log.Timber


class ForgotPassword:Fragment() {
    lateinit var forgotPassVM: MainViewModel
    lateinit var tlPhoneNumber: TextInputLayout
    lateinit var tlIdNumber: TextInputLayout
    lateinit var etPhoneNumber: TextInputEditText
    lateinit var etIdNumber: TextInputEditText
    lateinit var btnReset: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        forgotPassVM =  ViewModelProvider(this).get(MainViewModel::class.java)
        // Inflate the layout for this fragment
        // _binding = FragmentRegisterStepTwoBinding.inflate(inflater,container,false)
        val view: View = inflater.inflate(R.layout.fragment_forgot_password, container, false)

        val ivBack: ImageView = view.findViewById(R.id.ivBack)

        tlPhoneNumber = view.findViewById(R.id.tlPhoneNumber)
        tlIdNumber = view.findViewById(R.id.tlPhoneNumber)
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber)
        etIdNumber = view.findViewById(R.id.etIdNumber)
        btnReset = view.findViewById(R.id.btnReset)

        ivBack.setOnClickListener {
            // Timber.v("bundle %s", bundleNationality.toString())
            findNavController().navigateUp()
        }


        btnReset.setOnClickListener {
            resetPassword()
        }
        initData()
        // val view = root
        return  view

    }

    private fun initData() {
        forgotPassVM.myApiResponseLD.observe(viewLifecycleOwner, Observer {
            Timber.v("myApiResponseLD ${it.requestName} ${it.code} ${it.message}")
            CustomProgressBar.hide()
            if(it.code == 200){
                if(it.requestName.contentEquals("resetPassRequest")){
                    val bundle = Bundle()
                    bundle.putString("user",forgotPassVM.currentUser.toJson())
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
    }

    private fun validatedFields(): Boolean {
        if(etPhoneNumber.text.toString().isEmpty()){
            tlPhoneNumber.error = "Enter your phone number to proceed"
            return false
        }
        if(etIdNumber.text.toString().isEmpty()){
            tlIdNumber.error = "Enter your national Id to proceed"
            return false
        }
        return true
    }

    private fun resetPassword() {
        if (validatedFields()) {
            forgotPassVM.currentUser.phonenumber = etPhoneNumber.text.toString().trim()
            forgotPassVM.currentUser.identification = etIdNumber.text.toString().trim()
            CustomProgressBar.show(this.requireContext())
            forgotPassVM.resetPassword()
        }

    }

}