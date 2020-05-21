package edu.uw.lho12.yama

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.DatabaseUtils
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.util.Log
import android.view.*
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_compose.*
import kotlinx.android.synthetic.main.content_main.*
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val smsBroadcastReceiver = SmsBroadcastReceiver()
    private var listOfMessages = arrayListOf<SmsInfo>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val intentFilter = IntentFilter("android.provider.Telephony.SMS_RECEIVED")
        registerReceiver(smsBroadcastReceiver , intentFilter)

        fab_compose.setOnClickListener { view ->
            val intent = Intent(this, SendActivity::class.java).apply {
                putExtra("START_CONTACTS_ID", true)
            }
            startActivity(intent)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
            == PackageManager.PERMISSION_GRANTED) {
            setUpAdapter()

        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_SMS),
                1)
        }

        // Asks for permissions to send SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.SEND_SMS),
                1
            )
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECEIVE_SMS),
                1
            )
        }

        if (savedInstanceState != null) {
            val list: ArrayList<SmsInfo>? = savedInstanceState.getParcelableArrayList("messages")
            if (list != null) {
                listOfMessages = list
                setUpAdapter()
            }

        }

    }

    // https://www.youtube.com/watch?time_continue=16&v=C8lUdPVSzDk&feature=emb_logo
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        for (permission in permissions) {
            println("Checking " + permission)
        }
        when (requestCode) {
            1 -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setUpAdapter()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


    override fun onStop() {
        unregisterReceiver(smsBroadcastReceiver)
        super.onStop()
    }

    private fun setUpAdapter() {
        listOfMessages = getSmsInfo()
        content_main.adapter = SmsMessagesAdapter(listOfMessages)
    }


    // https://stackoverflow.com/questions/19856338/using-new-telephony-content-provider-to-read-sms
    private fun getSmsInfo(): ArrayList<SmsInfo> {
        val listSms = arrayListOf<SmsInfo>()
        val cr = contentResolver

        val cursor = cr.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(
                Telephony.Sms.Inbox.PERSON,
                Telephony.Sms.Inbox.ADDRESS,
                Telephony.Sms.Inbox.BODY,
                Telephony.Sms.Inbox.DATE),
            null,
            null,
            Telephony.Sms.Inbox.DEFAULT_SORT_ORDER
        )

        val totalSms = cursor!!.count

        if (cursor.moveToFirst()) {
            for (i in 0 until totalSms) {
                var author: String
                val message = cursor.getString(2)
                val datetime = cursor.getLong(3)

                val dateVal = Date(datetime)
                val format = SimpleDateFormat("MM/dd/yyyy hh:mm")
                val dateText = format.format(dateVal)


                if (cursor.getString(0) != null) {
                    author = cursor.getString(0)
                } else {
                    author = cursor.getString(1)
                }
                listSms.add(
                    SmsInfo(author, message, dateText)
                )
                cursor.moveToNext()
            }
        } else {
            // throw RuntimeException("You have no SMS in Inbox")
            supportFragmentManager.beginTransaction().run {
                replace(R.id.frameLayout, EmptyMessagesFragment.newInstance())
                commit()
            }
        }
        cursor.close()

        return listSms
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("messages", listOfMessages)
    }

    inner class SmsMessagesAdapter(private val messages: List<SmsInfo>) :
        RecyclerView.Adapter<SmsMessagesAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            var author = itemView.findViewById<TextView>(R.id.author)
            var message = itemView.findViewById<TextView>(R.id.message)
            var datetime = itemView.findViewById<TextView>(R.id.datetime)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(applicationContext)
                .inflate(R.layout.message_list_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = messages[position]
            holder.author.text = item.author
            holder.message.text = item.message
            holder.datetime.text = item.datetime
        }

        override fun getItemCount(): Int {
            return messages.size
        }
    }
}