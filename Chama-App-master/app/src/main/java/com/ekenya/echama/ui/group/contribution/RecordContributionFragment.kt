package com.ekenya.echama.ui.group.contribution


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ekenya.echama.MainActivity
import com.ekenya.echama.databinding.RecordContributionLayoutBinding
import com.ekenya.echama.model.GroupAccount
import com.ekenya.echama.model.LoanApproved
import com.ekenya.echama.model.User
import com.ekenya.echama.util.CustomProgressBar
import com.ekenya.echama.util.DataUtil
import com.ekenya.echama.util.ToastnDialog
import com.ekenya.echama.util.expiredTokenDialogue
import com.ekenya.echama.util.showInDevoplopmentDialogue
import com.ekenya.echama.util.successAlert
import com.ekenya.echama.viewModel.GroupViewModel
import com.ekenya.echama.viewModel.MainViewModel
import timber.log.Timber
import java.io.File


/**
 * A simple [Fragment] subclass.
 */
class RecordContributionFragment : Fragment() {
    private var paymentmode: String = ""
    private var amount: Double = 0.0
    private var _binding: RecordContributionLayoutBinding? = null
    private val binding get() = _binding!!
    var action: String = ""

    private var currentUser =  User()
    private var groupAccList: List<GroupAccount> = ArrayList()
    private lateinit var userViewModel: MainViewModel
    //lateinit var etContributionType:EditText
    lateinit var groupViewModel:GroupViewModel
    val MY_PERMISSIONS_REQUEST_CAMERA = 11
    val IMAGE_PICK_CODE = 999
    val CAMERA = 2

    private var creditaccountid = 0
    private var contributionid = 0
    private var contributionname = ""
    private var approvedLoan = LoanApproved()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       //val rootview= inflater.inflate(R.layout.fragment_payment, container, false)
        userViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        groupViewModel = ViewModelProvider(this).get(GroupViewModel::class.java)
        _binding = RecordContributionLayoutBinding.inflate(inflater, container, false)
        val rootView = binding.root

        arguments?.getString("action")?.let {
            action = it
            //loan repayment
            //contribution contribution payment
        }


        arguments?.getString("contributionid")?.let {
            contributionid = it.toInt()
        }

        arguments?.getString("loan")?.let {
            approvedLoan = approvedLoan.fromJson(it)
        }

        initView()
        initButton()
        initData()
        initTextView()
        checknGrantPermmision()

        return rootView
    }

    private fun initView() {

        if(action.contentEquals("loan")) {
            binding.txtGroupAccount.visibility = View.GONE
            binding.tvLabel.visibility = View.GONE
        }


    }

    private fun launchGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            val uri = data?.data
            if (uri != null) {
                uploadFile(uri.path.toString())
               //val bMapimage = BitmapFactory.decodeFile(uri.path.toString())
                binding.imgReceiptPic.setImageURI(uri)
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
    private fun uploadFile(filePath: String) {
        val file = File(filePath)
        //  var grpFilesAr : ArrayList<MultipartBody.Part> = ArrayList<MultipartBody.Part>()
        DataUtil.grpFilesAr.clear()
        DataUtil.grpFilesAr.add(file)


    }
    fun checknGrantPermmision():Boolean{

        val permission = ContextCompat.checkSelfPermission(activity!!.applicationContext,
            Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Timber.v("Permission to record denied")
            makePermisionRequest()
            return false
        }
        return  true
    }
    private fun makePermisionRequest() {
        requestPermissions( arrayOf(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE),
            MY_PERMISSIONS_REQUEST_CAMERA)
        // ActivityCompat.requestPermissions(activity!!,
        //     arrayOf(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE),
        //    MY_PERMISSIONS_REQUEST_CAMERA)
    }

    private fun initTextView() {
        binding.txtGroupAccount.setOnClickListener {
            selectGroupAcc()
        }
        binding.tvAddDocument.setOnClickListener {
            launchGallery()
        }
    }
    fun selectGroupAcc(){
        val accountAdapter = context?.let { ArrayAdapter(it, android.R.layout.simple_spinner_dropdown_item, groupAccList) }
        accountAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        AlertDialog.Builder(this.requireContext())
            .setTitle("Select Group Account")
            .setAdapter(accountAdapter) { dialog: DialogInterface, which: Int ->
                binding.txtGroupAccount.text = groupAccList[which].toString()
                creditaccountid = groupAccList[which].accountid
                dialog.dismiss()
            }
            .create()
            .show()
    }


    private fun initEdittext() {

    }

    private fun initImageView() {

    }

    private fun initButton() {
        binding.btnRecordPayment.setOnClickListener {
            if(action.contentEquals("loan")){
                (this.activity as MainActivity).showInDevoplopmentDialogue()
                if (validated()) {
                    val withdrawData = HashMap<String, Any>()
                    withdrawData["amount"] = binding.etxtAmount.text.toString().toDouble()
                    withdrawData["loandisbursedid"] = approvedLoan.loanId
                    withdrawData["receiptNo"] = binding.etxtReceiptNo.text.toString()
                    CustomProgressBar.show(this.requireContext(), "Sending request")
                    groupViewModel.recordLoanPayment(withdrawData)
                }

            }else if(action.contentEquals("contribution")) {
                if (validated()) {
                    val withdrawData = HashMap<String, Any>()
                    withdrawData["amount"] = binding.etxtAmount.text.toString().toDouble()
                    withdrawData["contributionid"] = contributionid
                    withdrawData["creditaccountid"] = creditaccountid
                    withdrawData["debitaccount"] = currentUser.phonenumber
                    withdrawData["receiptNo"] = binding.etxtReceiptNo.text.toString()
                    CustomProgressBar.show(this.requireContext(), "Sending request")
                    groupViewModel.recordPayment(withdrawData)

                }
            }
        }
    }



    private fun initData() {
        groupViewModel.getGroupAccounts()?.observe(viewLifecycleOwner, Observer { gAccList ->

//            groupAccList =  gAccList
//            groupAccStrList.clear()
//            for (cTypeItem in groupAccList){
//                Timber.v(cTypeItem.accountdetails?.getNameNAcc())
//                groupAccStrList.add(cTypeItem.accountdetails?.getNameNAcc()!!)
//            }

        })

        userViewModel.userLD.observe(viewLifecycleOwner, Observer { users ->
            if(users.isNotEmpty()){
                currentUser = users[0]
            }
        })


        groupViewModel.myGroupApiResponseLD.observe(viewLifecycleOwner, Observer { myApiResponse ->
            CustomProgressBar.hide()
            Timber.v("myApiResponseLD  ${myApiResponse.code} ${myApiResponse.requestName} ${myApiResponse.message}")
            if(myApiResponse.code == 200){

                if(myApiResponse.requestName.contentEquals("getGroupAccountsRequest")) {
                    groupAccList = myApiResponse.responseObj as ArrayList<GroupAccount>
                }

                if(
                    myApiResponse.requestName.contentEquals("recordPaymentRequest")
                    || myApiResponse.requestName.contentEquals("recordLoanPayment")
                ) {

                    if(DataUtil.grpFilesAr.size > 0){
                        CustomProgressBar.show(this.requireContext(),"Uploading image please wait")
                        groupViewModel.uploadPaymentReceipt(contributionid, DataUtil.getFileMultipart())

                    }else{
                        successAlert("Success",myApiResponse.message)
                    }
                }

                if(myApiResponse.requestName.contentEquals("uploadPaymentReceiptRequest")) {
                    DataUtil.grpFilesAr.removeAll(DataUtil.grpFilesAr)
                    Timber.v("size ${DataUtil.grpFilesAr}");
                    successAlert(message = myApiResponse.message)
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

    private fun uploadReceiptImage() {

    }

    private fun validated(): Boolean {
        if(action.contentEquals("contribution")) {
            if (creditaccountid == 0) {
                ToastnDialog.toastError(
                    this.requireContext(),
                    "Select group account to record payment"
                )
                return false
            }
        }
        if( binding.etxtReceiptNo.text.toString().isEmpty()){
            binding.tlReceiptNumber.error = "Enter receipt number"
            return false
        }
        if( binding.etxtAmount.text.toString().isEmpty()){
            binding.tlAmount.error = "Enter Amount"
            return false
        }



        return true
    }




}
