package com.ekenya.echama.ui.group.approval


import android.R
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.ekenya.echama.databinding.LoanApproveBinding
import com.ekenya.echama.inc.AppConstants
import com.ekenya.echama.model.GroupAccount
import com.ekenya.echama.model.Loan
import com.ekenya.echama.util.CustomProgressBar
import com.ekenya.echama.util.ToastnDialog
import com.ekenya.echama.util.expiredTokenDialogue
import com.ekenya.echama.util.successAlert
import com.ekenya.echama.viewModel.GroupViewModel
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 */
class LoanApprovalFragment : Fragment() {
    private var accountid: Int = 0
    lateinit var groupViewModel : GroupViewModel
    private var _binding: LoanApproveBinding? = null
    private val binding get() = _binding!!

    var loan = Loan()
    private var groupAccList: List<GroupAccount> = ArrayList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = LoanApproveBinding.inflate(inflater, container, false)
        val rootView = binding.root
        //val rootView= inflater.inflate(R.layout.fragment_group_contribution, container, false)
        groupViewModel = ViewModelProvider(this).get(GroupViewModel::class.java)
      //  val btnCreateContribution = rootView.findViewById(R.id.btnCreate) as Button
      //  btnCreateContribution.setOnClickListener { findNavController().navigate(R.id.nav_create_contribution) }
        arguments?.getString("loan")?.let {
            loan = loan.fromJson(it)
        }

        initData()
        initTextView()
        initButton()
        initImageV()
        return rootView

    }

    private fun initImageV() {
        Glide.with(this)
            .asBitmap()
            .load(AppConstants.base_url+"/chama/mobile/req/countries/"+"KE")
            .into( binding.ivGroupProfileHolder)
    }

    fun selectGroupAcc(){
        val accountAdapter = context?.let { ArrayAdapter(it, R.layout.simple_spinner_dropdown_item, groupAccList) }
        accountAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        AlertDialog.Builder(this.requireContext())
            .setTitle("Select Group Account")
            .setAdapter(accountAdapter) { dialog: DialogInterface, which: Int ->
                binding.txtGroupAccount.text = groupAccList[which].toString()
                accountid = groupAccList[which].accountid
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun initTextView() {
        binding.tvLoanProductName.text =loan.loanProductName
        binding.tvMemberName.text = loan.memberName
        binding.tvAppliedAmount.text = loan.getFAmount()
        binding.tvAppliedAmount.text = loan.getFAmount()
        binding.tvAppliedDate.text = loan.getFAppliedonDate()
        binding.txtGroupAccount.setOnClickListener {
            selectGroupAcc()
        }

    }

    private fun initButton() {
        binding.btnApproveLoan.setOnClickListener {
            if (validateInfo()) {
                val loanData = HashMap<String, Any>()
                loanData["loanapplicationid"] = loan.loanId
                loanData["approve"] = true
                loanData["accountid"] = accountid
                CustomProgressBar.show(this.requireContext(), "Sending request")
                groupViewModel.approveLoan(loanData)
            }
      }
    }

    private fun validateInfo(): Boolean {
        if( accountid == 0){
            ToastnDialog.toastError(this.requireContext(), "Select group account to record payment")
            return false
        }
        return true
    }


    private fun initData() {
        groupViewModel.getGroupAccounts()
        groupViewModel.myGroupApiResponseLD.observe(viewLifecycleOwner, Observer { myApiResponse ->
            CustomProgressBar.hide()
            Timber.v("myApiResponseLD  ${myApiResponse.code} ${myApiResponse.requestName} ${myApiResponse.message}")
            if(myApiResponse.code == 200){
                if(myApiResponse.requestName.contentEquals("approveLoanRequest")){
                    successAlert("Success",myApiResponse.message)
                }
                if(myApiResponse.requestName.contentEquals("getGroupAccountsRequest")) {
                    groupAccList = myApiResponse.responseObj as ArrayList<GroupAccount>
                }
            }else  if(myApiResponse.code == 700) {
                CustomProgressBar.show(this.requireContext())
            }else{
                if(myApiResponse.message.isNotEmpty()){
                    ToastnDialog.toastError(this.context!!,"Error:"+myApiResponse.message)
                    if(myApiResponse.message.toLowerCase().contains("token expired")){
                        expiredTokenDialogue()
                    }
                }
            }
        })

    }





}
