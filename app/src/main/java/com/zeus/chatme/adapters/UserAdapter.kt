package com.zeus.chatme.adapters

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.zeus.chatme.R
import com.zeus.chatme.adapters.UserAdapter.UserViewHolder
import com.zeus.chatme.chatSection.Chat
import com.zeus.chatme.chatSection.ChatDetailActivity
import com.zeus.chatme.databinding.SearchUserLayoutBinding
import com.zeus.chatme.models.User
import java.text.SimpleDateFormat
import java.util.*

class UserAdapter(private val context: Context, private val userList: List<User>) :
    RecyclerView.Adapter<UserViewHolder>() {

    private var mListener: onItemClickListener? = null
    var fUser: FirebaseUser? = null
    var theLastMessage: String? = null
    var timeLastMessage: String? = null

    interface onItemClickListener {
        fun onItemClick(position: Int) {}
    }

    fun setOnItemClickListener(listener: onItemClickListener?) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(
            LayoutInflater.from(context).inflate(R.layout.search_user_layout, parent, false), mListener)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        fUser = FirebaseAuth.getInstance().currentUser

        holder.binding.selectUserName.text = user.username

        if (user.imageURL == "default") {
            holder.binding.selectUserDp.setImageResource(R.drawable.default_dp_2)
        }
        else {
            Glide.with(context).load(user.imageURL).placeholder(R.drawable.default_dp_2).into(holder.binding.selectUserDp)
//            Picasso.get().load(user.imageURL).into(holder.binding.selectUserDp)
        }

        val senderId = FirebaseAuth.getInstance().uid
        val senderRoom = senderId + user.id

        //comment the code to red last message because it will crash since we are using 'chat' as the model and not 'message'

        //for last message and time
        theLastMessage = "default"
        FirebaseDatabase.getInstance().reference.child("Chats").child(senderRoom).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snapshot1 in snapshot.children) {
                        val message = snapshot1.getValue(Chat::class.java)!!
                        if (message.receiver == senderId && message.sender == user.id ||
                            message.receiver == user.id && message.sender == senderId) {
                            theLastMessage = message.message
                            if (message.timestamp != null) {
                                timeLastMessage = message.timestamp.toString()
                                val time = timeLastMessage!!.toLong()
                                val currentTime = System.currentTimeMillis()

                                //Convert time Stamp to date and time
                                val cal = Calendar.getInstance(Locale.ENGLISH)
                                cal.timeInMillis = time
                                val uploadedDateTime = DateFormat.format("dd/MM/yyyy", cal).toString()

                                //Convert time Stamp to date and time
                                val cal2 = Calendar.getInstance(Locale.ENGLISH)
                                cal2.timeInMillis = currentTime
                                val systemDateTime = DateFormat.format("dd/MM/yyyy", cal2).toString()
                                if (systemDateTime == uploadedDateTime) {
                                    if(message.type.equals("image")) {
                                        val simpleDateFormat = SimpleDateFormat("hh:mm a")
                                        holder.binding.selectDate.visibility = View.VISIBLE
                                        holder.binding.selectDate.text = simpleDateFormat.format(
                                            Date(time)
                                        ) //set time textview to visible
                                        holder.binding.selectUserLastMessage.text = "Photo"
                                        holder.binding.selectUserLastMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                            ContextCompat.getDrawable(context, R.drawable.ic_image),
                                            null,
                                            null,
                                            null)
                                    }
                                    else if(message.type.equals("video")){
                                        val simpleDateFormat = SimpleDateFormat("hh:mm a")
                                        holder.binding.selectDate.visibility = View.VISIBLE
                                        holder.binding.selectDate.text = simpleDateFormat.format(
                                            Date(time)
                                        ) //set time textview to visible
                                        holder.binding.selectUserLastMessage.text = "Video"
                                        holder.binding.selectUserLastMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                            ContextCompat.getDrawable(context, R.drawable.ic_video_2),
                                            null,
                                            null,
                                            null)

                                    }
                                    else if(message.type.equals("audio")){
                                        val simpleDateFormat = SimpleDateFormat("hh:mm a")
                                        holder.binding.selectDate.visibility = View.VISIBLE
                                        holder.binding.selectDate.text = simpleDateFormat.format(
                                            Date(time)
                                        ) //set time textview to visible
                                        holder.binding.selectUserLastMessage.text = "Audio"
                                        holder.binding.selectUserLastMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                            ContextCompat.getDrawable(context, R.drawable.ic_mic),
                                            null,
                                            null,
                                            null)

                                    }
                                    else if(message.type.equals("text")){
                                        val simpleDateFormat = SimpleDateFormat("hh:mm a")
                                        holder.binding.selectDate.visibility = View.VISIBLE
                                        holder.binding.selectDate.text = simpleDateFormat.format(
                                            Date(time)
                                        ) //set time textview to visible
                                        holder.binding.selectUserLastMessage.text = theLastMessage
                                        if(message.messageURL.equals("null")) { // message deleted, show deleted icon
                                            holder.binding.selectUserLastMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                                ContextCompat.getDrawable(context, R.drawable.ic_deleted),
                                                null,
                                                null,
                                                null)
                                        } else {
                                            holder.binding.selectUserLastMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                                null,
                                                null,
                                                null,
                                                null)
                                        }
                                    }
                                } else {
                                    if (message.type.equals("image")) {
                                        holder.binding.selectDate.visibility = View.VISIBLE
                                        holder.binding.selectDate.text = uploadedDateTime //set time textview to visible
                                        holder.binding.selectUserLastMessage.text = "Photo"
                                        holder.binding.selectUserLastMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                            ContextCompat.getDrawable(context, R.drawable.ic_image),
                                            null,
                                            null,
                                            null)

                                    }
                                    else if (message.type.equals("video")) {
                                        holder.binding.selectDate.visibility = View.VISIBLE
                                        holder.binding.selectDate.text = uploadedDateTime //set time textview to visible
                                        holder.binding.selectUserLastMessage.text = "Video"
                                        holder.binding.selectUserLastMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                            ContextCompat.getDrawable(context, R.drawable.ic_video_2),
                                            null,
                                            null,
                                            null)
                                    }
                                    else if (message.type.equals("audio")) {
                                        holder.binding.selectDate.visibility = View.VISIBLE
                                        holder.binding.selectDate.text = uploadedDateTime //set time textview to visible
                                        holder.binding.selectUserLastMessage.text = "Audio"
                                        holder.binding.selectUserLastMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                            ContextCompat.getDrawable(context, R.drawable.ic_mic),
                                            null,
                                            null,
                                            null)
                                    }
                                    else if (message.type.equals("text")) {
                                        holder.binding.selectDate.visibility = View.VISIBLE
                                        holder.binding.selectDate.text = uploadedDateTime //set time textview to visible
                                        holder.binding.selectUserLastMessage.text = theLastMessage
                                        if(message.messageURL.equals("null")) { // message deleted, show deleted icon
                                            holder.binding.selectUserLastMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                                ContextCompat.getDrawable(context, R.drawable.ic_deleted),
                                                null,
                                                null,
                                                null)
                                        } else {
                                            holder.binding.selectUserLastMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                                null,
                                                null,
                                                null,
                                                null)
                                        }
                                    }
                                }
                            } else { //If user has not chatted
                                holder.binding.selectUserLastMessage.text = user.bio
                                holder.binding.selectDate.visibility = View.INVISIBLE //gone
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        holder.itemView.setOnClickListener {
            context.startActivity(Intent(context, ChatDetailActivity::class.java)
                .putExtra("userId", user.id)
//                .putExtra("profilePic", user.imageURL)
                .putExtra("tap", 0))
//                .putExtra("userName", user.username))
            Animatoo.animateSlideLeft(context);
        }

    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class UserViewHolder(itemView: View, listener : onItemClickListener?) : RecyclerView.ViewHolder(itemView) {
        var binding: SearchUserLayoutBinding

        init {
            binding = SearchUserLayoutBinding.bind(itemView)
        }
    }
}