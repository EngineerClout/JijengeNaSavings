package com.ekenya.echama.ui.group.approval


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.ekenya.echama.adapter.MyPagerAdapter
import com.ekenya.echama.databinding.ApprovalDetailsBinding
import com.ekenya.echama.util.CustomProgressBar
import com.ekenya.echama.util.ToastnDialog
import com.ekenya.echama.util.expiredTokenDialogue
import com.ekenya.echama.viewModel.GroupViewModel
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 */
class ApprovalDetailsFragment : Fragment() {
    lateinit var viewPAdapter : MyPagerAdapter
   // lateinit var tabLayout : TabLayout
  //  lateinit var viewPager : ViewPager
    var currrentFrag : String = "payment"
    lateinit var groupViewModel:GroupViewModel
    private var _binding: ApprovalDetailsBinding? = null

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        _binding = ApprovalDetailsBinding.inflate(inflater, container, false)
        val rootView = binding.root
       // val rootView =  inflater.inflate(R.layout.fragment_loan_details, container, false)
//        var gson = "{\"groupid\":17,\"activemembership\":true,\"description\":\"\",\"groupname\":\"test\",\"groupsize\":1,\"invitesPhonenumbers\":\"null\",\"location\":\"nakuru\"}"
//        arguments?.putString("group",gson)
//        arguments?.getString("group")?.let {
//            Timber.v("group json")
//            Timber.v(it)
//             currrentGroup = Group(0).fromJson(it)
//        }
        groupViewModel = ViewModelProvider(this).get(GroupViewModel::class.java)
       // viewPager = rootView.findViewById(R.id.viewPager)
       // tabLayout = rootView.findViewById(R.id.tabLayout)
        viewPAdapter= MyPagerAdapter(childFragmentManager)
        //Seting uo function viewpager
        setupViewpager(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        setHasOptionsMenu(true)
        initData()
        return rootView
    }

    private fun initData() {
        groupViewModel.myGroupApiResponseLD.observe(viewLifecycleOwner, Observer { myApiResponse ->
            CustomProgressBar.hide()
            Timber.v("myApiResponseLD  ${myApiResponse.code} ${myApiResponse.requestName} ${myApiResponse.message}")
            if(myApiResponse.code == 200){
                if(myApiResponse.requestName.contentEquals("exitGroupRequest")) {
                    ToastnDialog.toastSuccess(this.requireContext(),myApiResponse.message)
                    findNavController().navigateUp()
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

    private fun setupViewpager(viewPager: ViewPager) {
        viewPAdapter.addFragment(LoanRequestFragment(),"Loan")
        viewPAdapter.addFragment(PaymentRequestFragment(),"Payment")
        viewPAdapter.addFragment(WithdrawalRequestFragment(),"Withdrawals")

        viewPager.adapter=viewPAdapter

        viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                Timber.v("onPageScrolled $position")
            }

            override fun onPageSelected(position: Int) {
                 Timber.v("onPageSelected $position")
                currrentFrag = "payment"
                if(position == 1){
                    currrentFrag = "withdraw"
                }
                if(position == 2){
                    currrentFrag = "loans"
                }
            }
            override fun onPageScrollStateChanged(state: Int) {
                Timber.v("onPageScrollStateChanged $state")
            }
        })
    }

}
