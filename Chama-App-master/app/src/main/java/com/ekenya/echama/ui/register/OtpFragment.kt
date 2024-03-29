package com.ekenya.echama.ui.register


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ekenya.echama.R
import com.ekenya.echama.util.CustomProgressBar
import com.ekenya.echama.util.SharedPrefenceUtil
import com.ekenya.echama.util.ToastnDialog
import com.ekenya.echama.viewModel.MainViewModel
import com.google.android.material.button.MaterialButton
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 */
class OtpFragment : Fragment() {
    private var input_one_EditText2: ImageView? = null
    private var input_two_EditText2: ImageView? = null
    private var input_three_EditText2: ImageView? = null
    private var input_four_EditText2: ImageView? = null
    lateinit  var txtDesc: TextView
    lateinit var delete_pin_pad:ImageView
    private var mConfirmPin: String? = null
  //  lateinit var mainViewModel: MainViewModel
    //lateinit var resources: Resources
    lateinit var btn_1: MaterialButton
    private  var btn_2:MaterialButton? = null
    private  var btn_3:MaterialButton? = null
    private  var btn_4:MaterialButton? = null
    private  var btn_5:MaterialButton? = null
    private  var btn_6:MaterialButton? = null
    lateinit  var btn_7:MaterialButton
    private  var btn_8:MaterialButton? = null
    private  var btn_9:MaterialButton? = null
    private  var btn_0:MaterialButton? = null
    private var one1: String? = null
    /**
     * The Two.
     */
    private var two2: String? = null
    /**
     * The Three.
     */
    private var three3: String? = null
    /**
     * The Four.
     */
    private var four4: String? = null
    var btn_ok: ImageView? = null
    lateinit  var otpMainVM:MainViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        otpMainVM = ViewModelProvider(this).get(MainViewModel::class.java)
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_otp, container, false) as View
       // mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        input_one_EditText2 = view.findViewById(R.id.input_one)
        input_two_EditText2 = view.findViewById(R.id.input_two)
        input_three_EditText2 = view.findViewById(R.id.input_three)
        input_four_EditText2 = view.findViewById(R.id.input_four)
        delete_pin_pad = view.findViewById(R.id.delete_pin_pad)
        txtDesc = view.findViewById(R.id.txtDesc)
        btn_ok = view.findViewById(R.id.pin_pad_ok)
        //////------------------/////////
        //////------------------/////////
        btn_1 = view.findViewById(R.id.btn_number_one)
        btn_2 = view.findViewById(R.id.btn_number_two)
        btn_3 = view.findViewById(R.id.btn_number_three)
        btn_4 = view.findViewById(R.id.btn_number_four)
        btn_5 = view.findViewById(R.id.btn_number_five)
        btn_6 = view.findViewById(R.id.btn_number_six)
        btn_7 = view.findViewById(R.id.btn_number_seven)
        btn_8 = view.findViewById(R.id.btn_number_eight)
        btn_9 = view.findViewById(R.id.btn_number_nine)
        btn_0 = view.findViewById(R.id.btn_number_zero)

        btn_1.setOnClickListener { controlPinPad2("1") }
        btn_2?.setOnClickListener{controlPinPad2("2")}
        btn_3?.setOnClickListener{ controlPinPad2("3")}
        btn_4?.setOnClickListener{ controlPinPad2("4")}
        btn_5?.setOnClickListener { controlPinPad2("5") }
        btn_6?.setOnClickListener{controlPinPad2("6")}

        btn_7.setOnClickListener { controlPinPad2("7") }
        btn_8?.setOnClickListener{controlPinPad2("8")}
        btn_9?.setOnClickListener{ controlPinPad2("9")}
        btn_0?.setOnClickListener{ controlPinPad2("0")}
        delete_pin_pad.setOnClickListener { deletePinEntry() }

        val imgOkay =  view.findViewById(R.id.pin_pad_ok) as ImageView
        val phoneNumber = view

        arguments?.getString("user")?.let {
            otpMainVM.currentUser = otpMainVM.currentUser.fromJson(it)
        }

        txtDesc.text = getString(R.string.please_enter_otp)

        imgOkay.setOnClickListener {
            //findNavController().navigate(R.id.nav_changepin)
            val otpJson = HashMap<String, Any>()
            otpMainVM.currentUser.otp = mConfirmPin.toString()
            otpJson.put("otp",  mConfirmPin?.toInt() as Any)
            otpJson.put("phonenumber", otpMainVM.currentUser.phonenumber)
            CustomProgressBar.show(this.requireContext())
            otpMainVM.validateOtp(otpJson)
            }
        initData()

        return view
    }

    private fun initData() {
        otpMainVM.myApiResponseLD.observe(viewLifecycleOwner, Observer {
            Timber.v("myApiResponseLD  ${it.requestName} ${it.message}")
            CustomProgressBar.hide()
            if(it.code == 200){
                if(it.requestName.contentEquals("verifyOtpRequest")){
                    val bundle = Bundle()
                    bundle.putString("user",otpMainVM.currentUser.toJson())
                    findNavController().navigate(R.id.nav_changepin,bundle)

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

    fun verifyOtp(otpJson: HashMap<String, String>) {

//            .observe(this, Observer {
//            when(it){
//                is ViewModelWrapper.error -> Log.e("otpError","Error"+it.error)
//                is ViewModelWrapper.response -> handleResponse(it.value)
//            }
//        })
    }

    private fun handleResponse(value: String) {
        val bundle = Bundle()
        bundle.putString("otp",mConfirmPin)
        SharedPrefenceUtil().setOtp(mConfirmPin!!,context!!)
        findNavController().navigate(R.id.nav_changepin)
    }

    /**
     * pinpad control
     * @param entry the entry
     */
    private fun controlPinPad2(entry: String) {
        if (one1 == null) {
            input_one_EditText2!!.setImageResource(R.mipmap.ic_star)
            one1 = entry
        } else if (two2 == null) {
            input_two_EditText2!!.setImageResource(R.mipmap.ic_star)
            two2 = entry
        } else if (three3 == null) {
            input_three_EditText2!!.setImageResource(R.mipmap.ic_star)
            three3 = entry
        } else if (four4 == null) {
            input_four_EditText2!!.setImageResource(R.mipmap.ic_star)
            four4 = entry
        }
        if (mConfirmPin == null) {
            mConfirmPin = entry
        } else {
            mConfirmPin = mConfirmPin + entry
        }
        if (mConfirmPin!!.length == 4) {
            btn_ok!!.isEnabled = true
        }
    }

    private fun deletePinEntry() {
        if (mConfirmPin != null && mConfirmPin!!.length > 0) {
            mConfirmPin = mConfirmPin!!.substring(0, mConfirmPin!!.length - 1)
        }
        if (four4 != null) {
            input_four_EditText2!!.setImageResource(R.mipmap.ic_underscore)
            four4 = null
        } else if (three3 != null) {
            input_three_EditText2!!.setImageResource(R.mipmap.ic_underscore)
            three3 = null
        } else if (two2 != null) {
            input_two_EditText2!!.setImageResource(R.mipmap.ic_underscore)
            two2 = null
        } else if (one1 != null) {
            input_one_EditText2!!.setImageResource(R.mipmap.ic_underscore)
            one1 = null
        }

    }
}
