package com.ekenya.echama.ui.home


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ekenya.echama.R
import com.ekenya.echama.adapter.AllGroupsAdapter
import com.ekenya.echama.adapter.GroupInvitesAdapter
import com.ekenya.echama.databinding.FragmentMyGroupsBinding
import com.ekenya.echama.inc.AppInfo
import com.ekenya.echama.model.Group
import com.ekenya.echama.model.GroupInvite
import com.ekenya.echama.model.User
import com.ekenya.echama.util.CustomProgressBar
import com.ekenya.echama.util.DataUtil
import com.ekenya.echama.util.ToastnDialog
import com.ekenya.echama.util.expiredTokenDialogue
import com.ekenya.echama.viewModel.GroupViewModel
import com.ekenya.echama.viewModel.MainViewModel
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * A simple [Fragment] subclass.
 */
class MyGroups : Fragment() {
    lateinit var groupViewModel:GroupViewModel
    lateinit var allGrpAdapter : AllGroupsAdapter
    lateinit var gInviteadapter: GroupInvitesAdapter
    lateinit var maingroupVM: MainViewModel
    var page = 0
    var size = 10
    var allGroupList:ArrayList<Group> = ArrayList<Group>()
    private var _binding: FragmentMyGroupsBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        R.layout.fragment_my_groups
      //  Inflate the layout for this fragment
      //  val rootView = inflater.inflate(R.layout.fragment_my_groups, container, false) as View
//        btnShowMore = rootView.findViewById(R.id.btn_show_more)
//        txtUsername = rootView.findViewById(R.id.txtUsername)
//        btnGroupLayout = rootView.findViewById(R.id.btnGroupLayout)
//        txtLastLoginDate = rootView.findViewById(R.id.txtLastLoginDate)
//        rcvAllGroups  = rootView.findViewById(R.id.rcvMyGroups)
        R.layout.fragment_my_groups

        maingroupVM = ViewModelProvider(this).get(MainViewModel::class.java)
        _binding = FragmentMyGroupsBinding.inflate(inflater, container, false)
        val view = binding.root
        groupViewModel = ViewModelProvider(this).get(GroupViewModel::class.java)

        binding.btnCreateGroup.setOnClickListener{
            DataUtil.selectedGrpId = 0
            findNavController().navigate(R.id.nav_create_newGroup)
        }
        binding.btnShowMore.setOnClickListener {
            CustomProgressBar.show(this.requireContext())
            Timber.v( page.toString())
            groupViewModel.getAllGroups(page,size)
          //  page++
        }

        binding.rcvMyGroups.layoutManager = LinearLayoutManager(context,RecyclerView.VERTICAL,false)
        //get all member groups
        initData()
        initTextView()
        return view
    }

    private fun initTextView(user: User = User("")) {
        val simpleDateFormat = SimpleDateFormat("dd MMM yyyy",Locale.ENGLISH)
        val dateStr: String = simpleDateFormat.format(Calendar.getInstance().time)
        binding.txtLastLoginDate.text = "Last login: $dateStr"
        binding.txtUsername.text = "Welcome ${user.firstname}"

        binding.txtTitle.setOnClickListener {
            binding.rcvMyGroupsInvite.visibility = View.GONE
            binding.rcvMyGroups.visibility = View.VISIBLE
        }

        binding.txtInvitationTitle.setOnClickListener {
            groupViewModel.getGrpInvites()
            binding.rcvMyGroupsInvite.visibility = View.VISIBLE
            binding.rcvMyGroups.visibility = View.GONE
        }

    }

    private fun initRvGroupInvites(grpIviteLIst:ArrayList<GroupInvite>){
        binding.rcvMyGroupsInvite.layoutManager = LinearLayoutManager(context,RecyclerView.VERTICAL,false)
        gInviteadapter = GroupInvitesAdapter(this,grpIviteLIst)
        binding.rcvMyGroupsInvite.adapter = gInviteadapter
    }

    private fun initGroupsRV(grouplist: ArrayList<Group>) {
        Timber.v("initGroupsRV details %s", grouplist.size)
        if(grouplist.isNotEmpty()){
            allGroupList = grouplist
           // allGroupList.addAll(grouplist)
        }
        allGrpAdapter = AllGroupsAdapter(this, allGroupList)
        binding.rcvMyGroups.adapter = allGrpAdapter

    }

    private fun initData() {
        //loading default group
        // CustomProgressBar.show(this.requireContext())
       // val jsonGroupDetails = JSONObject()
        // jsonGroupDetails.put("id", SharedPrefenceUtil().getUserId(this.context!!))
//        jsonGroupDetails.put("page",0)
//        jsonGroupDetails.put("size",100)
        groupViewModel.getAllGroups(page,size)
       // page++

        groupViewModel.getGrpInvites()

        maingroupVM.userLD.observe(viewLifecycleOwner, Observer { users ->
            Timber.v("user "+users.size)
            if(users.isNotEmpty()){
                initTextView(users[0])
               // DataUtil.authToken = users[0].access_token
            }
        })

        groupViewModel.groupInvitesLD.observe(viewLifecycleOwner, Observer { groupsInvites ->
            Timber.v("Group invites "+groupsInvites.size)
            if(groupsInvites.isNotEmpty()){
                initRvGroupInvites(groupsInvites  as ArrayList<GroupInvite>)
                binding.txtInvitationTitle.visibility = View.VISIBLE
                binding.txtInvitationTitle.text = "Group Invites(${groupsInvites.size})"
            }else{
                binding.txtInvitationTitle.visibility = View.GONE
            }
        })

        groupViewModel.groupLD.observe(viewLifecycleOwner, Observer { groups ->
            initGroupsRV(groups  as ArrayList<Group>)
        })

        groupViewModel.myGroupApiResponseLD.observe(viewLifecycleOwner, Observer { myApiResponse ->
           CustomProgressBar.hide()
            Timber.v("myApiResponseLD  ${myApiResponse.code} ${myApiResponse.requestName} ${myApiResponse.message}")
            if(myApiResponse.code == 200){
                if (myApiResponse.requestName.contentEquals("acceptDeclineGrpInviteRequest")) {
                    ToastnDialog.toastSuccess(this.requireContext(),myApiResponse.message)
                }
                if (myApiResponse.requestName.contentEquals("getAllGroupsRequest")){
                    val type = Types.newParameterizedType(MutableList::class.java, Group::class.java)
                    val adapterRegion: JsonAdapter<List<Group>> = AppInfo.moshi.adapter(type)
                    var grpList = adapterRegion.fromJsonValue( myApiResponse.responseObj)!!
                    if(grpList.isNotEmpty()){
                      //  initGroupsRV(grpList as ArrayList<Group>)
                    }else{
                        binding.btnShowMore.visibility = View.GONE
                        if(page > 0){
                           // page--
                            ToastnDialog.toastSuccess(this.requireContext(),"No more groups available")
                        }else{

                        }
                    }
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

    // fun getAllGroups(page: Int, size: Int) {

//          .observe(this, Observer {
//          when(it){
//              is ViewModelWrapper.error -> Log.e("errorgroups",it.error)
//              is ViewModelWrapper.response -> initGroupsRV(it.value)
//          }
//      })
  //  }

    private fun viewMoreGroups () {

    }






}
