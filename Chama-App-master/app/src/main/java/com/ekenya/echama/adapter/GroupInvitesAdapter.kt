package com.ekenya.echama.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.ekenya.echama.R
import com.ekenya.echama.model.GroupInvite
import com.ekenya.echama.repository.MyApiResponse
import com.ekenya.echama.viewModel.GroupViewModel

class GroupInvitesAdapter (val context: Fragment, val inviteList:ArrayList<GroupInvite> ):RecyclerView.Adapter<GroupInvitesAdapter.GroupInviteViewHolder>(){
    val groupModel = ViewModelProvider(context).get(GroupViewModel::class.java)

    inner class GroupInviteViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val btnAccept = itemView.findViewById<Button>(R.id.btnAccept)
        val btnReject = itemView.findViewById<Button>(R.id.btnReject)
        val tvGroupName = itemView.findViewById<TextView>(R.id.tvGroupName)
        val imgGroup = itemView.findViewById<ImageView>(R.id.imgGroup)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupInviteViewHolder {

       return GroupInviteViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_group_invites,parent,false))
    }

    override fun getItemCount(): Int {
        return inviteList.size
    }

    override fun onBindViewHolder(holder: GroupInviteViewHolder, position: Int) {
        holder.tvGroupName.text = inviteList[position].groupname

//        Glide.with(context)
//            .asBitmap()
//            .load(inviteList[position].groupimgurl)
//            .into(holder.imgGroup)

        holder.btnAccept.setOnClickListener {
            showActionDialogue("accept",position)
        }
        holder.btnReject.setOnClickListener {
            showActionDialogue("decline",position)
        }
    }
    fun showActionDialogue(action:String,position: Int){
        var title = "Accept"
        var msg = "Do you wish to join ${inviteList[position].groupname} group invitation"
        if(action.contentEquals("decline")){
             title = "Reject"
            msg = "Do you wish to reject ${inviteList[position].groupname} group invitation"
        }
        val builder = AlertDialog.Builder(context.requireContext())
        //set title for alert dialog
        builder.setTitle(title)
        //set message for alert dialog
        builder.setMessage(msg)
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton("Yes"){dialogInterface, which ->
            groupModel.myGroupApiResponseLD.postValue(MyApiResponse(700,"acceptDeclineGrpInviteRequest","",""))

            val jsonDeclineDetails = HashMap<String,Any>()
            jsonDeclineDetails["inviteid"] =  inviteList[position].id
            if(action.contentEquals("accept")){
                jsonDeclineDetails["action"] = "accept"
                groupModel.acceptDeclineGrpInvite(jsonDeclineDetails)
            }else{
                jsonDeclineDetails["action"] = "decline"
                groupModel.acceptDeclineGrpInvite(jsonDeclineDetails)
            }
            inviteList.remove(inviteList[position])
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