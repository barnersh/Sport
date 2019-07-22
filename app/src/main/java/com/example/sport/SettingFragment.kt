package com.example.sport

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setting.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val FOLLOW = "param1"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SettingFragment.ChangeSetting] interface
 * to handle interaction events.
 * Use the [SettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class SettingFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var follow: Boolean = false
    private var listener: ChangeSetting? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            follow = it.getBoolean(FOLLOW)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        sw_follow.isChecked = follow

        sw_follow.setOnClickListener {
            follow = sw_follow.isChecked
            onButtonPressed(follow)
        }

        btn_Confirm.setOnClickListener {
            val fragmentTransaction = fragmentManager?.beginTransaction()
            fragmentTransaction?.hide(this)
            fragmentTransaction?.show(map)
            fragmentTransaction?.commit()
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(follow: Boolean) {
        listener?.changeSetting(follow)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ChangeSetting) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement ChangeSetting")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        Log.d("detach", "SettingFragmentDetach")
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface ChangeSetting {
        // TODO: Update argument type and name
        fun changeSetting(follow: Boolean)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param follow Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(follow: Boolean) =
            SettingFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(FOLLOW, follow)
                }
            }
    }
}
