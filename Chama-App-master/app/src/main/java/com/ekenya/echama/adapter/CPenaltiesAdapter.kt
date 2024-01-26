package com.ekenya.echama.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ekenya.echama.R
import com.ekenya.echama.model.GroupPenalty
import com.ekenya.echama.util.DataUtil
import com.ekenya.echama.viewModel.GroupViewModel

class CPenaltiesAdapter (val contextF: Fragment, val penaltyList:ArrayList<GroupPenalty> ):RecyclerView.Adapter<CPenaltiesAdapter.GroupInviteViewHolder>(){
    val groupModel = ViewModelProvider(contextF).get(GroupViewModel::class.java)

    inner class GroupInviteViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val tvCurrency = itemView.findViewById<TextView>(R.id.tvCurrency)
        val tvPenaltyAmount = itemView.findViewById<TextView>(R.id.tvPenaltyAmount)
        val tvContributionName = itemView.findViewById<TextView>(R.id.tvContributionName)
        val tvMemberName = itemView.findViewById<TextView>(R.id.tvMemberName)
        val ivPenaltyUserPic = itemView.findViewById<ImageView>(R.id.ivPenaltyUserPic)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupInviteViewHolder {
       return GroupInviteViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_penalty,parent,false))
    }

    override fun getItemCount(): Int {
        if(penaltyList.size == 0){
            var p = GroupPenalty("John Doe")
            p.penaltyAmount = 200.00
            p.contributionName = "Corona Research Fund"
            penaltyList.add(p)
            p = GroupPenalty("David Joel")
            p.penaltyAmount = 2800.00
            p.contributionName = "Corona Research Fund"
            penaltyList.add(p)
        }
        return penaltyList.size
    }

    override fun onBindViewHolder(holder: GroupInviteViewHolder, position: Int) {
        holder.tvCurrency.text = DataUtil.gcurrency
        holder.tvPenaltyAmount.text = penaltyList[position].penaltyAmount.toString()
        holder.tvContributionName.text = penaltyList[position].contributionName
        holder.tvMemberName.text = penaltyList[position].memberName

        Glide.with(contextF.requireContext())
            .asBitmap()
            .placeholder(contextF.resources.getDrawable(R.mipmap.group_profile))
            .load(penaltyList[position].memberPicUrl)
            .into(holder.ivPenaltyUserPic)
    }





}