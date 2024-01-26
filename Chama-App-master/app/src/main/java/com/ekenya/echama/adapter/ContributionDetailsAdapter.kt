package com.ekenya.echama.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.ekenya.echama.R
import com.ekenya.echama.model.Transaction
import timber.log.Timber

class ContributionDetailsAdapter (context: Fragment,val activityList:List<Transaction>):RecyclerView.Adapter<ContributionDetailsAdapter.ContribViewHolder>(){

    inner class ContribViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val tvMemberName = itemView.findViewById<TextView>(R.id.tvMemberName)
        val tvDate  = itemView.findViewById<TextView>(R.id.tvDate)
        val tvAmount = itemView.findViewById<TextView>(R.id.tvAmount)
        val tvCurrency = itemView.findViewById<TextView>(R.id.tvCurrency)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContribViewHolder {
        return ContribViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_contribution_single_payment,parent,false))
    }

    override fun getItemCount(): Int {
        Timber.v(""+activityList.size)
       return activityList.size
    }

    override fun onBindViewHolder(holder: ContribViewHolder, position: Int) {

        holder.tvMemberName.text = activityList[position].membername.capitalize()
        holder.tvDate.text = activityList[position].getFTrxDate()
        holder.tvAmount.text = activityList[position].getFAmount()
        holder.tvCurrency.text = activityList[position].currency
    }
}