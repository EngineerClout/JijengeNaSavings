package com.ekenya.echama.ui.navigationDrawer

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.ekenya.echama.R
import com.ekenya.echama.databinding.FragmentUserupdateBinding
import com.ekenya.echama.inc.AppConstants
import com.ekenya.echama.model.Transaction
import com.ekenya.echama.model.User
import com.ekenya.echama.util.CustomProgressBar
import com.ekenya.echama.util.DataUtil
import com.ekenya.echama.util.ToastnDialog
import com.ekenya.echama.util.getHashedPin256
import com.ekenya.echama.util.successAlert
import com.ekenya.echama.viewModel.MainViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import timber.log.Timber
import java.util.regex.Pattern


class UserFragment : Fragment() {


    private var _binding: FragmentUserupdateBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    lateinit var btnDeposit:MaterialButton
    var activityList:ArrayList<Transaction> = ArrayList()
    lateinit var userVM:MainViewModel
    var user:User? = null
    var originaluser:User? = null
    var page:Int = 1
    var size:Int = 10

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        R.layout.fragment_userupdate
        userVM = ViewModelProvider(this).get(MainViewModel::class.java)
       // groupViewModel.getGrpInvites(jsonInvitesDetails)
        _binding = FragmentUserupdateBinding.inflate(inflater, container, false)
        val view = binding.root
        initData()
        initButton()
        initImageView()

        return view
    }

    private fun initImageView() {
        Glide.with(this)
            .asBitmap()
            .load(AppConstants.base_url+"/chama/mobile/req/countries/"+"KE")
            .into( binding.imgUserProfilePhoto)

        binding.imgUserProfilePhoto.setOnClickListener {
             //Todo show dialogue for user to select or take photo
        }

    }

    private fun initButton() {
        binding.btnUpdate.setOnClickListener{
            if(validInfo()){
                val userData = HashMap<String, String>()
                userData["email"] =  user?.email.toString()
                userData["gender"] = user?.gender.toString()
                userData["firstname"] = user?.firstname.toString()
                userData["nationality"] = user?.nationality.toString()
                userData["identification"] = user?.identification.toString()
                userData["phonenumber"] = user?.phonenumber.toString()
                userData["dateofbirth"] = user?.dateofbirth.toString()
                userData["othernames"] = user?.othernames.toString()
                CustomProgressBar.show(this.requireContext(),"Updating")
                userVM.updateUserDetails(userData)
            }else{
                ToastnDialog.toastError(this.requireContext(),"Kindly check if all user data is correct")
            }
        }

        binding.btnChangePassword.setOnClickListener{
            changePassDialogue()
        }
        binding.btnDeleteAccount.setOnClickListener {
            showDeleteDialogue()
        }

    }
    fun showDeleteDialogue(){
        val builder = AlertDialog.Builder(this.requireContext())
        //set title for alert dialog
        builder.setTitle("Delete")
        //set message for alert dialog
        builder.setMessage("Are you sure you want to delete your account ")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        //performing positive action
        builder.setPositiveButton("Yes"){dialogInterface, which ->
            userVM.deleteUser()
            dialogInterface.dismiss()
        }
        //performing cancel action
//        builder.setNeutralButton("Cancel"){dialogInterface , which ->
//            Toast.makeText(applicationContext,"clicked cancel\n operation cancel",Toast.LENGTH_LONG).show()
//        }
        //performing negative action
        builder.setNegativeButton("No"){dialogInterface, which ->
            dialogInterface.dismiss()
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    fun changePassDialogue(title:String = "",message:String = ""){
        val builder = AlertDialog.Builder(this.requireContext())
        val mDialogView = LayoutInflater.from(this.requireContext()).inflate(R.layout.dialogue_layout, null)
        builder.setView(mDialogView)
        var tvDialogTitle: TextView =  mDialogView.findViewById(R.id.tvDialogTitle)
        var tvDialogMessage: TextView =  mDialogView.findViewById(R.id.tvDialogMessage)
        var tlPin: TextInputLayout =  mDialogView.findViewById(R.id.tlPin)
        var etxtPin: EditText =  mDialogView.findViewById(R.id.etxtPin)
        var btnReject: MaterialButton =  mDialogView.findViewById(R.id.btnReject)
        var btnApprove: MaterialButton =  mDialogView.findViewById(R.id.btnApprove)

        tvDialogTitle.text = "Enter Pin"
        tvDialogMessage.text = "Enter your pin to update your password"
        btnReject.text = "Cancel"
        btnApprove.text = "Proceed"

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()

        btnReject.setOnClickListener {
            alertDialog.dismiss()
        }
        btnApprove.setOnClickListener {
            if(etxtPin.text.isEmpty() || etxtPin.text.length < 4){
                tlPin.error = "Invalid Pin"
            }
            val ePinStr = DataUtil.getHashedPin256(etxtPin.text.toString())
            //Todo confirm with user encrypted pin
            if (etxtPin.text.toString().getHashedPin256().contentEquals(userVM.currentUser.pass.toString().getHashedPin256())
                || etxtPin.text.toString().contentEquals("1234") //Todo remove this line on production
            ){
                userVM.currentUser.phonenumber = user?.phonenumber.toString()
                userVM.currentUser.identification = user?.identification.toString()
                CustomProgressBar.show(this.requireContext(),"Sending OTP")
                userVM.resetPassword()
            }else{
                tlPin.error = "Incorrect Pin. 2 trials remaining"
            }
            alertDialog.dismiss()

        }
        // Set other dialog properties
        alertDialog.setCancelable(true)
        alertDialog.show()
    }

    private fun validInfo(): Boolean {
        binding.etSurname.doOnTextChanged { text, start, count, after ->
            if(text.toString().isEmpty()){
                binding.tlSurname.error = "Enter your surname"
            }else{
                user?.firstname = text.toString()
            }
            binding.btnUpdate.isEnabled = !text.toString().isEmpty()
        }
        binding.etOtherNames.doOnTextChanged { text, start, count, after ->
            Timber.v("binding.etOtherNames "+text.toString())
            if(text.toString().isEmpty()){
                binding.tlOtherNames.error = "Enter your name"
            }else{
                user?.othernames = text.toString()
            }

            binding.btnUpdate.isEnabled = !text.toString().isEmpty()
        }
        binding.etEmail.doOnTextChanged { text, start, count, after ->
            Timber.v("binding.etOtherNames "+text.toString())
            val pattern: Pattern = Patterns.EMAIL_ADDRESS

            if(!pattern.matcher(text).matches()){
                binding.tlEmail.error = "Invalid email"
            }else{
                user?.email = text.toString()
            }

            binding.btnUpdate.isEnabled = !text.toString().isEmpty()
        }
        binding.etPhoneNumber.doOnTextChanged { text, start, count, after ->
            Timber.v("binding.etOtherNames "+text.toString())
            if(text.toString().isEmpty()){
                binding.tlPhoneNumber.error = "Enter valid phone number"
            }else{
                user?.phonenumber = text.toString()
            }
            binding.btnUpdate.isEnabled = !text.toString().isEmpty()
        }


       return true
    }

    private fun initData() {
      //  CustomProgressBar.show(this.requireContext(),"Loading data")
        userVM.getUserDetails()
        userVM.userLD.observe(viewLifecycleOwner, Observer { users ->
            if(users.isNotEmpty()){
                populateData(users[0])
            }
        })

        userVM.myApiResponseLD.observe(viewLifecycleOwner, Observer {
            CustomProgressBar.hide()
            Timber.v("myApiResponseLD ${it.requestName} ${it.code} ${it.message}")
            if(it.code == 200){
                if(it.requestName.contentEquals("resetPassRequest")){
                    val bundle = Bundle()
                    bundle.putString("user",originaluser?.toJson())
                    findNavController().navigate(R.id.nav_otp_Verification)
                    if(it.message.isNotEmpty()){
                        ToastnDialog.toastSuccess(this.requireContext(),it.message+" success")
                    }
                }
                if(it.requestName.contentEquals("getUserDetailsRequest")){
                   // populateData(it.responseObj as User)
                }
                if(it.requestName.contentEquals("deleteUserRequest")){
                    findNavController().navigate(R.id.nav_user_login)
                }
                if(it.requestName.contentEquals("updateUserDetailsRequest")){
                       successAlert("Success",it.message,false)
                }
            }else{
                if(it.message.isNotEmpty()) {
                    ToastnDialog.toastError(this.requireContext(), it.message)
                }
            }

        })
    }

    private fun populateData(puser: User) {
        user = puser
        originaluser = puser
        binding.etDateOfBirth.setText( user?.dateofbirth.toString())
        binding.etOtherNames.setText( user?.othernames.toString())
        binding.etSurname.setText( user?.firstname.toString())
        binding.etGender.setText( user?.gender.toString())
        binding.etEmail.setText( user?.email.toString())
        binding.etNationality.setText( user?.nationality.toString())
        binding.etPhoneNumber.setText( user?.phonenumber.toString())
        binding.etxtIdentity.setText( user?.identification.toString())
        validInfo()
    }

//    override fun onDestroyView() {
//        _binding = null
//    }

}