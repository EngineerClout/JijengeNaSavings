package com.ekenya.echama.ui.group.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ekenya.echama.R
import com.ekenya.echama.adapter.GroupAccountsAdapter
import com.ekenya.echama.databinding.AddAccountsFragmentBinding
import com.ekenya.echama.model.GroupAccount
import com.ekenya.echama.model.GroupAccountDetails
import com.ekenya.echama.util.CustomProgressBar
import com.ekenya.echama.util.DataUtil
import com.ekenya.echama.util.ToastnDialog
import com.ekenya.echama.util.expiredTokenDialogue
import com.ekenya.echama.util.successAlert
import com.ekenya.echama.viewModel.GroupViewModel
import com.google.android.material.button.MaterialButton
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 */
class AddAccountFragment : Fragment() {
    lateinit var groupViewModel:GroupViewModel
    private var _binding: AddAccountsFragmentBinding? = null
    private val binding get() = _binding!!
    var choosenAccount = ""
    var choosenAccountH = HashMap<String,Int>()
    var gAcc = GroupAccount(0)
    private var groupAccList: List<GroupAccount> = ArrayList()

    private var groupBankAccList: ArrayList<GroupAccount> = ArrayList()
    private var groupSaccoAccList: ArrayList<GroupAccount> = ArrayList()
    private var groupMobileAccList: ArrayList<GroupAccount> = ArrayList()
    private var groupPettyAccList: ArrayList<GroupAccount> = ArrayList()
    private var groupInvestmentAccList: ArrayList<GroupAccount> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true);
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        groupViewModel = ViewModelProvider(this).get(GroupViewModel::class.java)
        _binding = AddAccountsFragmentBinding.inflate(inflater, container, false)
        val rootView = binding.root
            R.layout.add_accounts_fragment
            initView()
            initButton()
            initData()
          //  binding.bankCL.visibility =  View.GONE //Todo remove this
            return rootView
        }
    private fun initTextView() {
          binding.tvBank.text = "Bank Account"+"(${groupBankAccList.size})"
          binding.tvSacco.text = "Sacco Account"+"(${groupSaccoAccList.size})"
          binding.tvMobileMoney.text = "Mobile Money Account"+"(${groupMobileAccList.size})"
          binding.tvPettyCash.text = "Petty Cash Account"+"(${groupPettyAccList.size})"
          binding.tvInvestment.text = "Investment Account"+"(${groupInvestmentAccList.size})"
    }
    fun showGroupAccountsDialogue(title:String = "",gacList:ArrayList<GroupAccount>){
        val builder = AlertDialog.Builder(this.requireContext())
        val mDialogView = LayoutInflater.from(this.requireContext()).inflate(R.layout.dialogue_groupaccount_layout, null)
        builder.setView(mDialogView)
        //set title for alert dialog
        //builder.setTitle("In development")
        //set message for alert dialog
        // builder.setMessage("Do you want to log out ")
        //builder.setIcon(android.R.drawable.ic_dialog_alert)
        //performing positive action

        var tvDialogTitle: TextView =  mDialogView.findViewById(R.id.tvDialogTitle)
        var tvDialogMessage: TextView =  mDialogView.findViewById(R.id.tvDialogMessage)
        var rcvGroupAcc: RecyclerView =  mDialogView.findViewById(R.id.rcvGroupAcc)
        var btnAddAccount: MaterialButton =  mDialogView.findViewById(R.id.btnAddAccount)
        tvDialogTitle.text = title
     //   tvDialogMessage.text = "Enter your pin to view balance"

        rcvGroupAcc.layoutManager= LinearLayoutManager(context,RecyclerView.VERTICAL,false)
        val recentAdapter = GroupAccountsAdapter(this, gacList)
        rcvGroupAcc.adapter = recentAdapter

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        btnAddAccount.setOnClickListener {
              toggleFMenu()
              alertDialog.dismiss()
        }
        // Set other dialog properties

        alertDialog.setCancelable(true)
        alertDialog.show()
    }

    private fun initButton() {
       binding.btnSaveAccount.setOnClickListener {
           if(validate()){
               CustomProgressBar.show(this.requireContext(),"Sending request")
               groupViewModel.addGroupAccount(gAcc)
           }
       }

    }

    private fun validate(): Boolean {
        if(choosenAccount.contentEquals("BA")){
//            binding.etBankName.setText("Equity")
//            binding.etBranchName.setText("Waiyaki")
//            binding.etAccountNumber.setText("00200345")
//            binding.etAccountName.setText("DavChamaBacc")
//            binding.etAmount.setText("5000")

            if(binding.etBankName.text.toString().isEmpty()){
                binding.tlBankName.error = "Enter Bank name"
                return false
            }

            if(binding.etBranchName.text.toString().isEmpty()){
                binding.tlBranch.error = "Enter Bank Branch name"
                return false
            }
            if(binding.etAccountNumber.text.toString().isEmpty()){
                binding.tlAccountNumber.error = "Enter Bank Account Name"
                return false
            }
            if(binding.etAccountName.text.toString().isEmpty()){
                binding.tlAccountName.error = "Enter Bank Account Name"
                return false
            }
            gAcc.accounttypeid = choosenAccountH[choosenAccount]!!
            gAcc.groupid = DataUtil.selectedGrpId
            gAcc.accountname = binding.etAccountName.text.toString()
            if(binding.etAmount.text.toString().isNotEmpty()){
                gAcc.accountbalance = binding.etAmount.text.toString().toDouble()
            }
            gAcc.accountdetails = GroupAccountDetails(binding.etAccountNumber.text.toString())
            gAcc.accountdetails?.branch = binding.etBranchName.text.toString()
            gAcc.accountdetails?.bankName = binding.etBankName.text.toString()


            return true

        }else
        if(choosenAccount.contentEquals("SA")){
//            binding.etSaccoName.setText("Teacher Sacco")
//            binding.etSaccoBranchName.setText("Tom Mboya")
//            binding.etSaccoAccountNumber.setText("00200346")
//            binding.etSaccoAccountName.setText("Dav Chama Sacco")
//            binding.etSaccoAmount.setText("4000")

            if(binding.etSaccoName.text.toString().isEmpty()){
                binding.tlSaccoName.error = "Enter Sacco name"
                return false
            }

            if(binding.etSaccoBranchName.text.toString().isEmpty()){
                binding.tlSaccoBranch.error = "Enter Sacco Branch name"
                return false
            }
            if(binding.etSaccoAccountNumber.text.toString().isEmpty()){
                binding.tlSaccoAccountNumber.error = "Enter Sacco Account Name"
                return false
            }
            if(binding.etSaccoAccountName.text.toString().isEmpty()){
                binding.tlSaccoAccountName.error = "Enter Sacco Account Name"
                return false
            }
            gAcc.accounttypeid = choosenAccountH[choosenAccount]!!
            gAcc.groupid = DataUtil.selectedGrpId
            gAcc.accountname = binding.etSaccoAccountName.text.toString()
            if(binding.etSaccoAmount.text.toString().isNotEmpty()){
                gAcc.accountbalance = binding.etSaccoAmount.text.toString().toDouble()
            }
            gAcc.accountdetails = GroupAccountDetails(binding.etSaccoAccountNumber.text.toString())
            gAcc.accountdetails?.branch = binding.etSaccoBranchName.text.toString()
            gAcc.accountdetails?.saccoName = binding.etSaccoName.text.toString()

        }else
        if(choosenAccount.contentEquals("MMA")){

//            binding.etMnoProvider.setText("Safaricom")
//            binding.etBusinessName.setText("David")
//            binding.etMmAccountNumber.setText("808480")
//            binding.etMmoneyAmount.setText("3000")

            if(binding.etBusinessName.text.toString().isEmpty()){
                binding.tlMmBusinessName.error = "Enter business name"
                return false
            }
            if(binding.etMmAccountNumber.text.toString().isEmpty()){
                binding.tlMmAccountNumber.error = "Enter  Phone or Paybill or Lipa na Mpesa No of the business"
                return false
            }

            gAcc.accounttypeid = choosenAccountH[choosenAccount]!!
            gAcc.groupid = DataUtil.selectedGrpId
            gAcc.accountname = binding.etBusinessName.text.toString()
            if(binding.etMmoneyAmount.text.toString().isNotEmpty()){
                gAcc.accountbalance = binding.etMmoneyAmount.text.toString().toDouble()
            }

            gAcc.accountdetails = GroupAccountDetails(binding.etMmAccountNumber.text.toString())
            if(binding.etMnoProvider.text.toString().isNotEmpty()){
                gAcc.accountdetails?.mnoProvider = binding.etMnoProvider.text.toString()
            }

        }else
        if(choosenAccount.contentEquals("PCA")){
//            binding.etPcHolderName.setText("Daudi David")
//            binding.etPcAccNo.setText("254780824461")
//            binding.etPcAmount.setText("2000")

            if(binding.etPcHolderName.text.toString().isEmpty()){
                binding.tlPcHolderName.error = "Enter petty cash holder name"
                return false
            }

            if(binding.etPcAccNo.text.toString().isEmpty()){
                binding.tlPcAccNo.error = "Please enter petty cash holder phone number"
                return false
            }

            gAcc.accounttypeid = choosenAccountH[choosenAccount]!!
            gAcc.groupid = DataUtil.selectedGrpId
            gAcc.accountname = binding.etPcHolderName.text.toString()
            if(binding.etPcAmount.text.toString().isNotEmpty()){
                gAcc.accountbalance = binding.etPcAmount.text.toString().toDouble()
            }
            gAcc.accountdetails = GroupAccountDetails(binding.etPcAccNo.text.toString())

        }else
        if(choosenAccount.contentEquals("IN")){
//            binding.etInvestmentManager.setText("John  Doe")
//            binding.etInvestmentName.setText("Kiwi Source")
//            binding.etInvestmentAccNo.setText("254780824461")
//            binding.etInvestmentDesc.setText("Located at ruiru ,fenced and 50000 gross harvest")
//            binding.etInvestmentAmount.setText("1000")

            if(binding.etInvestmentManager.text.toString().isEmpty()){
                binding.tlInvestmentManager.error = "Enter investment manager"
                return false
            }
            if(binding.etInvestmentName.text.toString().isEmpty()){
                binding.tlInvestmentName.error = "Enter investment name"
                return false
            }

            if(binding.etInvestmentDesc.text.toString().isEmpty()){
                binding.tlInvestmentDesc.error = "Please enter investment description"
                return false
            }

            gAcc.accounttypeid = choosenAccountH[choosenAccount]!!
            gAcc.groupid = DataUtil.selectedGrpId
            gAcc.accountname = binding.etInvestmentName.text.toString()
            if(binding.etInvestmentAmount.text.toString().isNotEmpty()){
                gAcc.accountbalance = binding.etInvestmentAmount.text.toString().toDouble()
            }
            gAcc.accountdetails = GroupAccountDetails(binding.etInvestmentAccNo.text.toString())
            gAcc.accountdetails?.description = binding.etInvestmentDesc.text.toString()
            gAcc.accountdetails?.managedby = binding.etInvestmentManager.text.toString()


        }

        return true
    }

    private fun toggleFMenu(){
        if(binding.bankCL.isVisible){
            if(choosenAccount.contentEquals("BA")){
                binding.addBankAccountCL.visibility = View.VISIBLE
            }
            if(choosenAccount.contentEquals("SA")){
                binding.addSaccoAccountCL.visibility = View.VISIBLE
            }
            if(choosenAccount.contentEquals("MMA")){
                binding.addMobileMoneyAccountCL.visibility = View.VISIBLE
            }
            if(choosenAccount.contentEquals("PCA")){
                binding.addPcAccountCL.visibility = View.VISIBLE
            }
            if(choosenAccount.contentEquals("IN")){
                binding.addInvestmentAccountCL.visibility = View.VISIBLE
            }

            binding.bankCL.visibility =  View.GONE
            binding.saccoCL.visibility = View.GONE
            binding.mmoneyCL.visibility = View.GONE
            binding.pettycashCL.visibility = View.GONE
            binding.investmentCL.visibility = View.GONE
            binding.btnSaveAccount.visibility = View.VISIBLE
        }else{
            binding.bankCL.visibility =  View.VISIBLE
            binding.saccoCL.visibility = View.VISIBLE
            binding.mmoneyCL.visibility = View.VISIBLE
            binding.pettycashCL.visibility = View.VISIBLE
            binding.investmentCL.visibility = View.VISIBLE
            binding.btnSaveAccount.visibility = View.GONE
        }

        toggleAccountFieldsLay()
    }

    private fun toggleAccountFieldsLay() {

        if(binding.bankCL.isVisible){
            binding.addBankAccountCL.visibility = View.GONE
            binding.addSaccoAccountCL.visibility = View.GONE
            binding.addMobileMoneyAccountCL.visibility = View.GONE
            binding.addPcAccountCL.visibility = View.GONE
            binding.addInvestmentAccountCL.visibility = View.GONE
        }
    }

    private fun initView() {
        binding.btnSaveAccount.visibility = View.GONE
        binding.bankCL.setOnClickListener {
            choosenAccount = "BA"
            if(groupBankAccList.size > 0 ){
                showGroupAccountsDialogue("Bank Account", groupBankAccList)
            }else{
                toggleFMenu()
            }

        }

        binding.saccoCL.setOnClickListener {
            choosenAccount = "SA"
            if(groupSaccoAccList.size > 0 ){
                showGroupAccountsDialogue("Sacco Account", groupSaccoAccList)
            }else{
                toggleFMenu()
            }
        }

        binding.mmoneyCL.setOnClickListener {
            choosenAccount = "MMA"
            if(groupMobileAccList.size > 0 ){
                showGroupAccountsDialogue("Mobile Money Account", groupMobileAccList)
            }else{
                toggleFMenu()
            }


        }

        binding.pettycashCL.setOnClickListener {
            choosenAccount = "PCA"
            if(groupPettyAccList.size > 0 ){
                showGroupAccountsDialogue("Petty Cash Account", groupPettyAccList)
            }else{
                toggleFMenu()
            }
        }

        binding.investmentCL.setOnClickListener {
            choosenAccount = "IN"
            if(groupInvestmentAccList.size > 0 ){
                showGroupAccountsDialogue("Investment Account", groupInvestmentAccList)
            }else{
                toggleFMenu()
            }

        }

    }


    private fun initData() {

        groupViewModel.getGroupAccounts()?.observe(viewLifecycleOwner, Observer { gAccList ->
            Timber.v("getGroupAccounts ${gAccList.size}");
//            groupAccList =  gAccList
//            groupBankAccList.clear()
//            groupMobileAccList.clear()
//            groupSaccoAccList.clear()
//            groupPettyAccList.clear()
//            groupInvestmentAccList.clear()
//            for(gAcc in gAccList){
//                Timber.v("grp json ${gAcc.accountdetails?.toJson()}")
//                Timber.v("grp jsoggn ${gAcc.accountdetailsJson}")
//                if(gAcc.accounttypeid == 2){
//                    groupBankAccList.add(gAcc)
//                }
//                if(gAcc.accounttypeid == 3){ //
//                    groupMobileAccList.add(gAcc)
//                }
//                if(gAcc.accounttypeid == 4){ //
//                    groupSaccoAccList.add(gAcc)
//                }
//                if(gAcc.accounttypeid == 5){ //
//                    groupPettyAccList.add(gAcc)
//                }
//                if(gAcc.accounttypeid == 7){ //
//                    groupInvestmentAccList.add(gAcc)
//                }
//            }
//            initTextView()
        })
        groupViewModel.getAccountTypes()?.observe(viewLifecycleOwner, Observer { gAccTypes ->
            Timber.v("${gAccTypes.size}");
            for( gAccType in gAccTypes){
                choosenAccountH[gAccType.prefix] = gAccType.id
            }
        })

        groupViewModel.myGroupApiResponseLD.observe(viewLifecycleOwner, Observer { gatlist ->

        })
        groupViewModel.myGroupApiResponseLD.observe(viewLifecycleOwner, Observer { myApiResponse ->
            CustomProgressBar.hide()
            Timber.v("myApiResponseLD  ${myApiResponse.code} ${myApiResponse.requestName} ${myApiResponse.message}")
            if(myApiResponse.code == 200){
                if(myApiResponse.requestName.contentEquals("addGroupAccountRequest")) {
                    successAlert("Success",myApiResponse.message)
                    toggleFMenu()
                }
                if(myApiResponse.requestName.contentEquals("getGroupAccountsRequest")) {
                    groupAccList = myApiResponse.responseObj as ArrayList<GroupAccount>
                    groupBankAccList.clear()
                    groupMobileAccList.clear()
                    groupSaccoAccList.clear()
                    groupPettyAccList.clear()
                    groupInvestmentAccList.clear()
                    for(gAcc in groupAccList){
                        Timber.v("grp json ${gAcc.accountdetails?.toJson()}")
                        Timber.v("grp jsoggn ${gAcc.accountdetailsJson}")
                        if(gAcc.accounttypeid == 2){
                            groupBankAccList.add(gAcc)
                        }
                        if(gAcc.accounttypeid == 3){ //
                            groupMobileAccList.add(gAcc)
                        }
                        if(gAcc.accounttypeid == 4){ //
                            groupSaccoAccList.add(gAcc)
                        }
                        if(gAcc.accounttypeid == 5){ //
                            groupPettyAccList.add(gAcc)
                        }
                        if(gAcc.accounttypeid == 7){ //
                            groupInvestmentAccList.add(gAcc)
                        }
                    }
                    initTextView()
                }

            }else  if(myApiResponse.code == 700) {
                CustomProgressBar.show(this.requireContext())
            }else{
                if(myApiResponse.message.isNotEmpty()){
                    ToastnDialog.toastError(this.requireContext(),"Error:"+myApiResponse.message)
                    if(myApiResponse.message.toLowerCase().contains("token expired")){
                        expiredTokenDialogue()
                    }
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * listens to menu item click event
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Timber.v("onOptionsItemSelected group")
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            android.R.id.home -> {
                if(binding.bankCL.isVisible){
                    findNavController().navigateUp()
                }else{
                    toggleFMenu()
                }
                true
            }
            else -> {
                return item.onNavDestinationSelected( findNavController()) || super.onOptionsItemSelected(item)
                //super.onOptionsItemSelected(item)
            }
        }
    }


}
