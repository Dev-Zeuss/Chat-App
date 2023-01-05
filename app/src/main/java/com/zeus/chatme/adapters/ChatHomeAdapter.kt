package com.zeus.chatme.adapters

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.zeus.chatme.R
import com.zeus.chatme.chatSection.Chat
import com.zeus.chatme.chatSection.ChatDetailActivity
import com.zeus.chatme.databinding.ChatChooseLayoutBinding
import com.zeus.chatme.models.User
import java.text.SimpleDateFormat
import java.util.*

class ChatHomeAdapter(var context : Context, var users : List<User>) : RecyclerView.Adapter<ChatHomeAdapter.ChatHomeViewHolder>() {

    private var mListener: onItemClickListener? = null
    private var theLastMessage: String? = null
    private var timeLastMessage: String? = null
    private var unread = 0

    interface onItemClickListener {
        fun onItemClick(position: Int) {}
    }

    fun setOnItemClickListener(listener: onItemClickListener?) {
        mListener = listener
    }

    class ChatHomeViewHolder(itemView : View, listener : onItemClickListener?): RecyclerView.ViewHolder(itemView) {
        var binding : ChatChooseLayoutBinding

        init {
            binding = ChatChooseLayoutBinding.bind(itemView)
            itemView.setOnClickListener {
                listener?.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ChatHomeViewHolder {
        return ChatHomeViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_choose_layout, parent, false), mListener)
    }

    override fun onBindViewHolder(holder: ChatHomeViewHolder, position: Int) {
        val user = users[position]

        val senderId = FirebaseAuth.getInstance().uid
        val senderRoom = senderId + user.id
        val receiverRoom = user.id + senderId

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
                                        holder.binding.selectDate.text = simpleDateFormat.format(Date(time)) //set time textview to visible
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
                                        holder.binding.selectDate.text = simpleDateFormat.format(Date(time)) //set time textview to visible
                                        holder.binding.selectUserLastMessage.text = "Video"
                                        holder.binding.selectUserLastMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                            ContextCompat.getDrawable(context, R.drawable.ic_video_2),
                                            null,
                                            null,
                                            null)

                                    }
                                    else if (message.type.equals("audio")) {
                                        val simpleDateFormat = SimpleDateFormat("hh:mm a")
                                        holder.binding.selectDate.visibility = View.VISIBLE
                                        holder.binding.selectDate.text = simpleDateFormat.format(Date(time)) //set time textview to visible
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
                                        holder.binding.selectDate.text = simpleDateFormat.format(Date(time)) //set time textview to visible
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

        //for unread messages
        unread = 0
        val dbReference = FirebaseDatabase.getInstance().reference.child("Chats").child(receiverRoom).child("messages")
        dbReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snapshot1 in snapshot.children) {
                    val message = snapshot1.getValue(Chat::class.java)
                    if (message!!.receiver == senderId && !message.isIsseen) { //check this
                        unread++
                    }
//                    if (unread == 0) {
//                        holder.binding.selectUnreadMessages.visibility = View.INVISIBLE
//                    } else {
//                        holder.binding.selectUnreadMessages.visibility = View.VISIBLE
//                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
 
        holder.binding.selectUserName.text = user.username

        checkOnlineStatus(user.id!!, holder.binding.imgOn)

        if (user.imageURL == "default") {
            holder.binding.selectUserDp.setImageResource(R.drawable.default_dp_2)
        }
        else {
            Glide.with(context).load(user.imageURL).placeholder(R.drawable.default_dp_2).into(holder.binding.selectUserDp)
//            Picasso.get().load(user.imageURL).into(holder.binding.selectUserDp)
        }

        holder.itemView.setOnClickListener {
            //Start chatting activity with the details of the person user is chatting with
            context.startActivity(Intent(context, ChatDetailActivity::class.java)
                    .putExtra("userId", user.id)
                    .putExtra("profilePic", user.imageURL)
                    .putExtra("tap", 0)
                    .putExtra("userName", user.username))
            Animatoo.animateSlideLeft(context)
        }

    }

    private fun checkOnlineStatus(userId : String, image : ImageView) {
        if (checkForInternet(context)) {
            FirebaseDatabase.getInstance().reference.child("Users").child(userId).get()
                .addOnSuccessListener {
                    val status = it.child("status").value

                    if (status != null) {
                        val value = status.toString()
                        if (value == "Online" || value == "typing..")
                            image.setImageResource(R.drawable.ic_online_dot)
                        else
                            image.setImageResource(R.drawable.ic_offline)
                    }
                }
        } else {
            image.setImageResource(R.drawable.ic_offline)
        }
    }

    //Check for internet connection in App
    private fun checkForInternet(context: Context): Boolean {
        //Register activity with the connectivity manager service
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        //If the android version is equa to 'M' or higher, we need to use the network capabilities to check what type of network has the internet connection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Returns a network object corresponding to the currently active default data network
            val network = connectivityManager.activeNetwork ?: return false

            //Representation of the capabilities of an active network
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                //Indicates this network uses a wi-fi transport or wi-fi has network capabilities
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                //Indicates this network has a cellular network or cellular has network capabilities
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                // else return false
                else -> false
            }
        } else {
            //If android version is below 'M'

            @Suppress("DEPRECIATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false

            @Suppress("DEPRECIATION")
            return networkInfo.isConnected
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }
}