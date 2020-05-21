package edu.uw.lho12.yama

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.content_compose.*
import java.lang.Exception

const val REQUEST_SELECT_CONTACT_PHONE_NUMBER = 1

private var phone_number = ""

class SendActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_compose)

        // Back button for returning to reading messages screen
        fab_back.setOnClickListener { view ->
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        if (savedInstanceState != null) {
            // Load information and run methods to "maintain state"
            val savedPhoneNumber = savedInstanceState.getString("phone")
            val messageContent = savedInstanceState.getString("message")
        }

        // Only ask for a contact selection when composing a brand new message (e.g. not responding to some via button in notification)
        if (intent.getBooleanExtra("START_CONTACTS_ID", false)) {
            selectContact()
            intent.removeExtra("START_CONTACTS_ID")
        }

        // Auto-fills the phone number if the user selected the "reply" button in the notification or
        // gets the saved info for when the user rotates the screen
        if (intent.getStringExtra("REPLY_PHONE_NUMBER") != null) {
            phone_number = intent.getStringExtra("REPLY_PHONE_NUMBER") as String
        } else if (savedInstanceState != null) {
            phone_number = savedInstanceState.getString("SEND_PHONE_NUMBER") as String
        }

        phone_number_text.text = phone_number

        // Sends a message when the button is clicked
        send_button.setOnClickListener {
            val messageContent = message_text_input.getText().toString()

            try {
                val smsManager = SmsManager.getDefault() as SmsManager
                smsManager.sendTextMessage(phone_number, null, messageContent, null, null)
                Toast.makeText(applicationContext, "SMS Sent!", Toast.LENGTH_LONG).show()

                phone_number_text.setText(null)
                message_text_input.getText()?.clear()

                select_contact_button.visibility = View.VISIBLE

            } catch (e: Exception) {
                Toast.makeText(applicationContext, "SMS Failed", Toast.LENGTH_LONG).show()
            }
        }

        select_contact_button.setOnClickListener{
            selectContact()
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("SEND_PHONE_NUMBER", phone_number)
        super.onSaveInstanceState(outState)
    }

    fun selectContact() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_SELECT_CONTACT_PHONE_NUMBER)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SELECT_CONTACT_PHONE_NUMBER && resultCode == Activity.RESULT_OK) {
            val contactUri = data?.data
            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
            contentResolver.query(contactUri as Uri, projection, null, null, null).use { cursor ->
                if (cursor!!.moveToFirst()) {
                    val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val number = cursor.getString(numberIndex)
                    select_contact_button.visibility = View.INVISIBLE
                    phone_number_text.text = number
                    phone_number = number
                }
            }
        }
    }

}