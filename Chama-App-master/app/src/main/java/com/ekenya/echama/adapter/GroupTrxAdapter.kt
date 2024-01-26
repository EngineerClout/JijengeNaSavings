package com.ekenya.echama.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.ekenya.echama.R
import com.ekenya.echama.model.AllMembersModel

//Todo remove or edit this adapter
class GroupTrxAdapter (val context: Fragment, val memberList:List<AllMembersModel.Member>):RecyclerView.Adapter<GroupTrxAdapter.AllMembersViewHolder>(){

    inner class AllMembersViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val membername = itemView.findViewById<TextView>(R.id.tvNewGroup)
        val phonenumber = itemView.findViewById<TextView>(R.id.tvNewGroupDesc)
        val amount = itemView.findViewById<TextView>(R.id.tvAmount)
        val currency = itemView.findViewById<TextView>(R.id.tvCurrency)
        val paymentdate = itemView.findViewById<TextView>(R.id.tvPaymentDate)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllMembersViewHolder {
        return AllMembersViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_gmember_contribution,parent,false))
    }

    override fun getItemCount(): Int {
      return memberList.size
    }

    override fun onBindViewHolder(holder: AllMembersViewHolder, position: Int) {
       // val lastname? = memberList.get(position)?.lastName? :null
        holder.currency.setText("Ksh")
        holder.phonenumber.setText(memberList[position].phoneNumber)
        holder.membername.setText(memberList.get(position).firstName+" "+memberList.get(position).lastName)//TODO REMOVE IF LAST NAME IS NULL
        holder.amount.setText(memberList.get(position).totalContribution.toString()+".00")
        holder.paymentdate.setText(memberList.get(position).createdOn)
    }
}