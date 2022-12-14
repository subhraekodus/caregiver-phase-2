package com.example.caregiverphase2.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.caregiverphase2.R
import com.example.caregiverphase2.adapter.DashBoardOpenJobsAdapter
import com.example.caregiverphase2.adapter.DashOpenBidAdapter
import com.example.caregiverphase2.adapter.DashQuickCallsAdapter
import com.example.caregiverphase2.databinding.FragmentDashboardBinding
import com.example.caregiverphase2.model.TestModel
import com.example.caregiverphase2.model.pojo.get_open_jobs.Data
import com.example.caregiverphase2.model.repository.Outcome
import com.example.caregiverphase2.utils.PrefManager
import com.example.caregiverphase2.viewmodel.GetOpenJobsViewModel
import dagger.hilt.android.AndroidEntryPoint
import gone
import isConnectedToInternet
import visible
import java.util.ArrayList

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var accessToken: String
    private val mGetOpenJobsViewModel: GetOpenJobsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //get token
        accessToken = "Bearer "+PrefManager.getKeyAuthToken()

        //observer
        getOPenJobsObserver()

        val quickCallList = ArrayList<TestModel>()
        quickCallList.add(TestModel("a"))
        quickCallList.add(TestModel("b"))
        quickCallList.add(TestModel("c"))
        fillQuickCallsRecycler(quickCallList)
        fillOpenBidsRecycler(quickCallList)

        Glide.with(this)
            .load("https://images.unsplash.com/photo-1527980965255-d3b416303d12?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=580&q=80") // image url
            .placeholder(R.color.dash_yellow) // any placeholder to load at start
            .centerCrop()
            .into(binding.userImageView)

    }

    override fun onResume() {
        if(requireActivity().isConnectedToInternet()){
            binding.openJobsShimmerView.visible()
            binding.openJobsShimmerView.startShimmer()
            binding.openJobsRecycler.gone()
            mGetOpenJobsViewModel.getOPenJobs(accessToken)
        }else{
            Toast.makeText(requireActivity(),"No internet connection.", Toast.LENGTH_SHORT).show()
        }
        super.onResume()
    }

    private fun fillQuickCallsRecycler(list: List<TestModel>) {
        val gridLayoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.quickCallRecycler.apply {
            layoutManager = gridLayoutManager
            adapter = DashQuickCallsAdapter(list,requireActivity())
        }
    }

    private fun fillOpenBidsRecycler(list: List<TestModel>) {
        val gridLayoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.openBidsRecycler.apply {
            layoutManager = gridLayoutManager
            adapter = DashOpenBidAdapter(list,requireActivity(),true)
        }
    }

    private fun fillOpenJobsRecycler(list: List<Data>) {
        val gridLayoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.openJobsRecycler.apply {
            layoutManager = gridLayoutManager
            adapter = DashBoardOpenJobsAdapter(list,requireActivity())
        }
    }

    private fun getOPenJobsObserver(){
        mGetOpenJobsViewModel.response.observe(viewLifecycleOwner, Observer { outcome ->
            when(outcome){
                is Outcome.Success ->{
                    binding.openJobsShimmerView.gone()
                    binding.openJobsShimmerView.stopShimmer()
                    if(outcome.data?.success == true){
                        if(outcome.data?.data != null && outcome.data?.data!!.isNotEmpty()){
                            binding.seeAll3Htv.visible()
                            binding.openJobHtv.visible()
                            binding.openJobsRecycler.visible()
                            fillOpenJobsRecycler(outcome.data?.data!!)
                        }else{
                            binding.seeAll3Htv.gone()
                            binding.openJobHtv.gone()
                            binding.openJobsRecycler.gone()
                        }
                        mGetOpenJobsViewModel.navigationComplete()
                    }else{
                        Toast.makeText(requireActivity(),outcome.data!!.message, Toast.LENGTH_SHORT).show()
                    }
                }
                is Outcome.Failure<*> -> {
                    Toast.makeText(requireActivity(),outcome.e.message, Toast.LENGTH_SHORT).show()

                    outcome.e.printStackTrace()
                    Log.i("status",outcome.e.cause.toString())
                }
            }
        })
    }

}