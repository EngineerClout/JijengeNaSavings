package com.ekenya.echama.ui.register


//import kotlinx.android.synthetic.main.fragment_register.*
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.DialogInterface
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ekenya.echama.R
import com.ekenya.echama.model.User
import com.ekenya.echama.util.ToastnDialog
import com.ekenya.echama.viewModel.MainViewModel
import com.google.android.material.textfield.TextInputLayout
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Objects
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass.
 */
class RegisterFragment : Fragment() {
     private lateinit var tvLogin: TextView

    private lateinit var etSurname:EditText
    lateinit var etOtherNames:EditText
    lateinit var tlGender:TextInputLayout
    lateinit var etGender:EditText
    lateinit var etDateOfBirth:EditText
    lateinit var tlEmail:TextInputLayout
    lateinit var tlDateOfBirth:TextInputLayout
    lateinit var tlSurname:TextInputLayout
    lateinit var tlOtherNames:TextInputLayout
    lateinit var etEmail:EditText
    lateinit var registerVM:MainViewModel

//    val c = Calendar.getInstance()
//    val year = c.get(Calendar.YEAR)
//    val month = c.get(Calendar.MONTH)
//    val day = c.get(Calendar.DAY_OF_MONTH)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        registerVM = ViewModelProvider(this).get(MainViewModel::class.java)
        val view:View = inflater.inflate(R.layout.fragment_register, container, false)
        val btnContinue = view.findViewById(R.id.btnContinue) as Button
      //  _binding = FragmentRegisterBinding.inflate(inflater,container,false)

//        val btnLogin = view.findViewById(R.id.tvForgotPassword) as TextView
       // val myViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        val ivBack: ImageView = view.findViewById(R.id.ivBack)
        etSurname = view.findViewById(R.id.etSurname)
        etOtherNames=view.findViewById(R.id.etOtherNames)
        etGender=view.findViewById(R.id.etGender)
        etDateOfBirth=view.findViewById(R.id.etDateOfBirth)
        tlEmail = view.findViewById(R.id.tlEmail)
        tlDateOfBirth = view.findViewById(R.id.tlDateOfBirth)
        tlGender=view.findViewById(R.id.tlGender)
        tlSurname=view.findViewById(R.id.tlSurname)
        tlOtherNames=view.findViewById(R.id.tlOtherNames)
        etEmail=view.findViewById(R.id.etEmail)
        tvLogin=view.findViewById(R.id.tvLogin)

        tvLogin.text = ""
        registerVM.getCountries()

        ivBack.setOnClickListener {
            findNavController().navigate(R.id.nav_landing)
        }

       btnContinue.setOnClickListener {
            if(validatedFields()) {
                val bundle = Bundle()
                bundle.putString("user", registerVM.currentUser.toJson())

                findNavController().navigate(R.id.nav_register_two,bundle)
            }
        }
       // btnLogin.setOnClickListener { findNavController().navigate(R.id.to_login_fragment) }

        initEditText()

       // val view = root
        return  view
    }
    fun initEditText(){
//       etSurname.setText("David")
//       etOtherNames.setText("manaman")
//       etGender.setText("Male")
//       etDateOfBirth.setText("1990-03-11")
//       etEmail.setText("manduku.david@ekenya.co.ke")

        var calendar = Calendar.getInstance()

       etDateOfBirth.setOnClickListener {

            val datePickerDialog = DatePickerDialog(
                activity!!,
                R.style.datepicker,
                OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    val newDate = Calendar.getInstance()
                    newDate[year, monthOfYear] = dayOfMonth
                    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH)
                    val date: String = simpleDateFormat.format(newDate.time)

                    calendar = newDate
                    var years =  TimeUnit.DAYS.convert(Calendar.getInstance().timeInMillis - newDate.timeInMillis, TimeUnit.MILLISECONDS)/365


                    if(years < 18){
                        ToastnDialog.toastError(this.requireContext(),"We only allow persons above 18 years")
                    }else{
                       etDateOfBirth.setText(date)
                    }
                    Timber.v("date year2 : %s",years)

                },
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH],
                calendar[Calendar.DAY_OF_MONTH]
            )

            datePickerDialog.show()


        }
        //
        val genderAdapter: ArrayAdapter<String> = ArrayAdapter(
            Objects.requireNonNull(activity)!!,
            android.R.layout.simple_spinner_dropdown_item,
            registerVM.getGenderList()
        )
       etGender.setOnClickListener {

            AlertDialog.Builder(activity)
                .setAdapter(
                    genderAdapter
                ) { dialog: DialogInterface, which: Int ->
                    if (which == 0) {
                       tlGender.error = getString(R.string.sign_up_one_error_select_gender)
                       tlGender.isErrorEnabled = false
                       etGender.setText("")
                    } else {
                       etGender.clearComposingText()
                       etGender.setText(registerVM.getGenderList().get(which))
                    }
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }

    private fun validatedFields(): Boolean {

//        registerBundle.putString("firstname",etSurname.text.toString())
//        registerBundle.putString("othernames",etOtherNames.text.toString())
//        registerBundle.putString("gender",etGender.text.toString())
//        registerBundle.putString("dateOfBirth","2020-01-01")
//        registerBundle.putString("email",etEmail.text.toString())
//        return true


        var isValid = true

        if (Objects.requireNonNull(etSurname.text).toString().trim { it <= ' ' }.isEmpty()) {
           tlSurname.error = getString(R.string.error_no_surname)
            isValid = false
        } else if (Objects.requireNonNull(etOtherNames.text).toString().trim { it <= ' ' }.isEmpty()) {
           tlOtherNames.error=getString(R.string.error_no_other_names)
            isValid = false
        } else if (Objects.requireNonNull(etGender.text).toString().trim { it <= ' ' }.isEmpty()) {
           tlGender.error = getString(R.string.error_no_gender)
            isValid = false
        } else if (etDateOfBirth.getText().isNullOrEmpty()) {
           tlDateOfBirth.setError(getString(R.string.error_no_date_of_birth))
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(etEmail.text.toString().trim { it <= ' ' }).matches()) {
            isValid = false
           tlEmail.error = getString(R.string.error_provide_email)
        }

        //save user information
        registerVM.currentUser = User("")
        registerVM.currentUser.email = etEmail.text.toString()
        registerVM.currentUser.dateofbirth = etDateOfBirth.text.toString()
        registerVM.currentUser.firstname = etSurname.text.toString()
        registerVM.currentUser.othernames = etOtherNames.text.toString()
        registerVM.currentUser.gender = etGender.text.toString()

        return isValid
    }



}
