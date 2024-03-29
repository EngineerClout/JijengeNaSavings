package com.ekenya.echama.adapter

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ekenya.echama.R
import com.ekenya.echama.inc.AppConstants
import com.ekenya.echama.model.Loan
import com.ekenya.echama.repository.MyApiResponse
import com.ekenya.echama.viewModel.GroupViewModel

class LoanRequestAdapter(val context: Fragment, val loanList:ArrayList<Loan>):RecyclerView.Adapter<LoanRequestAdapter.RecentActivityViewHolder> (){
    val groupModel = ViewModelProvider(context).get(GroupViewModel::class.java)

    inner class RecentActivityViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val tvLoanProductName = itemView.findViewById(R.id.tvLoanProductName) as TextView
        val tvMemberName = itemView.findViewById(R.id.tvMemberName) as TextView
        val tvAppliedDate = itemView.findViewById(R.id.tvAppliedDate) as TextView
        val tvAppliedAmount = itemView.findViewById(R.id.tvAppliedAmount) as TextView
        val btnItemApprove = itemView.findViewById(R.id.btnItemApprove) as Button
        val btnItemReject = itemView.findViewById(R.id.btnItemReject) as Button
        val ivGroupProfileHolder = itemView.findViewById(R.id.ivGroupProfileHolder) as ImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentActivityViewHolder {
        return RecentActivityViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.items_loans_request,parent,false))
    }

    override fun getItemCount(): Int {
        return loanList.size
    }

    override fun onBindViewHolder(holder: RecentActivityViewHolder, position: Int) {
        holder.tvLoanProductName.text = loanList[position].loanProductName
        holder.tvMemberName.text = loanList[position].memberName
        holder.tvAppliedAmount.text = loanList[position].getFAmount()
        holder.tvAppliedAmount.text = loanList[position].getFAmount()
        holder.tvAppliedDate.text = loanList[position].getFAppliedonDate()

        holder.btnItemReject.setOnClickListener {
            showActionDialogue("decline",position);
        }
        holder.btnItemApprove.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("loan",loanList[position].toJson())
            findNavController(context).navigate(R.id.nav_approveloan,bundle)
        }

        Glide.with(context.requireContext())
            .asBitmap()
            .load(AppConstants.base_url+"/chama/mobile/req/countries/"+"KE")
            .into( holder.ivGroupProfileHolder)

    }
    fun showActionDialogue(action:String,position: Int){
        var title = "Approve"
        var msg = "Do you want to approve ${loanList[position].memberName} loan request?"
        if(action.contentEquals("decline")){
            title = "Reject"
            msg = "Do you want to reject ${loanList[position].memberName} loan request?"
        }
        val builder = AlertDialog.Builder(context.requireContext())
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton("Yes"){dialogInterface, which ->
            groupModel.myGroupApiResponseLD.postValue(MyApiResponse(700,"acceptDeclineGrploan","",""))


            val jsonDeclineDetails = HashMap<String,Any>()
            jsonDeclineDetails["loanapplicationid"] =  loanList[position].loanId
           // jsonDeclineDetails["accountid"] = 0
            if(action.contentEquals("approve")){
                jsonDeclineDetails["approve"] = true
                groupModel.approveLoan(jsonDeclineDetails)
            }else{
                jsonDeclineDetails["approve"] = false
                groupModel.approveLoan(jsonDeclineDetails)
            }
            loanList.remove(loanList[position])
            notifyItemRangeChanged(position, itemCount -1)
            notifyDataSetChanged()
            dialogInterface.dismiss()
        }
        
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