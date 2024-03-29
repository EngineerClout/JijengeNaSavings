package com.ekenya.echama.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.ekenya.echama.R
import com.ekenya.echama.model.Member
import com.ekenya.echama.util.DataUtil
import timber.log.Timber

class SelectContactAdapter(val context:Fragment,val contactList:List<Member>):RecyclerView.Adapter<SelectContactAdapter.SelectContactViewHolder>() {
lateinit var selectedContactsList:ArrayList<Member>
    inner class SelectContactViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        var name = itemView.findViewById(R.id.txt_member_name) as TextView
        var phone = itemView.findViewById<TextView>(R.id.txt_meember_phone)
        var checkbox = itemView.findViewById<CheckBox>(R.id.img_check)
        var roleSpinner = itemView.findViewById<Spinner>(R.id.roleSpinner)
        var btnAddRole = itemView.findViewById<Button>(R.id.btnAddRole)
        var txtRole = itemView.findViewById<TextView>(R.id.txtRole)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectContactViewHolder {
        selectedContactsList = ArrayList()
        return SelectContactViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_contact,parent,false))
    }

    override fun getItemCount(): Int {
       return contactList.size
    }

    override fun onBindViewHolder(holder: SelectContactViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.name.text = contactList[position].name
        holder.phone.text = contactList[position].phoneNumber

         var roleAr = arrayListOf("Chairperson","Treasurer","Secretary","Member")


        if (holder.roleSpinner != null) {
            val adapter = ArrayAdapter(context.requireContext() ,android.R.layout.simple_spinner_item, roleAr)
            holder.roleSpinner.adapter = adapter
            holder.roleSpinner.setSelection(3)
            holder.roleSpinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View, pos: Int, id: Long) {
                    contactList[position].role = roleAr[pos]
                    Timber.v("role ${roleAr[pos]}")

                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                    Timber.v("onNothingSelected ")
                }
            }
        }
        holder.btnAddRole.setOnClickListener {
            showRoleAlert(holder,position)
        }
        holder.txtRole.text = "Member"

        holder.checkbox.setOnClickListener {
            if (holder.checkbox.isChecked){
                selectedContactsList.add(contactList[position])
            }else{
                selectedContactsList.remove(contactList[position])
            }
            // making selecetd ist static data
            DataUtil.selectedContacts = selectedContactsList
        }
    }

    private fun showRoleAlert(
        holder: SelectContactViewHolder,
        position: Int
    ) {
        var items = arrayOf("Chairperson","Treasurer","Secretary","Member")
       // val items = arrayOf("Red", "Orange", "Yellow", "Blue")
        val builder = AlertDialog.Builder(context.requireContext())
//        val builder = AlertDialog.Builder(ContextThemeWrapper(context.requireContext(),R.style.AlertDialogCustom))
        with(builder)
        {
            setTitle("Choose a group role for ${contactList[position].name}")

            setSingleChoiceItems(items, 3
                ) { dialog, which ->
                     contactList[position].role = items[which]
                     holder.txtRole.text = items[which]
                        Timber.v(items[which])
                       // selectedList.add(which)
                   // } else if (selectedList.contains(which)) {
                       // selectedList.remove(Integer.valueOf(which))
                }
//            setItems(items) { dialog, which ->
//                Toast.makeText(context, items[which] + " is clicked", Toast.LENGTH_SHORT).show()
//            }
            builder.setPositiveButton("Okay"){dialogInterface, which ->
                dialogInterface.dismiss()
            }

            show()
        }
    }
}