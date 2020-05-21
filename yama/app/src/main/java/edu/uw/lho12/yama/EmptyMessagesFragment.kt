package edu.uw.lho12.yama


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * A simple [Fragment] subclass.
 */
class EmptyMessagesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val noMessagesView =  inflater.inflate(R.layout.fragment_empty_messages, container, false)
        noMessagesView.findViewById<TextView>(R.id.no_messages).text = context?.getString(R.string.no_messages)
        return noMessagesView
    }

    companion object {
        fun newInstance() = EmptyMessagesFragment()
    }


}
