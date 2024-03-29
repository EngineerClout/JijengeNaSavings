package com.ekenya.echama.ui.group.contribution


import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.ekenya.echama.R
import com.ekenya.echama.adapter.ContributionDetailsAdapter
import com.ekenya.echama.databinding.FragmentContributionDetailsBinding
import com.ekenya.echama.model.Contribution
import com.ekenya.echama.model.Transaction
import com.ekenya.echama.model.User
import com.ekenya.echama.util.CustomProgressBar
import com.ekenya.echama.util.ToastnDialog
import com.ekenya.echama.util.expiredTokenDialogue
import com.ekenya.echama.util.successAlert
import com.ekenya.echama.viewModel.GroupViewModel
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 */
class ContributionDetailsFragment : Fragment() {
    private lateinit var contribAdapter: ContributionDetailsAdapter
    private  var activityList: ArrayList<Transaction> = ArrayList()
 // lateinit var rcvContribution:RecyclerView
//  lateinit var btnMakeContribution:Button
    lateinit var contribution:Contribution
    lateinit var groupViewModel: GroupViewModel
    private var _binding: FragmentContributionDetailsBinding? = null
    private val binding get() = _binding!!
    var  currentUser: User = User("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true);
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        groupViewModel = ViewModelProvider(this).get(GroupViewModel::class.java)
        _binding = FragmentContributionDetailsBinding.inflate(inflater, container, false)
        val rootView = binding.root
        R.layout.fragment_contribution_details
        //val rootView = inflater.inflate(R.layout.fragment_contribution_details, container, false)
        // rcvContribution = rootView.findViewById(R.id.rcvContribution)

        //binding.btnMakeContribution = rootView.findViewById(R.id.btnMakePayments)
//        var gson = "{\"id\":1,\"name\":\"Corona Research Fund\",\"startdate\":\"2020-09-17\",\"contributiondetails\":{\"amount\":2000.0,\"penalties\":{\"amount\":50.0,\"applied\":true}},\"contributiontypeid\":1,\"enddate\":\"recurring contribution\",\"contributiontypename\":\"recurring contribution\",\"scheduletypeid\":1,\"scheduletypename\":\"weekly\",\"amounttypeid\":1,\"amounttypename\":\"fixed amount\",\"groupid\":1,\"groupname\":\"AllinOne Investments\",\"amountcontributed\":50.0,\"active\":false,\"createdby\":\"muriithi gicheru\",\"contributioncount\":null}"
//        contribution = Contribution(0).fromJson(gson)
       // arguments?.putString("contribution",gson)
        arguments?.getString("contribution")?.let {
            Timber.v("contribution json")
            Timber.v(it)
            contribution = Contribution(0).fromJson(it)
        }

        binding.btnMakePayments.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("action","contribution")
            bundle.putString("contribution",contribution.toJson())
            findNavController().navigate(R.id.nav_makepayment,bundle)
        }
        initData()
        initTextView()
        initContributionTrx()

        return rootView
    }

    private fun initContributionTrx() {
        binding.rcvContribution.layoutManager = LinearLayoutManager(context,RecyclerView.VERTICAL,false)
        contribAdapter   = ContributionDetailsAdapter(this,activityList)
        binding.rcvContribution.adapter = contribAdapter

    }

    private fun initTextView() {
        binding.tvAmountContributed.text = contribution.getFContributedAmount()
        binding.tvContributionType.text = contribution.contributiontypename
        binding.tvContributionEndDate.text = contribution.enddate
        binding.tvContributionParticipants.text = "12/15 participants" //Todo get this data from api
        binding.tvDueDate.text = contribution.getDueDate()
    }

    private fun initData() {
        val jsonGroupDetails = HashMap<String,Any>()
        jsonGroupDetails["contributionid"] = contribution.id
        jsonGroupDetails["page"] = 0
        jsonGroupDetails["size"] = 100

        groupViewModel.getContributionTransactions(jsonGroupDetails).observe(viewLifecycleOwner, Observer {
            Timber.v("getContributionTransactions size ${it.size}")
            if(it.isNotEmpty()){
                activityList = it as ArrayList<Transaction>
                initContributionTrx()
                binding.txtNoTrxDesc.visibility = View.GONE
            }else{
                binding.txtNoTrxDesc.visibility = View.VISIBLE
            }
        })



        groupViewModel.myGroupApiResponseLD.observe(viewLifecycleOwner, Observer { myApiResponse ->
            CustomProgressBar.hide()
            Timber.v("myApiResponseLD  ${myApiResponse.code} ${myApiResponse.requestName} ${myApiResponse.message}")
            if(myApiResponse.code == 200){
                if(myApiResponse.requestName.contentEquals("contributionWithdrawRequest")) {
                    successAlert("Success",myApiResponse.message,false)
                }
                if(myApiResponse.requestName.contentEquals("getGroupAccountsRequest")) {
                    successAlert("Success",myApiResponse.message,false)
                }
                if(myApiResponse.requestName.contentEquals("deactivateContributionRequest")) {
                    successAlert("Success",myApiResponse.message)
                }

                if(myApiResponse.requestName.contentEquals("getCTrxRequest")) {
                    val gtrx = myApiResponse.responseObj as ArrayList<Transaction>
                    Timber.v("getCTrxRequest ${gtrx.size}")
                    if( gtrx.isEmpty()){
                        Timber.v("binding.txtNoTrxDesc.visibility VISIBLE")
                       // binding.txtNoTrxDesc.visibility = View.VISIBLE
                        ToastnDialog.toastSuccess(this.requireContext(),myApiResponse.message)
                    }else{
                        Timber.v("binding.txtNoTrxDesc.visibility GONE")

                       // binding.txtNoTrxDesc.visibility = View.GONE
                    }
                }

//                if(myApiResponse.requestName.contentEquals("getGrpTrxRequest")){
//
//                }

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

    private fun withdrawRequestDialog(message: String){
        context?.applicationContext?.let {
            MaterialDialog(it)
                .show {
                    title(R.string.withdraw_request)

                }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overflow_contribution_menu, menu)
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
                findNavController().navigateUp()
                //
                true
            }
            R.id.menu_cadd_record -> {
                val bundle = Bundle()
                bundle.putString("contributionid",contribution.id.toString())
                bundle.putString("action","contribution")
                findNavController().navigate(R.id.nav_record_payment,bundle)
                true
            }
            R.id.menu_cwithdraw_request -> {
                val bundle = Bundle()
                bundle.putString("contributionid",contribution.id.toString())
                bundle.putString("contributionname",contribution.name)
                findNavController().navigate(R.id.nav_cwithdraw,bundle)
                true
            }
            R.id.menu_cpenalties -> {
                val bundle = Bundle()
                bundle.putString("contributionid",contribution.id.toString())
                bundle.putString("contributionname",contribution.name)
                findNavController().navigate(R.id.nav_cpenalty,bundle)
                true
            }
            R.id.menu_cdeactivate -> {
                showCDeactivateDialogue()
                true
            }

            else -> {
                return item.onNavDestinationSelected( findNavController()) || super.onOptionsItemSelected(item)
                //super.onOptionsItemSelected(item)
            }
        }
    }
    fun showCDeactivateDialogue(){
        val builder = AlertDialog.Builder(this.requireContext())
        //set title for alert dialog
        builder.setTitle("Deactivate Contribution")
        //set message for alert dialog
        builder.setMessage("Do you want to deactivate ${contribution.name} group")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        //performing positive action
        builder.setPositiveButton("Yes"){dialogInterface, which ->

            val cData = HashMap<String,Any>()
            cData["contributionid"] = contribution.id.toString()
            groupViewModel.deactivateContribution(cData)
            CustomProgressBar.show(this.requireContext(),"Sending deactivation request")
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




}
