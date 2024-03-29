package com.ekenya.echama.ui.group.contribution


import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ekenya.echama.R
import com.ekenya.echama.databinding.FragmentCreateContributionBinding
import com.ekenya.echama.model.ContributionDetails
import com.ekenya.echama.model.ContributionType
import com.ekenya.echama.model.CroneJob
import com.ekenya.echama.model.Penalty
import com.ekenya.echama.model.Reminder
import com.ekenya.echama.model.ScheduleType
import com.ekenya.echama.util.CustomProgressBar
import com.ekenya.echama.util.DataUtil
import com.ekenya.echama.util.ToastnDialog
import com.ekenya.echama.util.expiredTokenDialogue
import com.ekenya.echama.util.toJson
import com.ekenya.echama.viewModel.GroupViewModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


/**
 * A simple [Fragment] subclass.
 */
class CreateContributionFragment : Fragment() {
//lateinit var etContributionType:EditText
    lateinit var groupViewModel:GroupViewModel
     var contribTypeList:ArrayList<String> = ArrayList()
     var contribTypeModelList: List<ContributionType> = ArrayList()
     var contribScheduleModelList:List<ScheduleType> = ArrayList()
     var scheduleList:ArrayList<String> = ArrayList()
//    lateinit var txtStartDate: TextView
//    lateinit var txtEndDate: TextView
//    lateinit var tvSchedule:TextView
     var selectedIndividualContribution:Int = 1
     var millis:Long = 0L
     var remindermillis:Long = 0L
    internal var myCalendar = Calendar.getInstance()
    private var _binding: FragmentCreateContributionBinding? = null
    private val binding get() = _binding!!
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH)
    val reminderDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.ENGLISH)
    var reminderDate = Date()
    val contDetails = ContributionDetails()
    var penalty = Penalty()
    var reminder = Reminder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true);
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCreateContributionBinding.inflate(inflater, container, false)
        val rootView = binding.root
        R.layout.fragment_create_contribution

        groupViewModel = ViewModelProvider(this).get(GroupViewModel::class.java)
        millis = 0

        binding.rbAnyAmount.setOnCheckedChangeListener { compoundButton, b ->
            selectedIndividualContribution=2
            Timber.v("selectedIndividualContribution $selectedIndividualContribution $b")
        }

        binding.rbFixedAmount.setOnCheckedChangeListener { compoundButton, b ->
            selectedIndividualContribution = 1
            Timber.v("selectedIndividualContribution $selectedIndividualContribution $b")
            if(b){
                binding.tlAmoutPerMember.visibility = View.VISIBLE
            }else{
                binding.tlAmoutPerMember.visibility = View.GONE
                binding.etAmountPerMember.setText("0.0")
            }
        }


        binding.txtPickSchedule.setOnClickListener{selectContributionSchedule()}

        binding.txtStartDate.setOnClickListener{ getStartDate() }
        binding.txtEndDate.setOnClickListener{getEndDate()}


        binding.etContributionType.setOnClickListener { selectContributionType() }
        initView()
        initData()
        initButton()
        initCheckBox()
        initImageView()
        initEdittext()
        initPenalty()
        initReminder()


        return rootView
    }

    private fun initReminder() {
        binding.txtReminderStartDate.setOnClickListener{ getReminderStartDate() }
        binding.txtReminderEndDate.setOnClickListener{getReminderEndDate()}
        binding.txtPickReminderSchedule.setOnClickListener{selectReminderSchedule()}


        binding.btnSaveReminder.setOnClickListener {
            binding.cbReminder.isChecked = validate_reminder()
            if(binding.cbReminder.isChecked){
                binding.txtReminderStartDate.text  = resources.getString(R.string.pick_a_date)
                binding.txtPickReminderSchedule.text  = resources.getString(R.string.pick_a_schedule)
                binding.secondLayCL.visibility = View.VISIBLE
                binding.reminderLayCL.visibility = View.GONE
                val jsonContribution = HashMap<String,Any>()
                contDetails.reminder = reminder
                jsonContribution["reminder"] =  contDetails

            }
        }

        binding.btnCancelReminder.setOnClickListener {
            binding.cbReminder.isChecked = validate_reminder()
            binding.secondLayCL.visibility = View.VISIBLE
            binding.reminderLayCL.visibility = View.GONE
        }

    }

    private fun initPenalty(){
        binding.tvPenaltySchedule.setOnClickListener{selectPenaltySchedule()}
        binding.tvPenaltyStartDate.setOnClickListener{ getPenaltyStartDate() }
        binding.rbPercentage.setOnCheckedChangeListener { compoundButton, b ->
            binding.tvPenaltyType.text = "% Percentage"
            binding.rbPenaltyFixedAmount.isChecked = !b
           // binding.rbPercentage.isChecked = true
        }

        binding.rbPenaltyFixedAmount.setOnCheckedChangeListener { compoundButton, b ->
            binding.tvPenaltyType.text = "Fixed Amount"
            binding.rbPercentage.isChecked = !b
           // binding.rbPenaltyFixedAmount.isChecked = true
        }

        binding.btnSavePenalty.setOnClickListener {
            binding.cbPenalties.isChecked = validate_penalty()
            if(binding.cbPenalties.isChecked){
                binding.tvPenaltySchedule.text = resources.getString(R.string.pick_a_schedule)
                binding.tvPenaltyStartDate.text = resources.getString(R.string.pick_a_date)
                binding.etPenaltyValue.text?.clear()
                binding.secondLayCL.visibility = View.VISIBLE
                binding.penaltyLayCL.visibility = View.GONE
                val jsonContribution = HashMap<String,Any>()
                contDetails.penalties = penalty
                jsonContribution["penalty"] =  contDetails



                Timber.v("penalty ${jsonContribution.toJson()}")
            }
        }

        binding.btnCancelPenalty.setOnClickListener {
            binding.cbPenalties.isChecked = validate_penalty()
            binding.secondLayCL.visibility = View.VISIBLE
            binding.penaltyLayCL.visibility = View.GONE
        }
    }

    private fun initEdittext() {

    }

    private fun initImageView() {
        binding.imgReminder.setOnClickListener {
            showReminderDialogue(true)
        }

        binding.imgPenaltyEdit.setOnClickListener {
            showPenaltyDialogue(true)
        }

    }

    private fun initCheckBox() {

        binding.cbPenalties.setOnClickListener {
            if( binding.cbPenalties.isChecked){
                showPenaltyDialogue()
            }else{
                removePenalty()
            }
        }
        binding.cbReminder.setOnClickListener {
            if( binding.cbReminder.isChecked){
                showReminderDialogue()
            }else{
               removeReminder()
            }
        }
    }
    private fun removePenalty(){
        val builder = AlertDialog.Builder(context)
        //set title for alert dialog
        builder.setTitle("Remove Penalty")
        //set message for alert dialog
        builder.setMessage("Do you want to remove contribution penalty?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        //performing positive action
        builder.setPositiveButton("Yes"){dialogInterface, which ->

            penalty.applied = false
            binding.cbReminder.text = "Reminder"
            dialogInterface.dismiss()
        }
        builder.setNegativeButton("No"){dialogInterface, which ->
            binding.cbReminder.isChecked = true
            dialogInterface.dismiss()
        }
        // Create the AlertDialog
        val  alertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
    private fun removeReminder(){
        val builder = AlertDialog.Builder(context)
        //set title for alert dialog
        builder.setTitle("Remove Reminder")
        //set message for alert dialog
        builder.setMessage("Do you want to remove contribution reminder?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        //performing positive action
        builder.setPositiveButton("Yes"){dialogInterface, which ->
            reminder.applied = false
            binding.cbReminder.text = "Reminder"
            dialogInterface.dismiss()
        }
        builder.setNegativeButton("No"){dialogInterface, which ->
            binding.cbReminder.isChecked = true
            dialogInterface.dismiss()
        }
        // Create the AlertDialog
      val  alertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
    private fun showReminderDialogue(isEdit:Boolean = false) {
        binding.secondLayCL.visibility = View.GONE
        binding.reminderLayCL.visibility = View.VISIBLE

//        Timber.v("showReminderDialogue")
//        val cal = Calendar.getInstance()
//        cal.time = reminderDate
//        DatePickerDialog(
//            this.requireContext(),R.style.datepicker,
//            OnDateSetListener { view, y, m, d ->
//                cal[Calendar.YEAR] = y
//                cal[Calendar.MONTH] = m
//                cal[Calendar.DAY_OF_MONTH] = d
//
//                // now show the time picker
//                TimePickerDialog(
//                    this.requireContext(),R.style.datepicker,
//                    OnTimeSetListener { view, h, min ->
//                        cal[Calendar.HOUR_OF_DAY] = h
//                        cal[Calendar.MINUTE] = min
//                        reminderDate = cal.time
//                        binding.cbReminder.text = "Reminder(${reminderDateFormat.format(reminderDate)})"
//                        //contDetails.reminderTime = reminderDateFormat.format(reminderDate)
//                    }, cal[Calendar.HOUR_OF_DAY],
//                    cal[Calendar.MINUTE], false
//                ).show()
//            }, cal[Calendar.YEAR], cal[Calendar.MONTH],
//            cal[Calendar.DAY_OF_MONTH]
//        ).show()
    }

    private fun showPenaltyDialogue(isEdit:Boolean = false) {
        binding.secondLayCL.visibility = View.GONE
        binding.penaltyLayCL.visibility = View.VISIBLE
    }

    private fun initButton() {
        binding.btnNext.setOnClickListener {
            if(validated("firstpart")){
                binding.firstLayCL.visibility = View.GONE
                binding.secondLayCL.visibility = View.VISIBLE
            }
        }

        binding.btnSave.setOnClickListener {
                saveContribution()
        }

    }

    private fun validate_reminder(): Boolean {

        if(binding.txtReminderStartDate.text.toString().contentEquals(resources.getString(R.string.start_date))){
            ToastnDialog.toastError(this.requireContext(),"Please select reminder due date")
            return false
        }

        if(binding.txtPickReminderSchedule.text.toString().contentEquals(resources.getString(R.string.pick_a_schedule))){
            ToastnDialog.toastError(this.requireContext(),"Please select reminder schedule")
            return false
        }
        reminder.applied = true
        binding.cbReminder.isChecked = true
        return true
    }
    private fun validate_penalty(): Boolean {
        if(binding.etPenaltyValue.text.isNullOrEmpty()){
            binding.tlPenaltyValue.error = "Enter Penalty amount"
            return false
        }

        if(binding.rbPercentage.isChecked){
            penalty.penaltytype = "percentage"
            if(binding.etPenaltyValue.text.toString().length >3){
                binding.tlPenaltyValue.error = "Invalid Percentage Value"
                return false
            }
        }else{
            penalty.penaltytype = "fixed"
        }

        penalty.amount = binding.etPenaltyValue.text.toString().toDouble()

        if(binding.tvPenaltyStartDate.text.toString().contentEquals(resources.getString(R.string.pick_a_date))){
            ToastnDialog.toastError(this.requireContext(),"Please select penalty due date")
            return false
        }

        if(binding.tvPenaltySchedule.text.toString().contentEquals(resources.getString(R.string.pick_a_schedule))){
            ToastnDialog.toastError(this.requireContext(),"Please select penalty schedule")
            return false
        }
        penalty.applied = true

        binding.cbPenalties.isChecked = true
        return true
    }

    private fun saveContribution() {
        if(validated()){
            val jsonContribution = HashMap<String,Any>()

            contDetails.amount =  binding.etTargetAmount.text.toString().toDouble()
            contDetails.memberamount = binding.etAmountPerMember.text.toString().toDouble()

            contDetails.reminder = reminder
            contDetails.penalties = penalty

            jsonContribution["amounttypeid"] = selectedIndividualContribution
            jsonContribution["name"] = binding.etContributionName.text.toString()
          //  jsonContribution["targetAmount"] = binding.etTargetAmount.text.toString() //Todo where to put in api
          //  jsonContribution["amountPerMember"] = binding.etAmountPerMember.text.toString() //Todo where to put in api
            jsonContribution["contributiondetails"] = contDetails
            jsonContribution["startdate"] = binding.txtStartDate.text
           // jsonContribution["endDate"] = binding.txtEndDate.text
            jsonContribution["groupid"] = DataUtil.selectedGrpId
            jsonContribution["contributiontypeid"] = DataUtil.contributionTypeId.toInt()
            jsonContribution["scheduletypeid"] = DataUtil.scheduleId.toInt()

           // jsonContribution["penalityAvailable"] = false
           // jsonContribution["remainderSet"] = true
           // val jsonMembergroup = JSONObject()
           // jsonMembergroup.put("id",DataUtil.selectedGrpId)
           // jsonContribution["memberGroups"] = jsonMembergroup
           // val jsoncontributionType = JSONObject()
           // jsoncontributionType.put("id",DataUtil.contributionTypeId.toInt())
           // jsonContribution["contributionType"] = jsoncontributionType
//            val jsonIndividualType = JSONObject()
//            jsonIndividualType.put("id",selectedIndividualContribution)
//            jsonContribution["indivualContributionType"] = jsonIndividualType
//            val jsonSchedule = JSONObject()
//            jsonSchedule.put("id",DataUtil.scheduleId.toInt())
//            jsonContribution["scheduleType"] = jsonSchedule

            Timber.v(jsonContribution.toJson())
            groupViewModel.createContribution(jsonContribution)
        }
    }

    private fun initView() {
        binding.firstLayCL.visibility   = View.VISIBLE
        binding.secondLayCL.visibility  = View.GONE
        binding.penaltyLayCL.visibility = View.GONE
        binding.reminderLayCL.visibility = View.GONE
    }

    private fun initData() {
        groupViewModel.getContributionType().observe(viewLifecycleOwner, Observer {contributionList ->
            Timber.v("contributionList ${contributionList.size}")
            if(contributionList.isNotEmpty()){
                contribTypeModelList = contributionList
                contribTypeList.clear()
                for (cTypeItem in contributionList){
                    Timber.v(cTypeItem.name.capitalize())
                    contribTypeList.add(cTypeItem.name.capitalize())
                }
            }
        })

        groupViewModel.getAmountType()
//            .observe(viewLifecycleOwner, Observer {schList ->
//            Timber.v("getAmountType ${scheduleList.size}")
//        })

        groupViewModel.getContributionSchedules().observe(viewLifecycleOwner, Observer {schList ->
            Timber.v("scheduleList ${scheduleList.size}")
//            if(scheduleList.isNotEmpty()){
//                contribScheduleModelList = schList
//                scheduleList.clear()
//                for (cTypeItem in schList){
//                    Timber.v(cTypeItem.name)
//                    scheduleList.add(cTypeItem.name.capitalize())
//                }
//            }
        })
        groupViewModel.myGroupApiResponseLD.observe(viewLifecycleOwner, Observer { myApiResponse ->
            CustomProgressBar.hide()
            Timber.v("myApiResponseLD  ${myApiResponse.code} ${myApiResponse.requestName} ${myApiResponse.message}")
            if(myApiResponse.code == 200){
                if(myApiResponse.requestName.contentEquals("createContributionRequest")) {
                    ToastnDialog.toastSuccess(this.context!!,myApiResponse.message)
                    findNavController().navigateUp()
                }
                if(myApiResponse.requestName.contentEquals("getContributionTypesRequest")) {
                    val gtrx = myApiResponse.responseObj as List<ContributionType>
                    Timber.v("ctypeList ${gtrx.size}")
                }
                if(myApiResponse.requestName.contentEquals("getContributionSchedulesRequest")) {
                    val schList = myApiResponse.responseObj as List<ScheduleType>
                    Timber.v("stypeList ${schList.size}")
                    contribScheduleModelList = schList
                    scheduleList.clear()
                    for (cTypeItem in schList){
                        Timber.v(cTypeItem.name)
                        scheduleList.add(cTypeItem.name)
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

    suspend fun sendContribution(jsonContribution: HashMap<String,Any>) {
        groupViewModel.createContribution(jsonContribution)
//            .observe(this, Observer {
//            when(it){
//                is ViewModelWrapper.error -> ToastnDialog.toastError(this.context!!,"Error: "+it.error)//Log.e("conteribRequest","Error occurred"+it.error)
//                is ViewModelWrapper.response -> handleRequestResponse(it.value)
//            }
//        })
    }

    private fun handleRequestResponse(value: String) {
        Timber.v(value)
        //USED GROUP TYPE MODEL SINCE THEY ARE HAVING SAME RESPOSE JSON
       // val mList= gson.fromJson(value,Array<JsonElement>::class.java).toList()

        //Return back to home fragment
       // ResponseWrapper().getApiResponse()
        ToastnDialog.toastSuccess(this.context!!,"Request was Successful")
        findNavController().navigateUp()
    }

    private fun validated(action:String = ""): Boolean {
         if(binding.etContributionName.text.isNullOrEmpty()){
             binding.tlContributionName.error = "Enter contribution name"
             return false
         }
        if(binding.etTargetAmount.text.isNullOrEmpty()){
             binding.tlTargetAmount.error = "Enter targeted amount"
             return false
         }
        if(binding.etContributionType.text.isNullOrEmpty()){
             binding.tlContributionType.error = "Select contribution type"
             return false
         }
        if(binding.etContributionType.text.isNullOrEmpty()){
             binding.tlContributionType.error = "Select contribution type"
             return false
         }
         if(binding.etAmountPerMember.text.isNullOrEmpty() && binding.rbFixedAmount.isChecked){
             binding.tlAmoutPerMember.error = "Enter amount required from a member "
             return false
         }

         if(action.contentEquals("firstpart")){
             return true
         }
        if(binding.txtStartDate.text.isNullOrEmpty()){
            binding.tlAmoutPerMember.error = "Enter amount required from a member "
            return false
        }
        if(binding.txtEndDate.text.isNullOrEmpty()){
            binding.tlAmoutPerMember.error = "Enter amount required from a member "
            return false
        }
        if(binding.txtPickSchedule.text.isNullOrEmpty()){
            binding.tlAmoutPerMember.error = "Enter amount required from a member "
            return false
        }



        return true
    }

    suspend fun getContributionSchedule() {
        groupViewModel.getContributionSchedules()
//            .observe(this, Observer {
//            when(it){
//                is ViewModelWrapper.error -> Log.e("scheduleerror","error"+it.error)
//                is ViewModelWrapper.response -> handleScheduleResponse(it.value)
//            }
//        })
    }




    fun selectContributionSchedule(){
        val accountAdapter = context?.let { ArrayAdapter(it, android.R.layout.simple_spinner_dropdown_item, scheduleList) }
        accountAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        AlertDialog.Builder(this.requireContext())
            .setTitle("Select Contribution Schedule")
            .setAdapter(accountAdapter) { dialog: DialogInterface, which: Int ->
                binding.txtPickSchedule.text = scheduleList[which]
                DataUtil.scheduleId = contribScheduleModelList[which].id.toString()
                dialog.dismiss()
            }
            .create()
            .show()
    }
    fun selectReminderSchedule(){
        var reminderScheduleList = scheduleList.filterNot {
            it.contentEquals("annualy")
        }

        val accountAdapter = context?.let { ArrayAdapter(it, android.R.layout.simple_spinner_dropdown_item, reminderScheduleList) }
        accountAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        AlertDialog.Builder(this.requireContext())
            .setTitle("Select Reminder Schedule")
            .setAdapter(accountAdapter) { dialog: DialogInterface, which: Int ->
                binding.txtPickReminderSchedule.text = reminderScheduleList[which]

                val cronJob = CroneJob()
                cronJob.hour = reminder.startdate?.hour!!
                cronJob.minute = reminder.startdate?.minute!!
                if (reminderScheduleList[which].contains("daily")) {

                } else if (reminderScheduleList[which].contains("weekly")) {
                    cronJob.dayofweek = "0"
                } else if (reminderScheduleList[which].contains("monthly")) {
                    cronJob.dayofmonth = reminder.startdate?.dayofmonth!!
                }
                reminder.frequency = cronJob

                dialog.dismiss()



            }
            .create()
            .show()
    }
    fun selectPenaltySchedule(){
        var penaltyList = scheduleList.filterNot {
            it.contentEquals("annualy")
        }

        val accountAdapter = context?.let { ArrayAdapter(it, android.R.layout.simple_spinner_dropdown_item, penaltyList) }
        accountAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        AlertDialog.Builder(this.requireContext())
            .setTitle("Select Penalty Schedule")
            .setAdapter(accountAdapter) { dialog: DialogInterface, which: Int ->
                binding.tvPenaltySchedule.text = penaltyList[which]

                        val cronJob = CroneJob()
                        cronJob.hour = penalty.startdate?.hour!!
                        cronJob.minute = penalty.startdate?.minute!!
                        if (penaltyList[which].contains("daily")) {

                        } else if (penaltyList[which].contains("weekly")) {
                            cronJob.dayofweek = "0"
                        } else if (penaltyList[which].contains("monthly")) {
                            cronJob.dayofmonth = penalty.startdate?.dayofmonth!!
                        }
                        penalty.frequency = cronJob

                        dialog.dismiss()



            }
            .create()
            .show()
    }
    fun selectContributionType() {
        val accountAdapter = context?.let { ArrayAdapter(it, android.R.layout.simple_spinner_dropdown_item, contribTypeList) }
        accountAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        AlertDialog.Builder(activity)
            .setTitle("Select Contribution")
            .setAdapter(accountAdapter) { dialog: DialogInterface, which: Int ->
                binding.etContributionType.setText(contribTypeList[which])
                DataUtil.contributionTypeId = contribTypeModelList[which].id.toString()
                dialog.dismiss()
            }
            .create()
            .show()

    }
    /**
     * get calendar date in DD/MM/YYYY format
     */
    private fun getReminderEndDate() {
        if (remindermillis != 0L) {
            val c = Calendar.getInstance()
            val calendar = Calendar.getInstance()
            val mYear = calendar.get(Calendar.YEAR)
            val mMonth = calendar.get(Calendar.MONTH)
            val mDay = calendar.get(Calendar.DAY_OF_MONTH)

            val dpDialog = DatePickerDialog(activity!!, R.style.datepicker,  OnDateSetListener { view, year, month, dayOfMonth ->
                val newDate = Calendar.getInstance()
                newDate[year, month] = dayOfMonth
                val endCron = CroneJob()
                endCron.month = month.toString()
                endCron.dayofmonth = dayOfMonth.toString()
                // now show the time picker
                TimePickerDialog(
                    this.requireContext(),R.style.datepicker,
                    OnTimeSetListener { view, h, min ->
                        newDate[Calendar.HOUR_OF_DAY] = h
                        newDate[Calendar.MINUTE] = min
                        endCron.hour = h.toString()
                        endCron.minute = min.toString()
                        reminder.enddate = endCron
                        binding.txtReminderEndDate.text = reminderDateFormat.format(newDate.time)
                    }, newDate[Calendar.HOUR_OF_DAY],
                    newDate[Calendar.MINUTE], false
                ).show()

            }, mYear, mMonth, mDay)

            dpDialog.datePicker.minDate = remindermillis
            dpDialog.show()
        } else {
            Toast.makeText(activity, "Please select reminder start date  first", Toast.LENGTH_SHORT).show()
            //Utils.showDialogWarning(getActivity(), "TBLAPP", "Please select pay bill date first");
        }

    }
    private fun getReminderStartDate() {
        val calendar = Calendar.getInstance()
        val mYear = calendar.get(Calendar.YEAR)
        val mMonth = calendar.get(Calendar.MONTH)
        val mDay = calendar.get(Calendar.DAY_OF_MONTH)
        val dpDialog = DatePickerDialog(activity!!, R.style.datepicker,
            OnDateSetListener { view, year, month, dayOfMonth ->
                val newDate = Calendar.getInstance()
                newDate[year, month] = dayOfMonth
                val startCron = CroneJob()
                startCron.month = month.toString()
                startCron.dayofmonth = dayOfMonth.toString()
                // now show the time picker
                TimePickerDialog(
                    this.requireContext(),R.style.datepicker,
                    OnTimeSetListener { view, h, min ->
                        newDate[Calendar.HOUR_OF_DAY] = h
                        newDate[Calendar.MINUTE] = min
                        startCron.hour = h.toString()
                        startCron.minute = min.toString()
                        reminder.startdate = startCron
                        remindermillis = newDate.timeInMillis
                        binding.txtReminderStartDate.text = reminderDateFormat.format(newDate.time)
                    }, newDate[Calendar.HOUR_OF_DAY],
                    newDate[Calendar.MINUTE], false
                ).show()

            }
            , mYear, mMonth, mDay)
        dpDialog.datePicker.minDate = calendar.timeInMillis
        dpDialog.show()
    }

    private fun getPenaltyStartDate() {
        val calendar = Calendar.getInstance()
        val mYear = calendar.get(Calendar.YEAR)
        val mMonth = calendar.get(Calendar.MONTH)
        val mDay = calendar.get(Calendar.DAY_OF_MONTH)
        val dpDialog = DatePickerDialog(activity!!, R.style.datepicker,
            OnDateSetListener { view, year, month, dayOfMonth ->
                val newDate = Calendar.getInstance()
                newDate[year, month] = dayOfMonth
                val startCron = CroneJob()
                startCron.month = month.toString()
                startCron.dayofmonth = dayOfMonth.toString()
                // now show the time picker
                TimePickerDialog(
                    this.requireContext(),R.style.datepicker,
                    OnTimeSetListener { view, h, min ->
                        newDate[Calendar.HOUR_OF_DAY] = h
                        newDate[Calendar.MINUTE] = min
                        startCron.hour = h.toString()
                        startCron.minute = min.toString()
                        penalty.startdate = startCron
                        binding.tvPenaltyStartDate.text = reminderDateFormat.format(newDate.time)
                    }, newDate[Calendar.HOUR_OF_DAY],
                    newDate[Calendar.MINUTE], false
                ).show()

            }
            , mYear, mMonth, mDay)
        dpDialog.datePicker.minDate = calendar.timeInMillis
        dpDialog.show()
    }
    private fun getStartDate() {
        val calendar = Calendar.getInstance()
        val mYear = calendar.get(Calendar.YEAR)
        val mMonth = calendar.get(Calendar.MONTH)
        val mDay = calendar.get(Calendar.DAY_OF_MONTH)
        val dpDialog = DatePickerDialog(activity!!, R.style.datepicker, myFromDateListener, mYear, mMonth, mDay)
        dpDialog.datePicker.minDate = calendar.timeInMillis
        dpDialog.show()
    }

    /**
     * get selected date from calendar
     */
    private val myFromDateListener =
        DatePickerDialog.OnDateSetListener { arg0, year, monthOfYear, dayOfMonth ->
//            var month=""
//            if (monthOfYear < 9) {
//               month= "0"+(monthOfYear+1).toString()
//            }
//            else{
//                month =(monthOfYear+1).toString()
//            }
            //val dates = ""+year + "-" + month + "-" + dayOfMonth.toString()  + ""
            //val dates = dayOfMonth.toString() + "- " + month + "-" + year + ""
            val newDate = Calendar.getInstance()
            newDate[year, monthOfYear] = dayOfMonth
            millis = newDate.timeInMillis
            val date: String = simpleDateFormat.format(newDate.time)
            //var years =  TimeUnit.DAYS.convert(Calendar.getInstance().timeInMillis - newDate.timeInMillis, TimeUnit.MILLISECONDS)/365
            binding.txtStartDate.text = date

         /*  val formatter  = DateTimeFormatter.ofPattern("yyyy MM dd");
                val text:String  = date.format(formatter);
                LocalDate.parse(text, formatter);*/
           // val sdf = SimpleDateFormat("yyyy-MM-DD")
            //LocalDate.parse(dates,sdf)
           // var date: Date? = null
//            try {
//                date = sdf.parse(dates)
//                binding.txtStartDate.text = dates
//            } catch (e: ParseException) {
//                // Utils.showDialogError(getActivity(), "Error", "Oops! error occured, please try again later");
//            }

        }

    /**
     * get calendar date in DD/MM/YYYY format
     */
    private fun getEndDate() {
        if (millis != 0L) {
            val c = Calendar.getInstance()
            val calendar = Calendar.getInstance()
            val mYear = calendar.get(Calendar.YEAR)
            val mMonth = calendar.get(Calendar.MONTH)
            val mDay = calendar.get(Calendar.DAY_OF_MONTH)

            val dpDialog = DatePickerDialog(activity!!, R.style.datepicker, myDateListener, mYear, mMonth, mDay)

            dpDialog.datePicker.minDate = millis
            dpDialog.show()
        } else {
            Toast.makeText(activity, "Please select paybill date first", Toast.LENGTH_SHORT).show()
            //Utils.showDialogWarning(getActivity(), "TBLAPP", "Please select pay bill date first");
        }

    }

    private val myDateListener =
        DatePickerDialog.OnDateSetListener { arg0, year, monthOfYear, dayOfMonth ->
            //val dates = dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year + ""
            //should be revised to output proper month with 0 prefix
            val newDate = Calendar.getInstance()
            newDate[year, monthOfYear] = dayOfMonth
            val date: String = simpleDateFormat.format(newDate.time)

            //var years =  TimeUnit.DAYS.convert(Calendar.getInstance().timeInMillis - newDate.timeInMillis, TimeUnit.MILLISECONDS)/365

            binding.txtEndDate.text = date


//            var month=""
//            if (monthOfYear < 9) {
//                month= "0"+(monthOfYear+1)
//            }
//            else{
//                month = (monthOfYear+1).toString()
//            }
//            val dates = ""+year + "-" + month + "-" + dayOfMonth.toString()  + ""
//            val sdf = SimpleDateFormat("yyyy-MM-DD")
//            binding.txtEndDate.text = dates.toString()
//            var date: Date? = null
//            try {
//                date = sdf.parse(dates)
//                binding.txtEndDate.text = date.toString()
//            } catch (e: ParseException) {
//                // Utils.showDialogError(getActivity(), "Error", "Oops! error occured, please try again later");
//            }
           // txtEndDate.setText(dates)
        }


    /*var date = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        //TODO Auto-generated method stub
        myCalendar.set(Calendar.YEAR, year)
        myCalendar.set(Calendar.MONTH, monthOfYear)
        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)


        txtStartDate.setText(myCalendar.time.toString())
        val sdf = SimpleDateFormat("yyyy-MM-DD")
        var date: Date? = null
        try {
            date = sdf.parse(myCalendar.time.toString())
            txtStartDate.setText(date.toString())
        } catch (e: ParseException) {
            // Utils.showDialogError(getActivity(), "Error", "Oops! error occured, please try again later");
        }
        }*/

  /* fun getStartDate(context: Context) {
        val dialog: DatePickerDialog
        dialog = DatePickerDialog(
            context, date, myCalendar
                .get(Calendar.YEAR), myCalendar
                .get(Calendar.MONTH), myCalendar
                .get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
        dialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.colorPrimary))
        dialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.colorPrimary))
    }*/

    /**
     * listens to menu item click event
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        super.onOptionsItemSelected(item)
        Timber.v("onOptionsItemSelected create")
        return when (item.itemId) {
            android.R.id.home -> {

                if(binding.penaltyLayCL.visibility == View.VISIBLE){
                    binding.secondLayCL.visibility = View.VISIBLE
                    binding.penaltyLayCL.visibility = View.GONE
                }else if(binding.reminderLayCL.visibility == View.VISIBLE){
                    binding.secondLayCL.visibility = View.VISIBLE
                    binding.reminderLayCL.visibility = View.GONE
                }else if(binding.secondLayCL.visibility == View.VISIBLE){
                    binding.firstLayCL.visibility = View.VISIBLE
                    binding.secondLayCL.visibility = View.GONE
                }else{
                    findNavController().navigateUp()
                }
                true
            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }
}
