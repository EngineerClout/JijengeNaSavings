package com.ekenya.echama.ui.group


import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ekenya.echama.R
import com.ekenya.echama.databinding.FullstatementLayoutBinding
import com.ekenya.echama.util.CustomProgressBar
import com.ekenya.echama.util.ToastnDialog
import com.ekenya.echama.util.successAlert
import com.ekenya.echama.viewModel.MainViewModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

/**
 * A simple [Fragment] subclass.
 */
class FullStatementFragment : Fragment() {
    var startmillis:Long = 0L
    private var _binding: FullstatementLayoutBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel
    var action: String = ""
    val fStatementDateFormat = SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       // val rootview= inflater.inflate(R.layout.fragment_payment, container, false)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        _binding = FullstatementLayoutBinding.inflate(inflater, container, false)
        val rootView = binding.root
        //action = "penalty"
       // amount = 10.00
        arguments?.getString("action")?.let {
            action = it
            //userstatement should contain contribution id
            //groupstatement recharge wallet
            //contributionstatement contribution payment
        }


        initButton()
        initData()
        initView()
        initEdittextView()
        return rootView
    }
    private fun initEdittextView() {
        binding.etEmail.isFocusableInTouchMode = false
        binding.etEmail.isFocusable = false
        binding.etEmail.setOnClickListener {
            binding.etEmail.isFocusableInTouchMode = true
            binding.etEmail.isFocusable = true
        }

        binding.etEmail.doOnTextChanged { text, start, count, after ->
            text?.let {
                Timber.v("binding.etOtherNames "+text.toString())
                val pattern: Pattern = Patterns.EMAIL_ADDRESS

                if(!pattern.matcher(text).matches()){
                    binding.tlEmail.error = "Invalid email"
                }
                binding.btnFullStatement.isEnabled = !text.toString().isEmpty()
            }
        }
    }
    private fun initView() {
        Timber.v("action"+action)

        if(action.contentEquals("groupstatement")){
            binding.tvFstatementDesc.text = "Get group full statement"
        }

        binding.txtFstatementStartDate.setOnClickListener{ getFStartDate() }
        binding.txtFstatementEndDate.setOnClickListener{getFEndDate()}
    }
    /**
     * get calendar date in DD/MM/YYYY format
     */
    private fun getFEndDate() {
        if (startmillis != 0L) {
            val calendar = Calendar.getInstance()
            val mYear = calendar.get(Calendar.YEAR)
            val mMonth = calendar.get(Calendar.MONTH)
            val mDay = calendar.get(Calendar.DAY_OF_MONTH)

            val dpDialog = DatePickerDialog(activity!!, R.style.datepicker,
                DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    val newDate = Calendar.getInstance()
                    newDate[year, month] = dayOfMonth

                    binding.txtFstatementEndDate.text = fStatementDateFormat.format(newDate.time)

                }, mYear, mMonth, mDay)
            dpDialog.datePicker.minDate = startmillis
            dpDialog.datePicker.maxDate = calendar.timeInMillis

            dpDialog.show()
        } else {
            Toast.makeText(activity, "Please select reminder start date  first", Toast.LENGTH_SHORT).show()
            //Utils.showDialogWarning(getActivity(), "TBLAPP", "Please select pay bill date first");
        }

    }
    private fun getFStartDate() {
        val calendar = Calendar.getInstance()
        val mYear = calendar.get(Calendar.YEAR)
        val mMonth = calendar.get(Calendar.MONTH)
        val mDay = calendar.get(Calendar.DAY_OF_MONTH)
        val dpDialog = DatePickerDialog(activity!!, R.style.datepicker,
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                val newDate = Calendar.getInstance()
                newDate[year, month] = dayOfMonth
                startmillis = newDate.timeInMillis
                binding.txtFstatementStartDate.text = fStatementDateFormat.format(newDate.time)
            }
            , mYear, mMonth, mDay)
        dpDialog.datePicker.maxDate = calendar.timeInMillis
        dpDialog.show()
    }

    private fun initData() {
        mainViewModel.userLD.observe(viewLifecycleOwner, Observer { users ->
            if(users.isNotEmpty()){
                binding.etEmail.setText(users[0].email)
            }
        })

        mainViewModel.myApiResponseLD.observe(viewLifecycleOwner, Observer { myApiResponse ->
            CustomProgressBar.hide()
            if(myApiResponse.code == 200){

                if (myApiResponse.requestName.contentEquals("getFullStatementRequest")){
                    successAlert("Success",myApiResponse.message)
                }

            }else{
                Timber.v("Error: "+myApiResponse.requestName+" "+myApiResponse.message)
                ToastnDialog.toastError(this.context!!,"Error:"+myApiResponse.message)
            }
        })
    }

    private fun initButton() {
        binding.btnFullStatement.setOnClickListener { 
            if(validate_info()){
                    val jsonGroupDetails = HashMap<String,Any>()
                    jsonGroupDetails["startdate"] = binding.txtFstatementStartDate.text.toString()
                    jsonGroupDetails["enddate"] =  binding.txtFstatementEndDate.text.toString()
                    jsonGroupDetails["email"] =  binding.etEmail.text.toString()
                    mainViewModel.getUserFullStatement(jsonGroupDetails)
                    CustomProgressBar.show(this.requireContext(), "Sending request")
            }
        }
    }

    
    private fun validate_info(): Boolean {
        if(binding.txtFstatementStartDate.text.toString().contentEquals(resources.getString(R.string.pick_a_date))){
            ToastnDialog.toastError(this.requireContext(),"Please select start date")
            return false
        }

        if(binding.txtFstatementEndDate.text.toString().contentEquals(resources.getString(R.string.pick_a_date))){
            ToastnDialog.toastError(this.requireContext(),"Please select end date")
            return false
        }

        if(binding.etEmail.text.toString().isEmpty()){
            binding.tlEmail.error = "Invalid email"
        }
        return true
    }


}
