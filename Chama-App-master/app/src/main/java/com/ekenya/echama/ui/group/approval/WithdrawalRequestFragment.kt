package com.ekenya.echama.ui.group.approval


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ekenya.echama.adapter.WithdrawRequestAdapter
import com.ekenya.echama.databinding.FragmentGwithdrawalRequestsBinding
import com.ekenya.echama.model.Withdraw
import com.ekenya.echama.util.CustomProgressBar
import com.ekenya.echama.util.DataUtil
import com.ekenya.echama.util.ToastnDialog
import com.ekenya.echama.util.expiredTokenDialogue
import com.ekenya.echama.util.successAlert
import com.ekenya.echama.viewModel.GroupViewModel
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 */
class WithdrawalRequestFragment : Fragment() {
    private lateinit var withdrawList: ArrayList<Withdraw>
    lateinit var groupViewModel : GroupViewModel
    lateinit var withdrawAdapter: WithdrawRequestAdapter
    private var _binding: FragmentGwithdrawalRequestsBinding? = null
    private val binding get() = _binding!!
    val cPage = 0
    val cSize = 100

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentGwithdrawalRequestsBinding.inflate(inflater, container, false)
        val rootView = binding.root
        //val rootView= inflater.inflate(R.layout.fragment_group_contribution, container, false)
        groupViewModel = ViewModelProvider(this).get(GroupViewModel::class.java)
      //  val btnCreateContribution = rootView.findViewById(R.id.btnCreate) as Button
      //  btnCreateContribution.setOnClickListener { findNavController().navigate(R.id.nav_create_contribution) }

        initData()
        initButton()
        return rootView
    }

    private fun initButton() {
    }

    private fun initGrpWithdrawRV() {
        binding.rcvWithdrawals.layoutManager= LinearLayoutManager(context,RecyclerView.VERTICAL,false)
        withdrawAdapter = WithdrawRequestAdapter(this,withdrawList)
        binding.rcvWithdrawals.adapter = withdrawAdapter
    }

    private fun initData() {

        val jsonContribution = HashMap<String,Any>()
        jsonContribution["filterid"] = DataUtil.selectedGrpId
        jsonContribution["filter"] = "group"
        jsonContribution["page"] = cPage
        jsonContribution["size"] = cSize
        groupViewModel.getGrpWithdrawalRequest(jsonContribution)

        groupViewModel.myGroupApiResponseLD.observe(viewLifecycleOwner, Observer { myApiResponse ->
            CustomProgressBar.hide()
            Timber.v("myApiResponseLD  ${myApiResponse.code} ${myApiResponse.requestName} ${myApiResponse.message}")
            if(myApiResponse.code == 200){
                if(myApiResponse.requestName.contentEquals("getGrpWithdrawalRequest")) {
                    val gtrx = myApiResponse.responseObj as ArrayList<Withdraw>
                    Timber.v("grpTrxList ${gtrx.size}")
                    if( gtrx.isEmpty()){
                        binding.txtEmptyDesc.visibility = View.VISIBLE
                    }else{
                        withdrawList = gtrx
                        initGrpWithdrawRV()
                        binding.txtEmptyDesc.visibility = View.GONE
                    }
                }
                if(myApiResponse.requestName.contentEquals("approveDeclineGrpWithdrawalRequest")){
                    successAlert("Success",myApiResponse.message,false)
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
